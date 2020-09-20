package fybug.nulll.contenthub.datamanager.content.file;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import fybug.nulll.contenthub.datamanager.DataHub;
import fybug.nulll.contenthub.datamanager.DataManager;
import fybug.nulll.contenthub.datamanager.HubControl;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public
class FileManager extends HubControl {
    public final Path Dirpath;

    public
    FileManager(DataHub dataHub, Path path) {
        super(dataHub);
        Dirpath = path;
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
     */
    public
    void putFile(int id, File tempfile) throws IOException {
        Files.move(tempfile.toPath(), Dirpath.relativize(Path.of("da_" + id)), REPLACE_EXISTING);
    }

    /*-------------------------------*/

    public
    void putGroupFile(int id, File tempfile) {
        // todo 自动分配组内 id
    }

    public
    void createGroup(int id) throws IOException {
        // 组文件夹的路径
        var pa = Dirpath.relativize(Path.of("da_" + id));
        // 初始化文件夹
        createGroupDir(pa, id);

        // 组内 id 的记录文件路径
        var idpa = pa.relativize(Path.of("h" + id + "_id"));
        // 初始化记录文件
        createGroupRecord(idpa, "0");

        // 组内数据记录文件路径
        var grouppa = pa.relativize(Path.of("h" + id + "_re"));
        // 初始化记录文件
        createGroupRecord(grouppa, "");
    }

    /**
     * 创建组文件夹
     * <p>
     * 检查并保持组文件夹的存在
     *
     * @param pa 组文件夹路径
     * @param id 组 id
     */
    private static
    void createGroupDir(Path pa, int id) throws IOException {
        /* 初始化文件夹 */
        if (Files.isExecutable(pa) && !Files.isDirectory(pa))
            throw new IOException("id: " + id + " is has data,bug not is group");
        else
            Files.createDirectories(pa);
    }

    /**
     * 创建组记录文件
     *
     * @param pa     记录的路径
     * @param initda 初始化的数据
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