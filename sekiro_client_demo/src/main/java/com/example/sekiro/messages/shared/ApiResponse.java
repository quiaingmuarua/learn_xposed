package com.example.sekiro.messages.shared;

public class ApiResponse<T> {
    public String status;   // "ok" / "error"
    public int code;        // 正常为 200，错误为自定义错误码
    public String message;  // 错误信息或成功说明
    public T data;
    public int duration;

    public ApiResponse(String status, T data) {
        this.status = status;
        this.code = 200;
        this.data = data;
    }

    public ApiResponse(int code, String message) {
        this.status = "error";
        this.code = code;
        this.message = message;
    }

    public ApiResponse() {

    }

    public static ApiResponse<?> fromException(CommandException e) {
        return new ApiResponse<>(e.getErrorCode().code, e.getMessage());
    }

    public static ApiResponse<?> fromException(Exception e) {
        if (e instanceof CommandException) {
            CommandException exception = (CommandException) e;
            return new ApiResponse<>(exception.getErrorCode().code, e.getMessage());
        }
        return new ApiResponse<>(ErrorCode.UNKNOWN_ERROR.code, e.getMessage());
    }


    public void setDuration(float duration) {
        this.duration = (int) (duration);

    }

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.code = 200;
        resp.status = "ok";
        resp.data = data;
        return resp;
    }

}
