package com.wzc.httpServer.core;

import com.wzc.httpServer.common.ClassPathTools;
import com.wzc.httpServer.common.CustomJsonMapper;
import com.wzc.httpServer.common.SimpleTools;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Response {
    int code;//http的code  200 404等。
    String message;
    Object body;
    HashMap<String, String> headers;//返回的头信息
    String charset = "UTF-8";

    /**
     * 返回文本的时候构造函数
     *
     * @param code
     * @param message
     * @param body
     */
    public Response(int code, String message, Object body) {
        init();
        body=fixBody(body);
        this.code = code;
        this.message = message;
        this.body = body;
        headers.putAll(resolveHeader(body));
    }

    /**
     * 只支持字符串 文件 输入流。其他格式转成对应支持的格式，或者使用OutputStream自行输出
     * @param body
     * @return
     */
    private Object fixBody(Object body) {
        if (null != body) {
            if (!(body instanceof String)
                    && !(body instanceof File)
                    && !(body instanceof InputStream)
                    ) {
                body = CustomJsonMapper.getDefault().writeValueAsString(body);
            }
        }
        return body;
    }

    /**
     * 返回项目内部的html
     *
     * @param htmlFileAbsPath
     * @return
     */
    public static Response view(String htmlFileAbsPath) {
        Response response = null;
        try {
            URL realURL = ClassPathTools.getURL(htmlFileAbsPath);
            response = new Response(200, "ok", realURL.openStream());
            response.getHeaders().putAll(
                    Response.resolveFileTypeHeader(SimpleTools.getUrlFileName(realURL.getPath()),
                            0L));
        } catch (Exception var4) {
            var4.printStackTrace();
        }
        return response;
    }

    /**
     * 图片外部File
     *
     * @param outFilePath
     * @return
     */
    public static File outViewFile(String outFilePath) {
        File file = StaticResourceHandler.resolveStaticResourceFile(outFilePath, ClassPathTools.getOutClassPath(null));
        return file;
    }

    /**
     * 根据文件名称，头信息数据处理。
     *
     * @param fileName
     * @param fileLen
     * @return
     */
    public static HashMap<String, String> resolveFileTypeHeader(String fileName, long fileLen) {
        HashMap<String, String> headers = new HashMap<>();
        try {

            String binaryContentType = "application/octet-stream";
            String respFileName = URLEncoder.encode(fileName, "UTF-8");
            boolean canInline = false;
            String fileContentType = binaryContentType;

            if (fileName.contains(".")) {
                String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
                List<String> imgSuffix = Arrays.asList("jpg", "png", "gif", "jpeg");
                if (imgSuffix.contains(suffix.toLowerCase())) {
                    fileContentType = "image/" + suffix;
                    canInline = true;
                }
                if ("html".equalsIgnoreCase(suffix)
                        || "htm".equalsIgnoreCase(suffix)) {
                    fileContentType = "text/html";
                    canInline = true;
                }
                if ("txt".equalsIgnoreCase(suffix)) {
                    fileContentType = "text/plain";
                    canInline = true;
                }
                if ("xml".equalsIgnoreCase(suffix)
                        || "tld".equalsIgnoreCase(suffix)) {
                    fileContentType = "text/xml";
                    canInline = true;
                }
            }
            headers.put("Content-Type", fileContentType);
            headers.put("Content-Disposition", (canInline ? "inline" : "attachment") + ";filename=" + respFileName);
            if (fileLen > 0)
                headers.put("Content-Length", "" + fileLen);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return headers;
    }

    public static HashMap<String, String> resolveHeader(Object body) {
        return resolveHeader(body, "UTF-8");
    }

    // url
    public static HashMap<String, String> resolveHeader(Object body, String charset) {
        HashMap<String, String> headers = new HashMap<>();
        try {
            //response.setHeader("content-disposition", "attachment;filename="+fileName);
            String defaultContentType = "application/json;charset=" + charset;
            String binaryContentType = "application/octet-stream";
            if (null != body) {
                if (body instanceof String) {
                    headers.put("Content-Type", defaultContentType);
                    headers.put("Content-Length", "" + ((String) body).getBytes(charset).length);
//                    headers.put("Transfer-Encoding", "chunked");
                }
                if (body instanceof InputStream) {
                    headers.put("Content-Type", binaryContentType);
                    headers.put("Content-Length", "" + ((InputStream) body).available());
                }
                if (body instanceof File) {
                    File bodyFile = (File) body;
                    headers.putAll(resolveFileTypeHeader(bodyFile.getName(), bodyFile.length()));
                    headers.put("Content-Length", "" + bodyFile.length());
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return headers;
    }

    /**
     * @param body 字符串、File、InputStream
     */
    public Response(Object body) {
        this(200, "ok", body);
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    private void init() {
        headers = new HashMap<>();
        headers.put("Content-Type", "application/json;charset=" + charset);
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Methods", "*");
        headers.put("Pragma", "no-cache");
        headers.put("Cache-Control", "no-cache,must-revalidate");

        headers.put("Expires", "Fri, 30 Oct 1998 14:19:41");//test
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    private void responseInputStream(InputStream inputStream, OutputStream os) {
        try {
            byte[] b = new byte[1024];
            int l = -1;
            while ((l = inputStream.read(b)) > -1) {
                os.write(b, 0, l);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (null != inputStream)
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            ;
        }
    }

    private void responseHeaderLine(String header, PrintWriter printWriter) {
        try {
            printWriter.write((header + "\r\n"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void responseHeaderOver(PrintWriter os) {
        try {
            os.write(("\r\n"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void responseBodyOver(PrintWriter os) {
        try {
            os.write(("0\r\n\r\n"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void response(OutputStream os) {
        PrintWriter printWriter = null;
        try {
            printWriter = new PrintWriter(new OutputStreamWriter(os, charset));
            //将响应头发送给客户端
            String responseFirstLine = "HTTP/1.1 " + getCode() + " " + getMessage() + "\r\n";
            printWriter.write(responseFirstLine);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                responseHeaderLine(entry.getKey() + ":" + entry.getValue(), printWriter);
            }
            responseHeaderOver(printWriter);
            printWriter.flush();
            //body 分情况，如果是String则转byte，如果是inputStream，则读取
            if (body != null) {
                if (body instanceof String) {
                    String bodyString = String.valueOf(body);
                    printWriter.write((bodyString));
//                    printWriter.write((Integer.toHexString(bodyString.getBytes(charset).length) + "\r\n"));
//                    printWriter.write((bodyString + "\r\n"));
//                    responseBodyOver(printWriter);
                } else if (body instanceof InputStream) {
                    InputStream inputStream = (InputStream) body;
                    responseInputStream(inputStream, os);
                } else if (body instanceof File) {
                    File bodyFile = (File) body;
                    responseInputStream(new FileInputStream(bodyFile), os);
                }

            }
            printWriter.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (null != printWriter)
                printWriter.close();
        }
    }


    public static void main(String[] args) throws Exception {

    }
}
