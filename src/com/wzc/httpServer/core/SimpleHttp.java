package com.wzc.httpServer.core;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SimpleHttp {
    public static int connectTimeout = 15000;
    public static int readTimeout = 60000;
    public static String charset = "UTF-8";

    private static String doGet(String httpurl) {
        HttpURLConnection connection = null;
        String result = null;// 返回结果字符串
        try {
            connection = conn(httpurl, "GET");
            result = readStringFromConn(connection);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();// 关闭远程连接
        }
        return result;
    }

    private static String readStringFromConn(HttpURLConnection connection) {
        String result = null;// 返回结果字符串
        InputStream is = null;
        ByteArrayOutputStream bos = null;
        try {
            if (connection.getResponseCode() == 200) {
                is = connection.getInputStream();
                bos = new ByteArrayOutputStream();
                byte[] b = new byte[1024];
                int l = -1;

                while ((l = is.read(b)) > 0) {
                    bos.write(b, 0, l);
                }
                bos.flush();
                result = bos.toString(charset);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    private static HttpURLConnection conn(String httpurl, String method) throws Exception {
        HttpURLConnection connection = null;
        // 创建远程url连接对象
        URL url = new URL(httpurl);
        // 通过远程url连接对象打开一个连接，强转成httpURLConnection类
        connection = (HttpURLConnection) url.openConnection();
        // 设置连接方式：get
        connection.setRequestMethod(method);
        // 设置连接主机服务器的超时时间：15000毫秒
        connection.setConnectTimeout(connectTimeout);
        // 设置读取远程返回的数据时间：60000毫秒
        connection.setReadTimeout(readTimeout);

        String param = null;
        if ("POST".equalsIgnoreCase(method) && httpurl.indexOf("?") > -1) {
            param = httpurl.split("\\?")[1];
        }
        if (param != null && param.length() > 0) {
            // 默认值为：false，当向远程服务器传送数据/写数据时，需要设置为true
            connection.setDoOutput(true);
            // 默认值为：true，当前向远程服务读取数据时，设置为true，该参数可有可无
            connection.setDoInput(true);
            // 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式。
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            // 通过连接对象获取一个输出流
            OutputStream os = null;
            try {
                os = connection.getOutputStream();
                // 通过输出流对象将参数写出去/传输出去,它是通过字节数组写出的
                os.write(param.getBytes());
            } catch (Exception ex) {

            } finally {
                close(os);
            }
        }

        // 发送请求
        connection.connect();
        return connection;
    }

    private static void close(Closeable closeable) {
        try {
            if (null != closeable)
                closeable.close();
        } catch (Exception ex) {

        }
    }

    private static String doPost(String httpurl) {
        HttpURLConnection connection = null;
        String result = null;// 返回结果字符串
        try {
            connection = conn(httpurl, "POST");
            result = readStringFromConn(connection);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();// 关闭远程连接
        }
        return result;
    }

    public static String reqeuest(String method, String url) {
        if (method == null || method.length() <= 0)
            method = "GET";
        return "GET".equalsIgnoreCase(method) ? doGet(url) : doPost(url);
    }

    public static void main(String[] args) {
        System.out.println(SimpleHttp.reqeuest("get", "http://www.baidu.com"));
    }
}
