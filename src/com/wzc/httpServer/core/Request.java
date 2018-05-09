package com.wzc.httpServer.core;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Request {
    boolean isUploadFile;
    String method;
    String uri;
    String protol;
    String body;
    HashMap<String, List<String>> params;
    HashMap<String, List<FileParam>> fileParams;//文件参数
    HashMap<String, String> headers;
    OutputStream outputStream;
    HashMap<String, Object> attribute = new HashMap<>();//用于保存属性扩展

    public Request() {
    }

    public Request(String method, String uri, String protol, String body, HashMap<String, List<String>> params, HashMap<String, String> headers, OutputStream outputStream) {
        this.method = method;
        this.uri = uri;
        this.protol = protol;
        this.body = body;
        this.params = params;
        this.headers = headers;
        this.outputStream = outputStream;
    }

    public void setUploadFile(boolean uploadFile) {
        isUploadFile = uploadFile;
    }


    public HashMap<String, List<FileParam>> getFileParams() {
        return fileParams;
    }

    public List<FileParam> getAllFileParams() {
        List<FileParam> fileParams = new ArrayList<>();
        if (null != this.fileParams) {
            for (List<FileParam> files : this.fileParams.values()) {
                if (files != null)
                    fileParams.addAll(files);
            }
        }
        return fileParams;
    }

    public String getParam(String name) {
        return this.getParams() != null && this.getParams().get(name) != null && !this.getParams().get(name).isEmpty() ? this.getParams().get(name).get(0) : null;
    }

    public boolean isUploadFile() {
        return isUploadFile;
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


    public HashMap<String, List<String>> getParams() {
        return params;
    }

    public void setParams(HashMap<String, List<String>> params) {
        this.params = params;
    }

    public void setFileParams(HashMap<String, List<FileParam>> fileParams) {
        this.fileParams = fileParams;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
    }
}
