package com.wzc.httpServer.core;

import com.wzc.httpServer.common.ClassPathTools;
import com.wzc.httpServer.common.SimpleTools;

import java.net.URL;

/**
 * jar包内部资源handler。
 */
public class InnerStaticResourceHanlder implements SocketHttpHelper.UrlHandler {

    public boolean matchRequest(Request request) {
        URL url = resolveUrl(request);
        request.getAttribute().put("url", url);
        return url != null;
    }

    private URL resolveUrl(Request request) {
        String uri = request.getUri();
        if (uri == null) uri = "";
        int spliter = uri.indexOf("?");
        if (spliter > -1) {
            uri = uri.substring(0, spliter);
        }

        URL url = ClassPathTools.getURL(uri);
        return url;
    }

    public Response onRequest(Request request) {
        Response response = null;
        try {
            URL realURL = (URL) request.getAttribute().get("url");
            response = new Response(200, "ok",
                    realURL.openStream());
            //根据文件名获取对应的文件类型\名称等信息
            response.getHeaders().putAll(Response.resolveFileTypeHeader(
                    SimpleTools.getUrlFileName(request.getUri()), 0
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;

    }
}
