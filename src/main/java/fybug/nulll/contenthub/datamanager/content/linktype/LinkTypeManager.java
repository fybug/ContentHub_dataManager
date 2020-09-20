package fybug.nulll.contenthub.datamanager.content.linktype;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fybug.nulll.contenthub.datamanager.content.linktype.pass.value.ValuePass;
import fybug.nulll.contenthub.datamanager.content.linktype.pass.value.Value_NumberPass;
import fybug.nulll.pdcache.PDCache;
import fybug.nulll.pdcache.supplier.memory.SCache;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * <h2>数据类型管理器.</h2>
 * 负责管理数据类型的数据，可获取根据处理类型分类的数据类型列表<br/>
 * 通过 {@link #getPass(int)} 可获取对应的数据存储处理器
 *
 * @author fybug
 * @version 0.0.1
 * @see DataIOofType
 */
public
class LinkTypeManager {

    // 类型列表
    private final LinktypeR[] TypeList;
    // 展示用列表缓存
    private final SCache<Map> LinkMapCache;
    // 数据处理器记录
    private final Map<String, Map<String, DataIOofType>> passMap;

    public
    LinkTypeManager() {
        // 静态列表
        TypeList = new LinktypeR[]{
                /* 值类型 */
                new LinktypeR(1, "string", "字符型", "value"), //
                new LinktypeR(2, "number", "数字型", "value"), //
                /* 文件类型 */
                new LinktypeR(3, "img", "图片文件", "storage"), //
                new LinktypeR(4, "file", "普通文件", "storage"), //
                new LinktypeR(5, "json", "json 数据", "storage"), //
                new LinktypeR(6, "xml", "xml 数据", "storage"), //
                new LinktypeR(7, "html", "html 数据", "storage"), //
                /* 文件组类型 */
                new LinktypeR(8, "group:txt", "文本组", "storage group"), //
                new LinktypeR(9, "group:img", "图片组", "storage group"), //
                new LinktypeR(10, "group:file", "普通文件组", "storage group"), //
                new LinktypeR(11, "group:json", "json 数据组", "storage group"), //
                new LinktypeR(12, "group:xml", "xml 数据组", "storage group"), //
                new LinktypeR(13, "group:html", "html 数据组", "storage group"), //
                /* 连接类型 */
                new LinktypeR(14, "get", "get 连接", "link"), //
                new LinktypeR(15, "post", "post 连接，后面使用 : 对发送的数据进行分割", "link")};

        // 缓存
        LinkMapCache = PDCache.SCache(Map.class).createdata(() -> {
            var map = new HashMap<>();

            String name;
            LinktypeR linktypeR;
            List<TypeR> list;
            /* 分类数据 */
            for ( int i = 0; i < TypeList.length; ){
                // 获取当前类型数据
                linktypeR = TypeList[i];
                name = linktypeR.getType();
                list = (List<TypeR>) map.getOrDefault(name, null);
                // 初始化列表
                if (list == null) {
                    list = new LinkedList<>();
                    map.put(name, list);
                }
                // 加入记录
                list.add(new TypeR(++i, linktypeR.getName(), linktypeR.getDes()));
            }

            return map;
        }).build();

        // 处理器
        passMap = Map.of("value", Map.of("default", new ValuePass(), //
                                         "number", new Value_NumberPass()));
    }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * <h2>展示用类型对象.</h2>
     *
     * @author fybug
     * @version 0.0.1
     */
    @AllArgsConstructor
    @Data
    public static
    class TypeR {
        /** 类型的 id */
        int id;
        /** 类型的名称 */
        String name;
        /** 类型的描述 */
        String des;
    }

    /**
     * 获取按照处理方式分类的类型列表
     *
     * @return 获取的列表
     */
    public
    Map<String, List<TypeR>> listLinkType() throws Exception { return LinkMapCache.get(); }

    /**
     * 根据 id 获取类型对象
     *
     * @param id 类型 id
     *
     * @return 类型对象
     */
    public
    LinktypeR getLinkType(int id) { return TypeList[id + 1]; }

    /*-------------------------------------*/

    /**
     * 获取数据处理器
     *
     * @param id 数据处理类型的 id
     *
     * @return 数据处理器
     */
    public
    DataIOofType getPass(int id) {
        var linktype = TypeList[id - 1];
        if (passMap.containsKey(linktype.getType())) {
            // 当前大类的处理记录
            var map = passMap.get(linktype.getType());
            return map.getOrDefault(linktype.getName(), map.get("default"));
        }
        return null;
    }
}
