package fybug.nulll.contenthub.datamanager.tags;
import lombok.Data;

/**
 * <h2>标签数据对象.</h2>
 *
 * @author fybug
 * @version 0.0.1
 */
@Data
public final
class TagR {
    /** 标签的 id */
    private int id;
    /** 标签的名称 */
    private String name;
}
