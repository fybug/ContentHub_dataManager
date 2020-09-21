package fybug.nulll.contenthub.datamanager.content.file;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import fybug.nulll.contenthub.datamanager.DataHub;
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
 * <h2>.</h2>
 *
 * @author fybug
 * @version 0.0.1
 * @see FileManager
 */
public
class Group {
    private final FileManager fm;
    /** 数据组的 id */
    @Getter private final int id;

    public static
    void main(String[] args) throws IOException {
        var fm = new FileManager(new DataHub(2), Path.of("tmp/a"));
        var g = new Group(1, fm);

        var i = g.putData(fm.putTemp(2, new ByteArrayInputStream("asd".getBytes())));
        g.setData(i, fm.putTemp(2, new ByteArrayInputStream("ghg".getBytes())));
        g.removeData(List.of(i));
    }

    // todo 上锁
    public
    Group(int id, FileManager fileManager) {
        this.id = id;
        fm = fileManager;
    }

    /*--------------------------------------------------------------------------------------------*/

    /** 当前管理的数据根目录 */
    public
    Path getDirpath() { return fm.Dirpath; }

    /*--------------------------------------------------------------------------------------------*/

    // todo 获取数据和路径列表

    /*--------------------------------------------*/

    /**
     * 放入数据到组内
     *
     * @param tmpfile 临时文件路径
     *
     * @return 当前文件的组内 id
     *
     * @throws IOException 文件系统发生错误
     */
    public
    int putData(File tmpfile) throws IOException {
        keepGroupPath(getDirpath(), getId());

        // 组文件夹的路径
        var rootpa = getDirpath().resolve(Path.of("da_" + getId()));
        // 组内 id 的记录文件路径
        var idpa = rootpa.resolve(Path.of("h" + getId() + "_id"));
        // 组内数据记录文件路径
        var grouppa = rootpa.resolve(Path.of("h" + getId() + "_re"));

        // 组内当前最大 id
        var nowid = Integer.parseInt(Files.readString(idpa));
        /* 保存为数据 */
        {
            // 移动临时文件为组内数据文件
            Files.move(tmpfile.toPath(), rootpa.resolve(Path.of(
                    "gro_" + ++nowid)), REPLACE_EXISTING);
            // 重写 id 记录文件
            Files.writeString(idpa, String.valueOf(nowid), WRITE, CREATE, TRUNCATE_EXISTING, DSYNC);
            Files.writeString(grouppa, nowid + System.lineSeparator(), WRITE, APPEND, CREATE, SYNC);
        }
        return nowid;
    }

    /**
     * 设置数据
     *
     * @param groupid 要设置的组内数据 id
     * @param tmpfile 临时文件路径
     *
     * @throws IOException 文件系统错误 | 数据不存在
     */
    public
    void setData(int groupid, File tmpfile) throws IOException {
        keepGroupPath(getDirpath(), getId());

        // 组文件夹的路径
        var rootpa = getDirpath().resolve(Path.of("da_" + getId()));
        // 数据路径
        var pa = rootpa.resolve(Path.of("gro_" + groupid));

        // 检查是否是数据
        if (Files.isExecutable(pa)) {
            if (Files.isDirectory(pa))
                throw new IOException(
                        "id: " + getId() + '_' + groupid + " is executable,bug not is data!");

            // 移动临时文件为组内数据文件
            Files.move(tmpfile.toPath(), pa, REPLACE_EXISTING);
        } else
            throw new IOException("id: " + getId() + '_' + groupid + " not is data!");
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
        keepGroupPath(getDirpath(), getId());

        // 组文件夹的路径
        var rootpa = getDirpath().resolve(Path.of("da_" + getId()));

        /* 转移记录，并剔除要删除的记录 */
        {
            // 组内数据记录文件路径
            var grouppa = rootpa.resolve(Path.of("h" + getId() + "_re"));
            // 记录转移用临时文件
            var grouppapatmp = fm.putTemp(getId(), new ByteArrayInputStream(new byte[0])).toPath();

            /* 转移数据 */
            try ( BufferedWriter grouppastream = Files.newBufferedWriter(grouppapatmp, UTF_8, WRITE, TRUNCATE_EXISTING);
                  BufferedReader groupparead = Files.newBufferedReader(grouppa, UTF_8)
            ) {
                String s;
                int nowi;

                /* 逐行读取 */
                checkid:
                while( (s = groupparead.readLine()) != null ){
                    s = s.trim();

                    // 空行不处理
                    if ("".equals(s))
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

                grouppastream.flush();
            }

            // 覆盖数据记录
            Files.move(grouppapatmp, grouppa, REPLACE_EXISTING);
        }

        /* 删除文件 */
        for ( Integer groupid : groupids ){
            Files.deleteIfExists(rootpa.resolve(Path.of("gro_" + groupid)));
        }
    }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 保持组内容空间完整性
     *
     * @param pa 组空间路径
     * @param id 组 id
     *
     * @throws IOException 文件系统错误
     */
    private static
    void keepGroupPath(Path pa, int id) throws IOException {
        // 组文件夹的路径
        var rootpa = pa.resolve(Path.of("da_" + id));
        /* 初始化文件夹 */
        if (Files.isExecutable(rootpa) && !Files.isDirectory(rootpa)) {
            throw new IOException("id: " + id + " is has data,bug not is group");
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
