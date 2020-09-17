package fybug.nulll.contenthub.datamanager;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

/**
 * <h2>容器控件.</h2>
 * <p>
 * {@link DataHub} 的控件，指定了必须带有传入 {@link DataHub} 的构造方法。<br/>
 * 使用 {@link SoftReference} 缓存，通过 {@link #getDataHub()} 获取当前的 {@link DataHub} 对象。
 *
 * @author fybug
 * @version 0.0.1
 */
public abstract
class HubControl {

    // DataHub 对象缓存
    private final Reference<DataHub> dataHubReference;

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 构造控件
     *
     * @param dataHub 持有该控件的 DataHub 对象
     */
    public
    HubControl(DataHub dataHub) { this.dataHubReference = new SoftReference<>(dataHub); }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 获取持有该控件的 DataHub 对象
     *
     * @return DataHub 对象
     */
    public final
    DataHub getDataHub() { return dataHubReference.get(); }
}
