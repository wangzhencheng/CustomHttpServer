package com.wzc.httpServer.core;

import com.wzc.httpServer.common.CustomJsonMapper;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class HandlerAnnotationHandler implements SocketHttpHelper.UrlHandler {
    private String[] urls;
    private String[] methods;
    private Object object;
    private Method method;

    //没有输出的相应。使用该值，需要自定义输出。
    private static final Response NO_RESPONSE = new Response(null) {
        @Override
        public void response(OutputStream os) {
        }
    };

    public HandlerAnnotationHandler(Object object, Method method, String[] urls, String[] methods) {
        this.object = object;
        this.method = method;
        this.urls = urls;
        this.methods = methods;
    }

    @Override
    public boolean matchRequest(Request request) {
        if (request.getUri() == null || urls == null) {
            return false;
        }
        boolean urlMatch = false;
        for (String url : urls) {
            String uri = request.getUri();
            int pDividerIndex = uri.indexOf("?");
            if (pDividerIndex > 0)
                uri = uri.substring(0, pDividerIndex);
            if (uri.endsWith("/"))
                uri = uri.substring(0, uri.length() - 1);
            if (url.endsWith("/"))
                url = url.substring(0, url.length() - 1);
            if (uri.equalsIgnoreCase(url)) {
                urlMatch = true;
                break;
            }
        }

        boolean methodMatch = false;
        if (urlMatch) {
            if (methods == null || methods.length == 0) {
                methodMatch = true;
            } else {
                for (String method : methods) {
                    if (request.getMethod().equalsIgnoreCase(method)) {
                        methodMatch = true;
                        break;
                    }
                }
            }
        }
        return urlMatch && methodMatch;
    }

    @Override
    public Response onRequest(Request request) {
        try {
            Object rsObj = null;
            Parameter[] parameters = method.getParameters();
            if (parameters == null || parameters.length == 0) {
                rsObj = method.invoke(object);
            } else {
                rsObj = method.invoke(object, request);
            }
            if (rsObj != null) {
                if (rsObj instanceof Response) {
                    return (Response) rsObj;
                }
                if (rsObj instanceof File
                        || rsObj instanceof InputStream
                        || rsObj instanceof String) {
                    return new Response(rsObj);
                }
                //自己可以处理Json解析。或者在方法中自行处理。
                return new Response(CustomJsonMapper.getDefault().writeValueAsString(rsObj));
            } else {
                return NO_RESPONSE;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(500, "ERROR", String.valueOf(e));
        }
    }

}
