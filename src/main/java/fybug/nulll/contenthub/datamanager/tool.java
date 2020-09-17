package fybug.nulll.contenthub.datamanager;
import java.lang.reflect.Array;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.experimental.UtilityClass;

@UtilityClass
public
class tool {

    /**
     * 根据字符串状态进行处理
     *
     * @param <T> 返回的数据类型
     * @param s   要检查的字符串
     * @param has 非空时的处理
     * @param non 为空时的处理
     *
     * @return 处理后的数据
     */
    public
    <T> T checkString(String s, Function<String, T> has, Function<String, T> non) {
        if (s == null || "".equals(s.trim()))
            return non.apply(s);
        return has.apply(s);
    }

    /**
     * 根据字符串状态进行处理
     *
     * @param s   要检查的字符串
     * @param has 非空时的处理
     *
     * @return 处理后的字符串
     */
    public
    String checkString(String s, Function<String, String> has)
    { return checkString(s, has, v -> ""); }

    /**
     * 根据数组状态进行处理
     *
     * @param <U> 传入的数组类型
     * @param <T> 返回的数据类型
     * @param s   要检查的数组
     * @param has 非空时的处理
     * @param non 为空时的处理
     *
     * @return 处理后的数据
     */
    public
    <U, T> T checkArray(U s, Function<U, T> has, Function<U, T> non) {
        if (s == null || Array.getLength(s) == 0)
            return non.apply(s);
        return has.apply(s);
    }
}
