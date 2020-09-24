package fybug.nulll.contenthub.datamanager.content.file.error;

/**
 * <h2>数据不存在异常.</h2>
 * <p>
 * 该异常表示数据实体不存在
 *
 * @author fybug
 * @version 0.0.1
 */
public
class NoDataException extends Exception {

    /** @param id 数据 id */
    public
    NoDataException(int id) {
        super("id: " + id + " not is data!");
    }

    /**
     * @param id    数据 id
     * @param cause 嵌套异常
     */
    public
    NoDataException(int id, Throwable cause) {
        super("id: " + id + " not is data!", cause);
    }
}
