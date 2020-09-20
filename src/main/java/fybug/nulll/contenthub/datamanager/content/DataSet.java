package fybug.nulll.contenthub.datamanager.content;
import fybug.nulll.contenthub.datamanager.DataHub;
import fybug.nulll.contenthub.datamanager.HubControl;

// todo 数据实体通过对应的数据类型编译器编译后返回

public abstract
class DataSet extends HubControl {

    public
    DataSet(DataHub dataHub) { super(dataHub); }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 添加数据
     *
     * @param dataR 数据记录对象
     *
     * @return 新记录的 id ，或者失败的状态码
     */
    public abstract
    int add(ContPutR dataR);

    /**
     * 移除数据
     *
     * @param id 数据的 id
     *
     * @return 操作状态码
     */
    public abstract
    int remove(int id);

    /**
     * 修改数据记录
     * <p>
     * 不需要修改的记录使用 {@code null} 标识
     *
     * @param dataR 数据记录对象
     *
     * @return 操作状态码
     */
    public abstract
    int alter(ContPutR dataR);

    /**
     * 移除所有记录中对应的标签记录
     * <p>
     * 用于在 {@link fybug.nulll.contenthub.datamanager.tags.TagsTo#remove(int)} 时删除在数据记录中对应的标签记录，
     * 避免已经删除的数据导致发生错误。
     *
     * @param tags 要删除的标签 id 组
     *
     * @return 操作状态码
     */
    public abstract
    int deleteTagOfCont(int[] tags);
}
