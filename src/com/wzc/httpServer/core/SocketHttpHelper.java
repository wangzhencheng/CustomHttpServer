package com.wzc.httpServer.core;

import com.wzc.httpServer.common.ClassPathTools;
import com.wzc.httpServer.common.SimpleTools;

import java.io.*;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 简易简单粗暴的本地HttpServer，用法如下：
 * new SocketHttpHelper()
 * .addHandlerByClass(HomeHandler.class)
 * .setAutoExit(new ExitHandler(new ArgsMap(args).get("password","admin")), null)
 * .startServer(8080);
 * 可以使用main函数的args提供参数转ArgsMap动态设置port和退出密码等参数。
 */
public class SocketHttpHelper {
    boolean shouldGoOn = true;
    boolean isGoOn = false;
    int threadPoolSize = 2;
    ServerSocket serverSocket = null;
    //注册的，需要处理的handler。
    List<UrlHandler> registerHanlder = Collections.synchronizedList(new ArrayList<UrlHandler>());

    public static List<SocketHttpHelper> socketHttps = new ArrayList<>();

    public SocketHttpHelper() {
        this(true);
    }

    /**
     * @param autoAddStaticResource
     */
    public SocketHttpHelper(boolean autoAddStaticResource) {
        SocketHttpHelper.socketHttps.add(this);
        //静态资源处理，先查找jar包目录资源，然后查找jar包内部资源. 自己稍后注册不同的目录也可
        if (autoAddStaticResource) {
            //外部目录
            registerHanlder.add(new StaticResourceHandler(ClassPathTools.getOutClassPath(null)));
            //内部目录
            registerHanlder.add(new InnerStaticResourceHanlder());
        }

    }

