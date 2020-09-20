package fybug.nulll.contenthub.datamanager.content;
import java.util.HashMap;
import java.util.Map;

import fybug.nulll.contenthub.datamanager.DataHub;
import fybug.nulll.contenthub.datamanager.HubControl;
import fybug.nulll.contenthub.datamanager.state;

// todo 数据库只包含 id，标题，描述，标签 id 组，数据类型 id，日期
// todo 数据实体通过对应的数据类型编译器编译后返回

/**
 * <h2>内容数据获取接口.</h2>
 * <p>
 * 数据查询用接口，使用条件赋予函数
 * <ul>
 *     <li>{@link #queryText(String)} 匹配标题或描述中的内容</li>
 *     <li>{@link #hasTags(int[])} 拥有指定标签</li>
 *     <li>{@link #isLinktype(int[])} 指定数据类型</li>
 *     <li>{@link #inDate(String, String)} 数据的时间区间</li>
 * </ul>
 * 赋予查找条件，通过 {@link #queryDatalist()} 触发条件，查找对应数据的属性列表。<br/>
 * 如果只需要通过 id 查找单个数据
 * <ul>
 *     <li></li>
 * </ul>
 *
 * @author fybug
 * @version 0.0.1
 * @see state
 */
public abstract
class DataGet extends HubControl {

    public
    DataGet(DataHub dataHub) { super(dataHub); }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 数据标题或描述所匹配的文本
     *
     * @param query 搜索的关键字
     *
     * @return this
     */
    public abstract
    DataGet queryText(String query);

    /**
     * 指定数据所包含的标签
     *
     * @param tags 标签的 id
     *
     * @return this
     */
    public abstract
    DataGet hasTags(int[] tags);

    /**
     * 指定数据的处理类型
     *
     * @param linktypes 处理类型的 id
     *
     * @return this
     */
    public abstract
    DataGet isLinktype(int[] linktypes);

    /**
     * 指定数据的日期范围
     *
     * @param after 在此日期后
     * @param befo  在此日期前
     *
     * @return this
     */
    public abstract
    DataGet inDate(String after, String befo);

    /*--------------------------------------*/

    /**
     * 根据当前条件搜索数据
     *
     * @return 正常在 {@code data} 映射中加入 {@link ContListR} 数组，发生错误则在 {@code error} 映射中写入状态码
     */
    public abstract
    Map<String, Object> queryDatalist();

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 根据 id 查找数据
     *
     * @param id 数据的 id
     *
     * @return 正常在 {@code data} 映射中加入 {@link ContListR} 对象，发生错误则在 {@code error} 映射中写入状态码
     */
    public abstract
    Map<String, Object> queryData(int id);

    /**
     * 根据 id 获取数据内容
     *
     * @param id 数据的 id
     *
     * @return 正常在 {@code data} 映射中写入当前返回的数据，发生错误则在 {@code error} 映射中写入状态码
     */
    public
    Map<String, Object> getOfid(int id) {
        // todo 获取文件
        return new HashMap<>();
    }

}
