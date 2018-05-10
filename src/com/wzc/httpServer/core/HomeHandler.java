package com.wzc.httpServer.core;

import com.wzc.httpServer.common.RespData;
import com.wzc.httpServer.common.SimpleTools;

import java.io.File;
import java.util.HashMap;

public class HomeHandler {

    @Handler(value = {"/", "/index"})
    public Object index(Request request) {
        if (request.getFileParams() != null && request.getFileParams().size() > 0) {
            for (FileParam fileParam : request.getAllFileParams()) {
                fileParam.transferFile("C:\\Users\\Administrator\\Desktop\\新建文件夹\\ceshi\\" + fileParam.getFileName());
            }
        }
        return RespData.success("this is index!");
    }

    @Handler(value = "/resp")
    public Object resp(Request request) {
        return RespData.success("ok");
    }

    @Handler(value = "/hello")
    public Object defaultHome(Request request) {
        return Response.view("/index2.html");
    }

    private Object staticResource(Request request) {
        File file = StaticResourceHandler.resolveStaticResourceFile(request, SimpleTools.getRootClassPath());
        return file;
    }

    @Handler("/testParams")
    public Object testParams(Request request) {

        HashMap map = new HashMap();
        map.put("code", 200);

        return map;
    }

    /**
     * 一个HelloWorld，样例。 可以覆盖。
     *
     * @return
     */
    @Handler("/HelloWorld")
    public Object helloworld(Request request) {
        HashMap map = new HashMap();
        map.put("code", "success");
        map.put("msg", "成功");
        map.put("data", "Hello World from Custom Http Server!");

        return map;
    }


}
