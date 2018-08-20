package com.wzc.httpServer.core;

import java.io.File;
import java.util.HashMap;

/**
 * jar包同目录下的静态资源搜索。优先级高于jar包内部静态资源
 */
public class StaticResourceHandler implements SocketHttpHelper.UrlHandler {
    private HashMap<Request, File> cacheFile = new HashMap<>();
    private String serverPath = null;

    public StaticResourceHandler(String serverPath) {
        this.serverPath = serverPath;
    }

    @Override
    public boolean matchRequest(Request request) {
        File file = resolveStaticResourceFile(request, serverPath);
        boolean match=file.exists()&&file.canRead()&&!file.isDirectory();
        if (match) {
            cacheFile.put(request, file);
        }
        return match;
    }

    public static File resolveStaticResourceFile(String uri, String serverPath) {
        if (uri == null) uri = "";
        int spliter = uri.indexOf("?");
        if (spliter > -1) {
            uri = uri.substring(0, spliter);
        }
        if (uri.startsWith("/"))
            uri = uri.substring(1);
        File file = new File(serverPath, uri);
        return file;
    }

    public static File resolveStaticResourceFile(Request request, String serverPath) {
        return resolveStaticResourceFile(request.getUri(), serverPath);
    }


    @Override
    public Response onRequest(Request request) {
        File file = cacheFile.remove(request);
        return new Response(200, "ok", file);
    }

}
