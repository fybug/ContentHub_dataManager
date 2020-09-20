package fybug.nulll.contenthub.datamanager.content.linktype;
import fybug.nulll.contenthub.datamanager.DataHub;

/**
 * <h2>数据处理接口.</h2>
 * <p>
 * 在此接口中定义不同数据处理类型的数据展示以及存储方式。
 * <p>
 * 将原数据处理成前端可用的数据类型，或者将前端传来的数据转换成可存储的数据
 *
 * @author fybug
 * @version 0.0.3
 */
public
interface DataIOofType {
    /**
     * 数据读取方法
     * <p>
     * 根据不同类型的数据使用不同的方式返回。<br/>
     * 返回的数据给予前端使用。
     *
     * @param dataHub 数据所属的容器
     * @param id      数据的 id
     *
     * @return 返回的数据
     */
    String passdata(DataHub dataHub, int id);

    /**
     * 数据输入方法
     * <p>
     * 根据不同的类型使用不同的数据存储方式，在此处处理传来的数据,
     * 并存储于对应的数据容器中
     *
     * @param dataHub 数据所属的容器
     * @param id      数据的 id
     * @param data    前端传来的数据
     */
    void datato(DataHub dataHub, int id, String data);
}
