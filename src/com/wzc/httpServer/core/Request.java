package com.wzc.httpServer.core;

import java.io.OutputStream;
import java.util.HashMap;

public class Request {
    String method;
    String uri;
    String protol;
    String body;
    HashMap<String, String> params;
    HashMap<String, String> headers;
    OutputStream outputStream;
    HashMap<String,Object> attribute=new HashMap<>();//用于保存属性扩展

    public Request() {
    }

    public Request(String method, String uri, String protol, String body, HashMap<String, String> params, HashMap<String, String> headers, OutputStream outputStream) {
        this.method = method;
        this.uri = uri;
        this.protol = protol;
        this.body = body;
        this.params = params;
        this.headers = headers;
        this.outputStream = outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public HashMap<String, Object> getAttribute() {
        return attribute;
    }

    public void setAttribute(HashMap<String, Object> attribute) {
        this.attribute = attribute;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getProtol() {
        return protol;
    }

    public void setProtol(String protol) {
        this.protol = protol;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public HashMap<String, String> getParams() {
        return params;
    }

    public void setParams(HashMap<String, String> params) {
        this.params = params;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
    }
}
