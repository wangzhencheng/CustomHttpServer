package com.wzc.httpServer.core;

import com.wzc.httpServer.common.RespData;

/**
 * 用于停止http服务器。
 */
public class ExitHandler {

    private String password="admin";

    public ExitHandler(String password) {
        this.password = password;
    }

    public ExitHandler(String[] args,String defaultVal) {
        this.password = new ArgsMap(args).get("password",defaultVal);
    }
    /**
     * 用于停止http程序。
     *
     * @param request
     * @return
     */
    @Handler(value = {"/exit"})
    public Object exit(Request request) {
        String password=request.getParam("password");
        if(this.password.equals(password)){
            SocketHttpHelper.stopAll();
            return RespData.success("exit");
        }
        return RespData.error("错误的密码");
    }

}
