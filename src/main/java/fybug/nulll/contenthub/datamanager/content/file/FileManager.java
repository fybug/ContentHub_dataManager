package fybug.nulll.contenthub.datamanager.content.file;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import fybug.nulll.contenthub.datamanager.DataHub;
import fybug.nulll.contenthub.datamanager.DataManager;
import fybug.nulll.contenthub.datamanager.HubControl;
import fybug.nulll.contenthub.datamanager.content.file.error.DataOccuipedException;
import fybug.nulll.contenthub.datamanager.content.file.error.NoDataException;
import fybug.nulll.pdcache.PDCache;
import fybug.nulll.pdcache.supplier.memory.SMapCache;
import fybug.nulll.pdconcurrent.SyLock;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * <h2>文件内容管理器.</h2>
 * 提供数据的管理，组数据对象的获取等。<br/>
 * 内部使用高一致性的 {@link SMapCache} 缓存对象按照 id 存放数据的锁，通过该锁保持单个数据的一致性<br/>
 * 不提供 id 记录或管理，需要单独实现并传入
 * <br/><br/>
 * 存储数据前使用 {@link #putTemp(int, InputStream)} 存放到临时文件中，然后使用 {@link #putFile(int, File)} 移动临时文件为数据文件。<br/>
 * 组数据通过 {@link Group} 对象管理单个组的数据。
 *
 * @author fybug
 * @version 0.0.1
 * @see Group
 */
public
class FileManager extends HubControl {

    /** 当前管理的数据根目录 */
    public final Path Dirpath;
    private final SMapCache<Integer, SyLock> LockMap;

    /**
     * 初始化文件管理器
     *
     * @param dataHub 数据容器
     * @param path    数据根路径
     *
     * @throws IOException 数据根目录创建失败
     */
    public
    FileManager(DataHub dataHub, Path path) throws IOException {
        super(dataHub);
        Dirpath = path;
        Files.createDirectories(Dirpath);
        LockMap = PDCache.SMapCache(Integer.class, SyLock.class)
                         .createdata(id -> SyLock.newRWLock())
                         .build();
    }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 存放临时文件
     * <p>
     * 临时文件会以 {@code "p{ DataHub id }I{ Data Id }_{ Temp Id }.tmp" } 命名保存
     *
     * @param id          数据 id
     * @param inputStream 临时文件用的输入流
     *
     * @return 临时文件路径
     *
     * @throws IOException 无法创建临时目录 | 无法写入临时文件
     */
    public
    File putTemp(int id, InputStream inputStream) throws IOException {
        var patyh = DataManager.getTempFile().toFile();

        /* 检查是否能创建文件夹 */
        if (!patyh.isDirectory()) {
            patyh.mkdirs();
            if (!patyh.isDirectory())
                throw new IOException("can`t create TempDir");
        }

        // 临时文件路径
        var tmpfile =
                File.createTempFile("h" + getDataHub().getId() + "I" + id + "_", ".tmp", patyh);

        /* 输出到文件流中 */
        try ( var out = Files.newOutputStream(tmpfile.toPath()) ) {
            int i;
            while( (i = inputStream.read()) != -1 )
                out.write(i);
            out.flush();
        }

        return tmpfile;
    }

    /**
     * 将临时文件存放为数据记录
     *
     * @param id       数据的 id
     * @param tempfile 临时文件的路径
     *
     * @throws IOException 无法移动文件
     * @throws Exception   锁缓存发生错误
     */
    public
    void putFile(int id, File tempfile) throws Exception {
        LockMap.get(id)
               .trywrite(IOException.class, () -> Files.move(tempfile.toPath(), Dirpath.resolve(Path.of(
                       "da_" + id)), REPLACE_EXISTING));
    }

    /**
     * 获取数据的路径
     *
     * @param id 数据 id
     *
     * @return 数据的路径
     *
     * @throws Exception             锁缓存发生错误
     * @throws NoDataException       无数据
     * @throws DataOccuipedException 数据位置被占用
     */
    public
    Path getDatapath(final int id) throws Exception {
        return LockMap.get(id).tryread(Exception.class, () -> {
            var pa = Dirpath.resolve(Path.of("da_" + id));

            // 检查数据是否存在
            if (Files.isExecutable(pa)) {
                // 数据被占用
                if (Files.isDirectory(pa)) {
                    throw new DataOccuipedException(id);
                }
                return pa;
            }
            throw new NoDataException(id);
        });
    }

    /**
     * 获取数据的内容
     *
     * @param id 数据的 id
     *
     * @return 读取用的数据流
     *
     * @throws IOException 无法打开文件
     * @throws Exception   锁缓存发生错误
     */
    public
    InputStream getData(int id) throws Exception {
        return LockMap.get(id)
                      .tryread(IOException.class, () -> Files.newInputStream(Dirpath.resolve(Path.of(
                              "da_" + id))));
    }

    /**
     * 删除数据
     *
     * @param id 数据的 id
     *
     * @throws IOException 文件系统错误
     * @throws Exception   锁缓存发生错误
     */
    public
    void removeData(int id) throws Exception {
        LockMap.get(id)
               .trywrite(IOException.class, () -> Files.deleteIfExists(Dirpath.resolve(Path.of(
                       "da_" + id))));
    }

    /*-------------------------------*/

    /**
     * 创建组数据
     *
     * @param id 新的组数据的 id
     *
     * @return 组对象
     *
     * @throws IOException           文件系统错误
     * @throws DataOccuipedException 数据位置被占用
     */
    public
    Group createGroup(int id) throws Exception {
        return Group.create(id, this, LockMap.get(id));
    }

    /**
     * 获取组对象
     *
     * @param id 组数据 id
     *
     * @return 组对象
     *
     * @throws IOException 组数据不存在
     * @throws Exception   锁缓存错误
     */
    public
    Group getGroup(int id) throws Exception { return Group.get(id, this, LockMap.get(id)); }
}
