package com.wzc.httpServer.core;

import com.wzc.httpServer.common.RespData;

import java.util.Timer;
import java.util.TimerTask;

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
            Timer timer=new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    SocketHttpHelper.stopAll();
                    timer.cancel();
                }
            },500);
            return RespData.success("exit");
        }
        return RespData.error("错误的密码");
    }

}
