package fybug.nulll.contenthub.datamanager.content;
import lombok.Data;

/**
 * <h2>内容属性对象.</h2>
 *
 * @author fybug
 * @version 0.0.1
 */
@Data
public
class ContR {
    /** 数据的 id */
    private int id;
    /** 数据的标题 */
    private String title;
    /** 数据的描述 */
    private String des;
    /** 数据的日期 */
    private String date;
    /** 数据的标签 */
    private String[] tags;
    /** 数据的处理类型 */
    private String linkType;
}