    public SocketHttpHelper setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
        return this;
    }

    public static void stopAll() {
        for (SocketHttpHelper httpHelper : socketHttps) {
            httpHelper.stop();
        }
        socketHttps.clear();
    }

    public void stop() {
        shouldGoOn = false;
        isGoOn = false;
        try {
            if (null != serverSocket && !serverSocket.isClosed())
                serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverSocket = null;
    }

    /**
     * 使用配置文件的port端口，如果无配置文件，或者未指定，则使用8081
     */
    public void start() {
        start(configPort());
    }

    /**
     * 使用main函数的设置进行启动。两个参数：password port
     *
     * @param args
     */
    public void startWithMainArgs(String[] args) {
        ArgsMap argsMap = new ArgsMap(args);
        String pwd = argsMap.get("password", "admin");
        int port = argsMap.getInt("port", 18082);
        this.setAutoExit(new ExitHandler(pwd), null)
                .start(port);
    }

    /**
     * 指定端口号启动。
     *
     * @param port
     */
    public void start(int port) {
        if (isGoOn) return;
        isGoOn = true;
        shouldGoOn = true;
        ExecutorService es = Executors.newFixedThreadPool(threadPoolSize);

        try {
            serverSocket = new ServerSocket(port);
            //该句话表示服务已启动，同时和SpringBoot项目保持一致，可以复用之前的JavaFxApp，嘎嘎
            System.out.println("JVM running for custom http server! port:" + port);
            while (shouldGoOn) {
                Socket socket = serverSocket.accept();
//                socket.setSoTimeout(5000);
                socket.setKeepAlive(false);
                socket.setTcpNoDelay(true);
                service(es, socket);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            stop();
            es.shutdown();
        }
    }

    /**
     * 添加处理请求的Hanlder
     * StaticResourceHandler是静态资源处理，可以添加多个。
     *
     * @param handler
     */
    public SocketHttpHelper addHandler(UrlHandler handler) {
        registerHanlder.add(0, handler);
        return this;
    }

    /**
     * 直接根据class添加对应的url,方法上需要有@Handler注解。
     *
     * @return
     */
    public <T extends Object> SocketHttpHelper addHandlerByClass(Class mClass, HandlerAnnotationHandler.OnException onException) {
        try {
            return addHandlerByObject(mClass.newInstance(), onException);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return this;
    }

    /**
     * 直接根据Object添加对应的url,方法上需要有@Handler注解。
     *
     * @return
     */
    public <T extends Object> SocketHttpHelper addHandlerByObject(T object, HandlerAnnotationHandler.OnException onException) {
        try {
            Class mClass = object.getClass();
            //解析所有的方法，并获取方法上的注解
            Method[] methods = mClass.getDeclaredMethods();
            for (Method method : methods) {
                method.setAccessible(true);
                Handler handler = method.getAnnotation(Handler.class);
                if (handler == null)
                    continue;
                HandlerAnnotationHandler handlerAnnotationHandler = new HandlerAnnotationHandler(
                        object, method, handler.value(), handler.method()
                );
                handlerAnnotationHandler.setOnException(onException);
                addHandler(handlerAnnotationHandler);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return this;
    }

    /**
     * 这里就是剥离出来的请求，可以实现业务处理。
     *
     * @param request
     * @return
     */
    private synchronized Response onRequest(Request request) {
        //需要自己实现对应的url和方法的映射。
        for (UrlHandler handler : registerHanlder) {
            if (handler.matchRequest(request)) {
                try {
                    return handler.onRequest(request);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return new Response(500, "ERROR", String.valueOf(ex));
                }
            }
        }
        //没有handler。404
        return new Response(404, "NO", "NOT FOUND");
    }

    public interface UrlHandler {
        boolean matchRequest(Request request);

        Response onRequest(Request request);
    }


    private void service(ExecutorService es, Socket socket) {
        es.submit(new Runnable() {
            @Override
            public synchronized void run() {
                InputStream is = null;
                OutputStream os = null;
                try {
                    //获取HTTP请求头
                    is = socket.getInputStream();
//                    System.out.println("is len:" + is.available());
                    os = socket.getOutputStream();
                    Response response = onRequest(resolveRequest(is, os));
                    responseSocket(os, response);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeClosable(is);
                    closeClosable(os);
                    closeClosable(socket);
                }
            }
        });
    }

    private void closeClosable(Closeable closeable) {
        if (null != closeable)
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private Request resolveRequest(InputStream is, OutputStream os) throws IOException {
        Request request = new Request();
        request.headers = new HashMap<>();
        request.params = new HashMap<>();

        ByteArrayOutputStream bosHeader = new ByteArrayOutputStream();
        ByteArrayOutputStream bosBody = new ByteArrayOutputStream();

        HashMap<String, String> params = null;

        int rChar = '\r';
        int nChar = '\n';
        try {
            String line = null;
            boolean resolvedHeader = false;
            int nextByte = -1;
            int char1 = -1, char2 = -1, char3 = -1, char4 = -1;
            while ((nextByte = is.read()) != -1) {
                bosHeader.write(nextByte);
                char1 = char2;
                char2 = char3;
                char3 = char4;
                char4 = nextByte;

                if (char4 == nChar && char3 == rChar && char2 == nChar && char1 == rChar) {
                    resolvedHeader = true;
                    try {
                        resolveHeader(bosHeader, request);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        closeClosable(bosHeader);
                    }
                    break;
                }
            }

            //重新读取body
            if (nextByte == -1) {//-1 表示没有body了。
                if (!resolvedHeader) {
                    resolvedHeader = true;
                    try {
                        resolveHeader(bosHeader, request);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        closeClosable(bosHeader);
                    }

                }
            }

            if ("OPTIONS".equalsIgnoreCase(request.method)) {
                request.method = request.headers.get("access-control-request-method");
                if (request.method != null) request.method = request.method.trim();
            }

            if (nextByte != -1) {//可能还有body的流
                if ("POST".equalsIgnoreCase(request.method)
                        || "PUT".equalsIgnoreCase(request.method)
                        || "PATCH".equalsIgnoreCase(request.method)) {
                    int contentLen = 0;
                    try {
                        String contentLenStr = request.headers.get("content-length");
                        if (null != contentLenStr) {
                            contentLen = Integer.parseInt(contentLenStr.trim());
                        }

                        if (contentLen > 0) {
                            //两种情况 普通传值 和 上传文件
                            request.isUploadFile = request.getHeaders().containsKey("content-type")
                                    && request.getHeaders().get("content-type").contains("multipart/form-data");
                            if (request.isUploadFile) {
                                //如何解析文件，都塞到内存？ 暂时支持小文件吧，写内存中
                                String boundary = resolveBoundary(request.getHeaders().get("content-type"));
                                if (boundary != null && boundary.length() > 0) {
                                    byte[] allFileAndParamsBuffer = new byte[contentLen];
                                    is.read(allFileAndParamsBuffer);
                                    //需要根据分隔符进行分隔；
                                    SimpleTools.splitBytes(allFileAndParamsBuffer, ("--" + boundary).getBytes("utf-8"), new SimpleTools.OnSplitByte() {
                                        @Override
                                        public boolean onSplitByte(byte[] source, byte[] breaker, int i, byte[] block) {
                                            //获取到的进行处理
                                            if (i >= source.length) return false;
                                            if (block.length == 0) return true;

                                            //第一行
                                            FileParam fileParam = FileParam.resolveBlockData(block, "utf-8", breaker.length);
                                            if (fileParam.isFile()) {
                                                if (request.getFileParams() == null)
                                                    request.setFileParams(new HashMap<>());
                                                List<FileParam> fileList = request.getFileParams().get(fileParam.getName());
                                                if (fileList == null) {
                                                    fileList = new ArrayList<>();
                                                    request.getFileParams().put(fileParam.getName(), fileList);
                                                }
                                                fileList.add(fileParam);
                                            } else {
                                                List<String> param = request.getParams().get(fileParam.getName());
                                                if (param == null) {
                                                    param = new ArrayList<>();
                                                    request.getParams().put(fileParam.getName(), param);
                                                }
                                                param.add(fileParam.getStringValue());
                                            }

                                            return true;
                                        }
                                    });

                                }

                            } else {
                                int bufferLen = 2048;
                                if (contentLen % bufferLen == bufferLen) {//如果整除，可能会出现无法正好读完还在等待的情况。
                                    bufferLen += 1;
                                }
                                byte[] buffer = new byte[bufferLen];
                                int l = -1;
                                while ((l = is.read(buffer)) != -1) {
                                    bosBody.write(buffer, 0, l);
                                    if (l < buffer.length) {
                                        break;
                                    }
                                }
                                bosBody.flush();
                                request.body = bosBody.toString("utf-8");
                            }

                        }

                    } catch (Exception ex) {

                    } finally {
                        closeClosable(bosBody);
                    }
                }
            }
            resolveParams(request.params, request.uri, request.headers, request.body);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            closeClosable(bosHeader);
        }

        return request;
    }

    /**
     * 获取分隔符
     *
     * @param contentType
     * @return
     */
    private String resolveBoundary(String contentType) {
        if (contentType == null) {
            return null;
        }
        String blocks[] = contentType.split(";");
        for (String block : blocks) {
            if (block.contains("=") && block.trim().startsWith("boundary")) {
                String boundarKV[] = block.trim().split("=");
                if (boundarKV.length == 2) {
                    return boundarKV[1].trim();
                }
                break;
            }
        }
        return null;
    }

    private void resolveHeader(ByteArrayOutputStream bosHeader, Request request) {
        if (request.headers == null)
            request.headers = new HashMap<>();
        try {

//            System.out.println("got header over!");
            //header 结束
            bosHeader.flush();
            String line = null;
            String headerString = bosHeader.toString("utf-8");
            String[] headerLines = headerString.split("\r\n");
            for (int i = 0; i < headerLines.length; i++) {
                line = headerLines[i];
                if (line.trim().length() <= 0) continue;
                if (i == 0) {
                    String[] requestInfo = line.split(" ");
                    if (requestInfo != null && requestInfo.length == 3) {
                        request.method = requestInfo[0];
                        request.uri = requestInfo[1];
                        request.protol = requestInfo[2];
                    }
                } else {
                    String headerArr[] = line.split(":");
                    if (headerArr != null && headerArr.length == 2) {
                        request.headers.put(headerArr[0].toLowerCase(), headerArr[1]);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            closeClosable(bosHeader);
        }
    }

    private void responseSocket(OutputStream os, Response response) {
        if (response != null) {
            response.response(os);
        }
    }


    private static void resolveParams(HashMap<String, List<String>> params, String uri, HashMap<String, String> headers, String bodyStr) {
        if (uri != null) {
            int uriParamsIndex = uri.indexOf("?");

            if (uriParamsIndex > -1) {
                String paramsStr = uri.substring(uriParamsIndex + 1);
                resolveUrlEncodeParams(params, paramsStr);
            }
        }

        String contentType = headers.get("content-type");
        if (contentType != null && contentType.contains("application/x-www-form-urlencoded")) {
            resolveUrlEncodeParams(params, bodyStr);
        }
    }

    private static void resolveUrlEncodeParams(HashMap<String, List<String>> params, String paramsStr) {
        if (params == null || paramsStr == null) return;
        try {
            paramsStr = URLDecoder.decode(paramsStr, "utf-8");
            String[] kvGroup = paramsStr.split("&");
            for (String kv : kvGroup) {
                String[] kvArr = kv.split("=");
                if (kvArr.length == 2) {
                    List<String> paramList = params.get(kvArr[0]);
                    if (paramList == null) {
                        paramList = new ArrayList<>();
                        params.put(kvArr[0], paramList);
                    }
                    paramList.add(kvArr[1]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int configPort() {
        return configPort("config.properties", "port");
    }

    public static int configPort(String propertiesFileName, String key) {
        if (propertiesFileName == null) propertiesFileName = "config.properties";
        if (key == null) key = "port";
        File file = new File(ClassPathTools.getOutClassPath(null), propertiesFileName);
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(file));
            String portValue = properties.getProperty(key);
            if (null != portValue) {
                int port = Integer.parseInt(portValue);
                if (port > 0) return port;
            }
        } catch (Exception e) {
            //            e.printStackTrace();
            System.out.println("no config.properties found ,use default port 8081!");
        }
        return 8081;
    }

    /**
     * 设置自动退出。 url： http://localhost:port/exit?password=yourpassword
     *
     * @param exitHandler
     * @param onException
     * @return
     */
    private SocketHttpHelper setAutoExit(ExitHandler exitHandler, HandlerAnnotationHandler.OnException onException) {
        this.addHandlerByObject(exitHandler, onException);
        return this;
    }


    /**
     * this is sample
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        //添加自定义handler并启动 可以指定端口号
        ClassPathTools.init(SocketHttpHelper.class);

        new SocketHttpHelper()
                .addHandlerByClass(HomeHandler.class, null)
                .startWithMainArgs(args);
    }

}
