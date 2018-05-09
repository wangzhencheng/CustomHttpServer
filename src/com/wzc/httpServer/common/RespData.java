package com.wzc.httpServer.common;

public class RespData {
    private int code;
    private String msg;
    private Object data;

    private static final int success = 200;
    private static final int params_error = 400;
    private static final int error = 500;

    public static RespData success(Object object) {
        return new RespData(success, "成功", object);
    }

    public static RespData paramsError() {
        return new RespData(params_error, "参数错误", null);
    }

    public static RespData error(String msg) {
        return new RespData(error, msg == null ? "出错了" : msg, null);
    }


    public RespData() {

    }

    public RespData(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
