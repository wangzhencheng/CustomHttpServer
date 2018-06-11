package com.wzc.httpServer.core;

import com.wzc.httpServer.common.SimpleTools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * 用于快速创建目录和输出配置；
 */
public class PackTools {

    /**
     * 用于快速输出目录和配置
     *
     * @param outDir
     * @param browserExePath
     * @param port
     */
    public static void pack(String outDir, String browserExePath, String port) {

        //获取对应的run.vbs  stop.vbs 替换对应的占位符： 浏览器路径  以及端口号port
        File dirFile = new File(outDir);
        try {
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            String readmeTxt = getContent("readme.txt");
            readmeTxt += "\r\n当前地址 http://localhost:" + port+"/index.html";
            writeTo(readmeTxt.getBytes("utf-8"), outDir, "readme.txt");

            String runVbs = getContent("run.vbs");
            runVbs = String.format(runVbs, port, browserExePath);
            writeTo(runVbs.getBytes("utf-8"), outDir, "run.vbs");

            copyByFileName(outDir, "SimpleHttp.class");

            String stopVbs = getContent("stop.vbs");
            stopVbs = String.format(stopVbs, port);
            writeTo(stopVbs.getBytes("utf-8"), outDir, "stop.vbs");

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private static String getContent(String fileName) {
        InputStream is = PackTools.class.getResourceAsStream("/" + fileName);
        String rs = SimpleTools.readStringFromIs(is, "utf-8");
        return rs;
    }

    private static byte[] getByteContent(String fileName) {
        InputStream is = PackTools.class.getResourceAsStream("/" + fileName);
        return SimpleTools.readBytesFromIs(is);
    }

    private static void writeTo(byte[] b, String dirFile, String fileName) throws Exception {
        File dst = new File(dirFile, fileName);
        if (!dst.getParentFile().exists())
            dst.getParentFile().mkdirs();
        if (!dst.exists()) {
            dst.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(dst);
        fos.write(b);
        fos.flush();
        SimpleTools.close(fos);
    }

    private static void copyByFileName(String dirFile, String fileName) throws Exception {
        writeTo(getByteContent(fileName), dirFile, fileName);
    }


    //用于快速打包
    public static void main(String[] args) throws Exception {
        //指定一个目录，创建
        //指定端口号  浏览器exe路径
        //将readme.txt  run.vbs SimpleHttp.class stop.vbs都copy过去
        //替换run.vbs stop.vbs中的端口和浏览器路径
        String dir = "E:\\work_source\\javaFx\\SimplestClient\\test";
        String browserPath = "D:\\wzc_pro\\firefox\\firefox.exe";
        String port = "18180";
        pack(dir, browserPath, port);

    }
}
