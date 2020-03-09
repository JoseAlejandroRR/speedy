package com.josealejandrorr.speedy.web;


import com.josealejandrorr.speedy.utils.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

    public class Analysis {
    static final int NONE = 0;
    static final int DATAHEADER = 1;
    static final int FILEDATA = 2;
    static final int FIELDDATA = 3;
    static final int MXA_SEGSIZE = 1000 * 1024 * 10; //最大文件长度

    public static Map<String, Object> parse(InputStream ins, String contentType, int totalLength)
            throws IOException {

        FileInfo fileInfo =new FileInfo();
        String fieldname = ""; // 表单域的名称
        String fieldvalue = ""; // 表单域的值
        String filename = ""; // 文件名
        String boundary = ""; // 分界符
        String lastboundary = ""; // 结束符
        String filefieldname = ""; // 文件表单域名
        Map<String, Object> formfields = new HashMap<String, Object>();
        int filesize = 0; // 文件长度

        List<Integer> filesSize = new ArrayList<>();
        List<String> filesName = new ArrayList<>();
        List<String> filesFieldName = new ArrayList<>();
        List<byte[]> filesBytes = new ArrayList<>();

        int pos = contentType.indexOf("boundary=");

        if (pos != -1) { // 取得分界符和结束符
            pos += "boundary=".length();
            boundary = "--" + contentType.substring(pos);
            lastboundary = boundary + "--";
        }
        int state = NONE;
        // 得到数据输入流reqbuf
        DataInputStream in = new DataInputStream(ins);
        // 将请求消息的实体送到b变量中
        int totalBytes = totalLength;
        String message = "";
        if (totalBytes > MXA_SEGSIZE) {// 每批大于10m时
            message = "Each batch of data can not be larger than " + MXA_SEGSIZE / (1000 * 1024)
                    + "M";
            return null;
        }
        byte[] b = new byte[totalBytes];
        in.readFully(b);
        in.close();
        String reqContent = new String(b, "UTF-8");//
        BufferedReader reqbuf = new BufferedReader(new StringReader(reqContent));

        boolean flag = true;
        int i = 0;
        while (flag == true) {
            String s = reqbuf.readLine();
            if ((s == null) || (s.equals(lastboundary)))
                break;

            switch (state) {
                case NONE:
                    if (s.startsWith(boundary)) {
                        state = DATAHEADER;
                        i += 1;
                    }
                    break;
                case DATAHEADER:
                    pos = s.indexOf("filename=");
                    if (pos == -1) { // 将表单域的名字解析出来
                        pos = s.indexOf("name=");
                        pos += "name=".length() + 1;
                        s = s.substring(pos);
                        int l = s.length();
                        s = s.substring(0, l - 1);
                        fieldname = s;
                        Logger.getLogger().debug("Primero ", fieldname);
                        state = FIELDDATA;
                    } else {
                        String temp = s;
                        // 将文件表单参数名解析出来
                        pos = s.indexOf("name=");
                        pos += "name=".length() + 1;
                        s = s.substring(pos);
                        int pos1 = s.indexOf("\";");
                        filefieldname = s.substring(0, pos1);
                        filesFieldName.add(filefieldname);

                        // 将文件名解析出来
                        pos = s.indexOf("filename=");
                        pos += "filename=".length() + 1;
                        s = s.substring(pos);
                        int l = s.length();
                        s = s.substring(0, l - 1);// 去掉最后那个引号”
                        pos = s.lastIndexOf("\\");
                        s = s.substring(pos + 1);
                        filename = s;
                        filesName.add(s);
                        // 从字节数组中取出文件数组
                        pos = byteIndexOf(b, temp, 0);
                        b = subBytes(b, pos + temp.getBytes().length + 2, b.length);// 去掉前面的部分
                        int n = 0;
                        /**
                         * 过滤boundary下形如 Content-Disposition: form-data; name="bin"; filename="12.pdf" Content-Type:
                         * application/octet-stream Content-Transfer-Encoding: binary 的字符串
                         */

                        while ((s = reqbuf.readLine()) != null) {
                            if (n == 1) {
                                break;
                            }
                            if (s.equals("")) {
                                n++;
                            }

                            b = subBytes(b, s.getBytes().length + 2, b.length);
                        }

                        pos = byteIndexOf(b, boundary, 0);
                        if (pos != -1) {
                            b = subBytes(b, 0, pos - 1);
                        }

                        filesBytes.add(b);

                        filesize = b.length - 1;
                        filesSize.add(filesize);
                        state = FILEDATA;
                        Logger.getLogger().debug("TERCERO ");
                    }
                    break;
                case FIELDDATA:
                    s = reqbuf.readLine();
                    fieldvalue = s;
                    formfields.put(fieldname, fieldvalue);
                    state = NONE;
                    break;
                case FILEDATA:
                    while ((!s.startsWith(boundary)) && (!s.startsWith(lastboundary))) {
                        s = reqbuf.readLine();
                        if (s.startsWith(boundary)) {
                            state = DATAHEADER;
                            break;
                        }
                    }
                    break;
            }
        }
        Logger.getLogger().debug("SEGUNDO3 ");
        fileInfo.setFieldname(filefieldname);
        fileInfo.setBytes(b);
        fileInfo.setFilename(filename);
        fileInfo.setLength(filesize);
        for (int j = 0; j < filesBytes.size(); j++)
        {
            FileInfo fi = new FileInfo();
            fi.setFieldname(filesFieldName.get(j));
            fi.setBytes(filesBytes.get(j));
            fi.setFilename(filesName.get(j));
            fi.setLength(filesSize.get(j));
            formfields.put(filesFieldName.get(j), fi);
        }

        //formfields.put(filefieldname, fileInfo);
        return formfields;

    }

    // 字节数组中的INDEXOF函数，与STRING类中的INDEXOF类似
    public static int byteIndexOf(byte[] b, String s, int start) {
        return byteIndexOf(b, s.getBytes(), start);
    }

    // 字节数组中的INDEXOF函数，与STRING类中的INDEXOF类似
    public static int byteIndexOf(byte[] b, byte[] s, int start) {
        int i;
        if (s.length == 0) {
            return 0;
        }
        int max = b.length - s.length;
        if (max < 0)
            return -1;
        if (start > max)
            return -1;
        if (start < 0)
            start = 0;
        search: for (i = start; i <= max; i++) {
            if (b[i] == s[0]) {
                int k = 1;
                while (k < s.length) {
                    if (b[k + i] != s[k]) {
                        continue search;
                    }
                    k++;
                }
                return i;
            }
        }
        return -1;
    }

    // 用于从一个字节数组中提取一个字节数组
    public static byte[] subBytes(byte[] b, int from, int end) {
        byte[] result = new byte[end - from];
        System.arraycopy(b, from, result, 0, end - from);
        return result;
    }

    // 用于从一个字节数组中提取一个字符串
    public static String subBytesString(byte[] b, int from, int end) {
        return new String(subBytes(b, from, end));
    }

}