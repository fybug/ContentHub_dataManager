package fybug.nulll.contenthub.datamanager.content.datafile.error;

/**
 * <h2>数据被占用异常.</h2>
 * <p>
 * 该异常表示数据实体的位置被占用
 *
 * @author fybug
 * @version 0.0.1
 */
public
class DataOccuipedException extends Exception {

    /** @param id 数据 id */
    public
    DataOccuipedException(int id) {
        super("id: " + id + " is executable,bug not is data!");
    }

    /**
     * @param id    数据 id
     * @param cause 嵌套异常
     */
    public
    DataOccuipedException(int id, Throwable cause) {
        super("id: " + id + " is executable,bug not is data!", cause);
    }
}
