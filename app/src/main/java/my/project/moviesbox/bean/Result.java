package my.project.moviesbox.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2025/11/3 15:14
 */
@Data
public class Result <T> {
    private ResultEnum resultEnum;
    private String msg;
    private T data;

    public Result() {}

    public Result(ResultEnum resultEnum, String msg, T data) {
        this.resultEnum = resultEnum;
        this.msg = msg;
        this.data = data;
    }

    public boolean isSuccess() {
        return this.resultEnum.equals(ResultEnum.SUCCESS);
    }

    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(ResultEnum.SUCCESS, msg, data);
    }

    public static <T> Result<T> success(T data) {
        return success("", data);
    }

    public static Result<Void> success(String msg) {
        return new Result<>(ResultEnum.SUCCESS, msg, null);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(ResultEnum.ERROR, msg, null);
    }
    public static <T> Result<T> error(String msg, T data) {
        return new Result<>(ResultEnum.ERROR, msg, data);
    }

    @Getter
    @AllArgsConstructor
    public enum ResultEnum {
        SUCCESS, ERROR;
    }
}