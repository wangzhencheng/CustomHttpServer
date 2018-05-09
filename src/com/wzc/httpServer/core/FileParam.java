package com.wzc.httpServer.core;

import com.wzc.httpServer.common.SimpleTools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class FileParam {
    private String contentDisposition;
    private String name;
    private String fileName;
    private String contentType;
    private byte[] data;
    private String stringValue;

    public boolean isFile() {//判断是否是文件。
        return null != contentType && contentType.trim().length() > 0;
    }

    public boolean isEmpty() {
        return data != null && data.length > 0;
    }

    public String getFileSuffix() {
        return null != fileName && fileName.indexOf(".") > 0 ? fileName.substring(fileName.lastIndexOf(".")) : null;
    }

    public boolean transferFile(String path) {
        File dst = new File(path);
        if (!dst.getParentFile().exists()) {
            dst.getParentFile().mkdirs();
        }
        FileOutputStream fos = null;
        try {
            dst.createNewFile();
            fos = new FileOutputStream(dst);
            fos.write(this.getData());
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != fos) try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dst.exists();
    }

    private class Pg {
        int line=0;
        boolean setFistLine = false;
        boolean setSecondLine = false;
    }

    /**
     * 解析一个块字符串为文件
     *
     * @param fileAllData
     */
    public static FileParam resolveBlockData(byte[] fileAllData, String charset, int boundaryLen) {
        FileParam fileParam = new FileParam();

        try {
            Pg pg = fileParam.new Pg();

            SimpleTools.splitBytes(fileAllData, "\r\n".getBytes(charset), new SimpleTools.OnSplitByte() {
                @Override
                public boolean onSplitByte(byte[] source, byte[] breaker, int i, byte[] block) {
                    pg.line++;
                    try {
                        if (pg.line==2) {
                            //处理第一行
                            pg.setFistLine = true;
                            String firstLine = new String(block, charset);
                            if (firstLine.toLowerCase().trim().startsWith("content-disposition")) {
                                String contentDisposition = firstLine.substring(firstLine.indexOf(":") + 1).trim();
                                fileParam.setContentDisposition(contentDisposition);

                                String[] kvs = contentDisposition.split(";");
                                for (String kvStr : kvs) {
                                    if (!kvStr.contains("=")) {
                                        continue;
                                    }
                                    String[] kvsArr = kvStr.split("=");
                                    if (kvsArr != null && kvsArr.length == 2) {
                                        String name = kvsArr[0].trim();
                                        String value = kvsArr[1].trim();
                                        if (value.startsWith("\"") && value.endsWith("\"")) {
                                            value = value.substring(1, value.length() - 1);
                                        }
                                        if ("name".equalsIgnoreCase(name)) {
                                            fileParam.name = value;
                                        }
                                        if ("filename".equalsIgnoreCase(name)) {
                                            fileParam.fileName = value;
                                        }
                                    }
                                }
                            }
                            return true;
                        }

                        if (pg.line==3) {
                            pg.setSecondLine = true;
                            String secondLine = new String(block, charset);
                            if (secondLine.trim().length() <= 0) {//普通参数
                                String leftData =new String(Arrays.copyOfRange(source, i+1 , source.length -2),charset);
                                fileParam.setStringValue(leftData);
                                return false;
                            } else {
                                //Content-Type
                                String contentType = secondLine.substring(secondLine.indexOf(":") + 1);
                                fileParam.setContentType(contentType.trim());
                                //剩下的就是全部数据
                                try {
                                    fileParam.setData(Arrays.copyOfRange(source, i +3, source.length -2));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return true;
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return fileParam;
    }

    public String getContentDisposition() {
        return contentDisposition;
    }

    public void setContentDisposition(String contentDisposition) {
        this.contentDisposition = contentDisposition;

    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }


    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
