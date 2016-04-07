package com.air.network;

import java.security.MessageDigest;
/**
 * Created by Paul Insider on 2015/4/28.
 */
public class MD5 {
    /**
     * 把字节数组转成16进位制数
     * @param bytes
     * @return
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuffer md5str = new StringBuffer();
        //把数组每一字节换成16进制连成md5字符串
        int digital;
        for (int i = 0; i < bytes.length; i++) {
            digital = bytes[i];
            if(digital < 0) {
                digital += 256;
            }
            if(digital < 16){
                md5str.append("0");
            }
            md5str.append(Integer.toHexString(digital));
        }
        return md5str.toString();
    }

    /**
     * 把字节数组转换成md5
     * @param input
     * @return
     */
    public static byte[] bytesToMD5(byte[] input) {
        String md5str = null;
        byte[] out=new byte[16];
        char[] temp;
        try {
            //创建一个提供信息摘要算法的对象，初始化为md5算法对象
            MessageDigest md = MessageDigest.getInstance("MD5");
            //计算后获得字节数组
            byte[] buff = md.digest(input);
            //把数组每一字节换成16进制连成md5字符串
            md5str = bytesToHex(buff);
        } catch (Exception e) {
            e.printStackTrace();
        }
        temp=md5str.toCharArray();
        for (int i=0;i<32;i+=2)
        {
            if (temp[i]>='a'&&temp[i]<='f')
            {
                if (temp[i]=='a')
                    temp[i]=':';
                if (temp[i]=='b')
                    temp[i]=';';
                if (temp[i]=='c')
                    temp[i]='<';
                if (temp[i]=='d')
                    temp[i]='=';
                if (temp[i]=='e')
                    temp[i]='>';
                if (temp[i]=='f')
                    temp[i]='?';
            }
            if (temp[i+1]>='a'&&temp[i+1]<='f')
            {
                if (temp[i+1]=='a')
                    temp[i+1]=':';
                if (temp[i+1]=='b')
                    temp[i+1]=';';
                if (temp[i+1]=='c')
                    temp[i+1]='<';
                if (temp[i+1]=='d')
                    temp[i+1]='=';
                if (temp[i+1]=='e')
                    temp[i+1]='>';
                if (temp[i+1]=='f')
                    temp[i+1]='?';
            }
            out[i/2]=(byte)((temp[i]-'0')<<4|(temp[i+1]-'0'));
        }
        return out;
    }
    /**
     * 把字符串转换成md5
     * @param str
     * @return
     */
    public static byte[] strToMD5(String str) {
        byte[] input = str.getBytes();
        return bytesToMD5(input);
    }
}
