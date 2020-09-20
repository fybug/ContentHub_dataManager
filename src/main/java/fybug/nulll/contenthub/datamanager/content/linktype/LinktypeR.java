package fybug.nulll.contenthub.datamanager.content.linktype;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <h2>数据类型通用对象.</h2>
 *
 * @author fybug
 * @version 0.0.1
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public
class LinktypeR {
    /** 类型 id */
    private int id;
    /** 类型名称 */
    private String name;
    /** 类型描述 */
    private String des;
    /** 处理类型 */
    private String type;
}
