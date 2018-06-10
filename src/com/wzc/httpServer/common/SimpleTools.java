package com.wzc.httpServer.common;

import com.wzc.httpServer.core.Request;
import com.wzc.httpServer.core.Response;

import java.io.*;
import java.net.URL;

public class SimpleTools {
    //获取jar或者class根目录。 对于使用外置配置文件很有效。
    private static String rootClassPath = null;

    /**
     * URL必须是存在的，否则可能寻址其他目录或为空。
     * URL对应的path是有协议的file:/ 或者jar:file:/，如果需要普通的FilePath需要去掉前缀协议。
     *
     * @param classPath
     * @return
     */
    public static URL getURL(String classPath) {
        return SimpleTools.class.getResource(classPath);
    }

    public static String getURLPath(String classPath) {
        return getURLPath(getURL(classPath));
    }

    public static String getURLPath(URL url) {
        if (url == null) {
            return null;
        }
        String path = url.toExternalForm();//相当于toString，不同于getPath getFile
        if (path.startsWith("file:/")) {
            path = path.substring("file:/".length());
        }
        if (path.startsWith("jar:file:/")) {
            path = path.substring("jar:file:/".length());
        }

        return path;
    }

    /**
     * class或者jar所处的位置。
     *
     * @return
     */
    public static String getRootClassPath() {
        if (rootClassPath == null) {
            String packageName = SimpleTools.class.getPackage().getName();
            int cycleCount = packageName.split("\\.").length + 1;
            packageName = packageName.replaceAll("\\.", "/");
            String currClassPath = getURLPath(getURL("/" + packageName));

            File file = new File(currClassPath);
            for (int i = 0; i < cycleCount; i++) {
                file = file.getParentFile();
            }
            rootClassPath = file.getAbsolutePath();
        }
        return rootClassPath;
    }

    /**
     * 自定义输出
     *
     * @param request
     * @param code
     * @param statusMsg
     * @param body
     */
    public static void response(Request request, int code, String statusMsg, Object body) {
        new Response(code, statusMsg, body).response(request.getOutputStream());
    }

    public static String getUrlFileName(String url) {
        if (null == url) return null;
        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }
        int pPos = url.lastIndexOf("/");
        String fileName = "";
        if (pPos > -1) {
            fileName = url.substring(pPos + 1);
        }
        return fileName;
    }

    /**
     * 获取文件名后缀
     *
     * @param urlOrPath
     * @return
     */
    public static String getSuffix(String urlOrPath) {
        if (urlOrPath.contains("?")) {
            urlOrPath = urlOrPath.substring(0, urlOrPath.indexOf("?"));
        }
        int pPos = urlOrPath.lastIndexOf(".");
        String suffix = "";
        if (pPos > -1) {
            suffix = urlOrPath.substring(pPos + 1);
        }
        return suffix;
    }


    /**
     * 遍历字节码，并根据breaker进行分隔。
     *
     * @param source
     * @param breaker
     * @param onSplitByte
     */
    public static void splitBytes(byte[] source, byte[] breaker, OnSplitByte onSplitByte) {
        ByteArrayOutputStream bos = null;
        boolean shouldGoOn = true;
        byte[] cacheByte = new byte[breaker.length];
        int cacheLen = 0;
        for (int i = 0; i < source.length; i++) {
            if (bos == null)
                bos = new ByteArrayOutputStream();
            if (source[i] == breaker[cacheLen]) {
                cacheByte[cacheLen] = source[i];
                cacheLen++;
                if (cacheLen == cacheByte.length) {
                    //最后一个全相同，该换行了。
                    shouldGoOn = onSplitByte.onSplitByte(source, breaker, i, bos.toByteArray());
                    try {
                        bos.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    bos = null;
                    cacheLen = 0;
                    if (!shouldGoOn) break;
                }
            } else {
                if (cacheLen > 0) {
                    //把存入缓存的写入。
                    bos.write(cacheByte, 0, cacheLen);
                    i--;
                } else {
                    bos.write(source[i]);
                }
                cacheLen = 0;
            }
        }

        if (shouldGoOn && bos != null) {
            bos.write(cacheByte, 0, cacheLen);
            shouldGoOn = onSplitByte.onSplitByte(source, breaker, source.length, bos.toByteArray());
            try {
                if (null != bos)
                    bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            if (null != bos)
                bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void close(Closeable closeable) {
        try {
            if (null != closeable)
                closeable.close();
        } catch (Exception ex) {

        }
    }

    public static String readStringFromIs(InputStream is, String charset) {
        String result = null;// 返回结果字符串
        try {
            result = new String(readBytesFromIs(is),charset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static byte[] readBytesFromIs(InputStream is) {
        byte[] bytes=null;
        ByteArrayOutputStream bos = null;
        try {
            bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int l = -1;

            while ((l = is.read(b)) > 0) {
                bos.write(b, 0, l);
            }
            bos.flush();
            bytes=bos.toByteArray();
        } catch (Exception ex) {
            ex.printStackTrace();
        }finally {
            close(is);
            close(bos);
        }
        return bytes;
    }


    public interface OnSplitByte {
        boolean onSplitByte(byte[] source, byte[] breaker, int i, byte[] block);
    }

    public static void main(String[] args) throws Exception {
        //byte[] 分隔
        String charset = "utf-8";
        String sourceStr = "456--123\r\ncont--1--123";
        String breakerStr = "--123";
    }

}
