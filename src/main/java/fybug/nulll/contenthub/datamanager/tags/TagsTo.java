package fybug.nulll.contenthub.datamanager.tags;
import java.util.Map;

import fybug.nulll.contenthub.datamanager.DataHub;
import fybug.nulll.contenthub.datamanager.HubControl;
import fybug.nulll.contenthub.datamanager.state;

/**
 * <h2>标签操作接口.</h2>
 * <p>
 * 操作的返回使用 {@link state} 状态码
 *
 * @author fybug
 * @version 0.0.1
 * @see state
 * @see TagR
 */
public abstract
class TagsTo extends HubControl {

    public
    TagsTo(DataHub dataHub) { super(dataHub); }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 列取标签
     *
     * @param query 查询表达式
     * @param all   是否取全部
     *
     * @return 正常在 {@code data} 映射中加入 {@link TagR} 数组，发生错误则在 {@code error} 映射中写入状态码
     */
    public abstract
    Map<String, Object> listTags(String query, boolean all);

    /*-------------------------*/

    /**
     * 追加标签
     *
     * @param name 新标签的名称
     *
     * @return 状态码或新标签的 id
     */
    public abstract
    int add(String name);

    /**
     * 移除标签
     *
     * @param id 标签的 id
     *
     * @return 状态码
     */
    public abstract
    int remove(int id);

    /**
     * 修改标签
     *
     * @param id      要修改的标签的 id
     * @param newname 标签的新名称
     *
     * @return 状态码
     */
    public abstract
    int alter(int id, String newname);
}
