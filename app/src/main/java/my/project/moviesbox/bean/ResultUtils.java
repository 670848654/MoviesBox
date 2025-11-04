package my.project.moviesbox.bean;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2025/11/3 15:16
 */
public class ResultUtils {
    private static String ERROR_BASE_MSG = "解析过程中出现错误!\n%s";
    private static String PARSER_ERROR_MSG = "当前解析代码无法解析相关内容!";
    public static <T> Result<T> ok(String msg, T data) {
        return Result.success(msg, data);
    }

    public static <T> Result<T> ok(T data) {
        return Result.success(data);
    }

    public static Result<Void> ok(String message) {
        return Result.success(message);
    }

    public static <T> Result<T> fail(String message) {
        return Result.error(String.format(ERROR_BASE_MSG, message));
    }

    public static <T> Result<T> parserFail() {
        return Result.error(String.format(ERROR_BASE_MSG, PARSER_ERROR_MSG));
    }

    public static <T> Result<T> fail(String message, T data) {
        return Result.error(String.format(ERROR_BASE_MSG, message), data);
    }
}
