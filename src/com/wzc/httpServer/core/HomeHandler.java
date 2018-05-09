package com.wzc.httpServer.core;

import com.wzc.httpServer.common.RespData;
import com.wzc.httpServer.common.SimpleTools;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeHandler {

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

    @Handler("/fileList")
    public Object fileList(Request request) {
        String path = request.getParams().get("path");

        System.out.println("fileList:" + path);

        File file = new File(path.trim());
        File[] subFiles = file.listFiles();

        System.out.println("fileList:" + subFiles);
        List<String> fileNames = new ArrayList<String>();
        if (subFiles != null) {
            for (File f : subFiles) {
                fileNames.add(f.getName());
            }
        }

        HashMap map = new HashMap();
        map.put("code", 200);
        map.put("data", fileNames);

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
