package fybug.nulll.contenthub.datamanager.content.file;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import fybug.nulll.contenthub.datamanager.content.file.error.DataOccuipedException;
import fybug.nulll.contenthub.datamanager.content.file.error.NoDataException;
import fybug.nulll.pdcache.PDCache;
import fybug.nulll.pdcache.supplier.memory.SMapCache;
import fybug.nulll.pdconcurrent.SyLock;
import fybug.nulll.pdconcurrent.fun.tryConsumer;
import lombok.Getter;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.DSYNC;
import static java.nio.file.StandardOpenOption.SYNC;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * <h2>数据组管理对象.</h2>
 * <p>
 * 使用 {@link Group#create(int, FileManager, SyLock)} 进行构造<br/>
 * 用于管理一组数据以及其组文件夹<br/>
 * 考虑到数据量的庞大且为了保持数据一致性，数据获取的时候均采用接口注入处理数据的方式，获取到数据的时候建议直接对外输出，而不是缓存起来
 *
 * @author fybug
 * @version 0.0.1
 * @see FileManager
 */
public
class Group {
    /** 当前文件管理器 */
    private final FileManager fm;
    /** 数据组的 id */
    @Getter private final int id;

    /** 组锁 */
    private final SyLock lock;
    /** 记录文件锁 */
    private final SyLock reicLock = SyLock.newRWLock();
    /** id 锁 */
    private final SMapCache<Integer, SyLock> idLock;

    /*--------------------------------------------------------------------------------------------*/

    private
    Group(int id, FileManager fileManager, SyLock lock) {
        this.id = id;
        fm = fileManager;
        this.lock = lock;
        // 锁缓存
        idLock = PDCache.SMapCache(Integer.class, SyLock.class)
                        .createdata(i -> SyLock.newRWLock())
                        .build();
    }

    /** 当前管理的数据根目录 */
    public
    Path getDirpath() { return fm.Dirpath; }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 创建组对象
     *
     * @param id   组的 id
     * @param fm   文件管理器
     * @param lock 锁对象
     *
     * @return 当前组
     *
     * @throws IOException           文件系统错误
     * @throws DataOccuipedException 数据位置被占用
     */
    public static
    Group create(int id, FileManager fm, SyLock lock) throws Exception {
        return lock.trywrite(Exception.class, () -> {
            keepGroupPath(fm.Dirpath, id);
            return new Group(id, fm, lock);
        });
    }

    /**
     * 移除组数据
     *
     * @throws IOException 文件系统错误
     */
    public
    void remove() throws IOException {
        lock.trywrite(IOException.class, () -> {
            // 组文件夹的路径
            var rootpa = checkGroup(this);

            Files.list(rootpa).forEach(p -> {
                try {
                    Files.delete(p);
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
            });
        });
    }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 读取数据内容
     * <p>
     * 为保持数据同步性，采用接口进行数据的交付
     *
     * @param ids 数据的 id
     * @param v   数据获取接口
     *
     * @throws IOException           无法读取文件
     * @throws DataOccuipedException 数据空间被占用
     * @throws NoDataException       数据不存在
     */
    public
    void readData(int ids, tryConsumer<InputStream, IOException> v) throws Exception {
        lock.tryread(Exception.class, () -> {
            // 组文件夹的路径
            var rootpa = checkGroup(this);

            reicLock.tryread(Exception.class, () -> {
                // 生成当前数据路径
                Path dapa = rootpa.resolve(Path.of("gro_" + ids));
                /* 检查数据是否可用 */
                if (Files.isExecutable(dapa)) {
                    // 数据被占用
                    if (Files.isDirectory(dapa)) {
                        throw new DataOccuipedException(ids);
                    }

                    // 交由外部处理数据流
                    v.accept(Files.newInputStream(dapa));
                } else {
                    throw new NoDataException(ids);
                }
            });
        });
    }

    /**
     * 获取组内数据 id 列表
     * <p>
     * 为防止内存溢出，数据采用接口逐个输出，而不是返回列表
     *
     * @param v 数据监听接口，每次传入单个 id 记录
     *
     * @throws IOException 文件系统错误
     */
    public
    void listDataId(Consumer<Integer> v) throws IOException {
        lock.tryread(IOException.class, () -> {
            // 组文件夹的路径
            var rootpa = checkGroup(this);
            // 组内数据记录文件路径
            var grouppa = rootpa.resolve(Path.of("h" + getId() + "_re"));

            /* 逐行读取记录 */
            reicLock.tryread(IOException.class, () -> {
                String s;
                try ( BufferedReader reader = Files.newBufferedReader(grouppa) ) {
                    /* 逐行读取 */
                    while( (s = reader.readLine()) != null ){
                        // 去除空数据
                        if ("".equals(s = s.trim()))
                            continue;
                        // 交由输出处理
                        v.accept(Integer.parseInt(s));
                    }
                }
            });
        });
    }

    /*--------------------------------------------*/

    /**
     * 放入数据到组内
     *
     * @param tmpfile 临时文件路径
     *
     * @return 当前文件的组内 id
     *
     * @throws IOException           文件系统发生错误
     * @throws DataOccuipedException 数据位置被占用
     */
    public
    int putData(File tmpfile) throws Exception {
        return lock.tryread(Exception.class, () -> {
            // 组文件夹的路径
            var rootpa = checkGroup(this);
            // 组内 id 的记录文件路径
            var idpa = rootpa.resolve(Path.of("h" + getId() + "_id"));
            // 组内数据记录文件路径
            var grouppa = rootpa.resolve(Path.of("h" + getId() + "_re"));

            /* 写入数据并修改记录 */
            return reicLock.trywrite(Exception.class, () -> {
                // 组内当前最大 id
                var nowid = Integer.parseInt(Files.readString(idpa)) + 1;
                // 数据的路径
                var dapa = rootpa.resolve(Path.of("gro_" + nowid));

                // 检查数据是否被占用
                if (Files.isDirectory(dapa))
                    throw new DataOccuipedException(nowid);
                // 移动临时文件为组内数据文件
                Files.move(tmpfile.toPath(), dapa, REPLACE_EXISTING);

                // 重写记录文件
                Files.writeString(idpa, String.valueOf(nowid), WRITE, CREATE, TRUNCATE_EXISTING, DSYNC);
                Files.writeString(grouppa,
                                  nowid + System.lineSeparator(), WRITE, APPEND, CREATE, SYNC);
                return nowid;
            });
        });
    }

    /**
     * 设置数据
     *
     * @param groupid 要设置的组内数据 id
     * @param tmpfile 临时文件路径
     *
     * @throws IOException           文件系统错误 | 数据不存在
     * @throws NoDataException       数据不存在
     * @throws DataOccuipedException 数据位置被占用
     */
    public
    void setData(int groupid, File tmpfile) throws Exception {
        lock.tryread(Exception.class, () -> {
            // 组文件夹的路径
            var rootpa = checkGroup(this);

            reicLock.tryread(Exception.class, () -> {
                // 数据路径
                var pa = rootpa.resolve(Path.of("gro_" + groupid));

                /* 修改数据 */
                idLock.get(groupid).trywrite(Exception.class, () -> {
                    // 检查是否是数据
                    if (Files.isExecutable(pa)) {
                        // 并非数据文件且被占用
                        if (Files.isDirectory(pa)) {
                            throw new DataOccuipedException(groupid);
                        }
                        // 移动临时文件为组内数据文件
                        Files.move(tmpfile.toPath(), pa, REPLACE_EXISTING);
                    } else
                        throw new NoDataException(groupid);
                });
            });
        });
    }

    /**
     * 批量移除数据
     *
     * @param groupids 要移除的数据列表
     *
     * @throws IOException 文件系统发生错误
     */
    public
    void removeData(List<Integer> groupids) throws IOException {
        lock.tryread(IOException.class, () -> {
            // 组文件夹的路径
            var rootpa = checkGroup(this);

            /* 删除数据并移除记录 */
            reicLock.trywrite(IOException.class, () -> {
                // 组内数据记录文件路径
                var grouppa = rootpa.resolve(Path.of("h" + getId() + "_re"));
                // 记录转移用临时文件
                var grouppapatmp =
                        fm.putTemp(getId(), new ByteArrayInputStream(new byte[0])).toPath();

                /* 修改记录 */
                String s;
                int nowi;
                try ( BufferedWriter grouppastream = Files.newBufferedWriter(grouppapatmp, UTF_8, WRITE, TRUNCATE_EXISTING);
                      BufferedReader groupparead = Files.newBufferedReader(grouppa, UTF_8)
                ) {
                    /* 逐行对比记录 */
                    checkid:
                    while( (s = groupparead.readLine()) != null ){
                        // 空行不处理
                        if ("".equals(s = s.trim()))
                            continue;
                        // 转换为数值
                        nowi = Integer.parseInt(s);

                        // 对比所有要删除的 id
                        for ( Integer groupid : groupids ){
                            // 不记录
                            if (nowi == groupid)
                                continue checkid;
                        }
                        /* 记录不需要删除的数据 */
                        grouppastream.write(s);
                        grouppastream.newLine();
                    }

                    // 覆盖数据记录
                    grouppastream.flush();
                    Files.move(grouppapatmp, grouppa, REPLACE_EXISTING);
                }

                /* 删除文件 */
                for ( Integer groupid : groupids ){
                    Files.deleteIfExists(rootpa.resolve(Path.of("gro_" + groupid)));
                }
            });
        });
    }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 检查组空间
     *
     * @param group 当前要检查的组
     *
     * @return 组空间路径
     *
     * @throws IOException 组空间不存在
     */
    private static
    Path checkGroup(Group group) throws IOException {
        // 组文件夹的路径
        var rootpa = group.getDirpath().resolve(Path.of("da_" + group.getId()));
        // 检查是否还有组空间
        if (!Files.isDirectory(rootpa)) {
            throw new IOException("group is remove!");
        }
        return rootpa;
    }

    /**
     * 保持组内容空间完整性
     *
     * @param pa 组空间路径
     * @param id 组 id
     *
     * @throws IOException           文件系统错误
     * @throws DataOccuipedException 数据位置被占用
     */
    private static
    void keepGroupPath(Path pa, int id) throws Exception {
        // 组文件夹的路径
        var rootpa = pa.resolve(Path.of("da_" + id));

        /* 初始化文件夹 */
        if (Files.isExecutable(rootpa) && !Files.isDirectory(rootpa)) {
            throw new DataOccuipedException(id);
        } else {
            Files.createDirectories(rootpa);
        }

        // 组内 id 的记录文件路径
        var idpa = rootpa.resolve(Path.of("h" + id + "_id"));
        createGroupRecord(idpa, "0");

        // 组内数据记录文件路径
        var grouppa = rootpa.resolve(Path.of("h" + id + "_re"));
        // 初始化记录文件
        createGroupRecord(grouppa, "");
    }

    /**
     * 创建组记录文件
     *
     * @param pa     记录的路径
     * @param initda 初始化的数据
     *
     * @throws IOException 文件系统错误
     */
    private static
    void createGroupRecord(Path pa, String initda) throws IOException {
        // 检查文件存在
        if (Files.isExecutable(pa)) {
            // 移除文件夹并重置为文件
            if (Files.isDirectory(pa)) {
                Files.delete(pa);
                Files.createFile(pa);
            }
        } else
            Files.createFile(pa);

        // 初始化数据
        if (Files.size(pa) == 0)
            Files.writeString(pa, initda, UTF_8);
    }
}
