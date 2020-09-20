package fybug.nulll.contenthub.datamanager.content;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * <h2>内容修改用对象.</h2>
 *
 * @author fybug
 * @version 0.0.1
 */
@RequiredArgsConstructor
@Data
public
class ContPutR {
    /** 数据的 id */
    private int id = -1;
    /** 数据的标题 */
    private String title;
    /** 数据的描述 */
    private String des;
    /** 数据的日期 */
    private String date;
    /** 数据的标签 */
    private int[] tags;
    /** 数据的处理类型 */
    private int linkType;
    /** 数据实体存储类型 */
    private String data;
}
