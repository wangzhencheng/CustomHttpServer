package com.wzc.httpServer.common;

import java.io.File;
import java.net.URL;

/**
 * 用来专门处理类路径的问题。ClassPathTools.init(Main.class)
 */
public class ClassPathTools {
    //获取jar或者class根目录。 对于使用外置配置文件很有效。
    private static String rootClassPath = null;
    private static Class runningMainClass = ClassPathTools.class;
    private static boolean currentIsJar = false;
    private static String jarName = null;
    private static String jarPath = null;

    /**
     * 需要制定一个Main函数入口类。或者项目中！！最先执行的几个类。
     *
     * @param runningMainClass
     */
    public static void init(Class runningMainClass) {
        jarName = null;
        currentIsJar = false;
        jarPath = null;

        ClassPathTools.runningMainClass = runningMainClass;

        String classPath = runningMainClass.getName().replaceAll("\\.", "/") + ".class";
        URL url = Thread.currentThread().getContextClassLoader().getResource(classPath);
        if (url.toExternalForm().contains(".jar")) {
            currentIsJar = true;
        }
        String currClassPath = getURLPath(url);

        String packageName = runningMainClass.getPackage().getName();
        int cycleCount = packageName.split("\\.").length + 1;

        File file = new File(currClassPath);
        for (int i = 0; i < cycleCount; i++) {
            file = file.getParentFile();
        }
        rootClassPath = file.getAbsolutePath();

        if (currentIsJar) {
            String[] names = rootClassPath.split("[\\\\/]");
            jarName = names[names.length - 1];
            if (jarName.endsWith("!"))
                jarName = jarName.substring(0, jarName.length() - 1);
            jarPath = new File(rootClassPath).getParent();
        }
    }

    public static String getJarName() {
        return jarName;
    }

    /**
     * 当前是否打包到了jar之中
     *
     * @return
     */
    public static boolean currentIsJar() {
        return currentIsJar;
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
        return rootClassPath;
    }

    public static String getJarPath() {
        return jarPath;
    }

    /**
     * 获取当前应用的URL资源
     *
     * @param name
     * @return
     */
    public static URL getURL(String name) {
        return runningMainClass.getResource(name);
    }

    /**
     * 获取程序目录外的文件路径
     *
     * @param path
     * @return
     */
    public static String getOutClassPath(String path) {
        if (path == null || path.isEmpty())
            return new File(ClassPathTools.getRootClassPath()).getParent();
        return new File(new File(ClassPathTools.getRootClassPath()).getParent(), path).getAbsolutePath();
    }

    public static void main(String[] args) throws Exception {
        //ClassPathTools.init(Main.class);

        ClassPathTools.init(ClassPathTools.class);

        System.out.println(ClassPathTools.currentIsJar);
        System.out.println(ClassPathTools.getRootClassPath());
    }

}
