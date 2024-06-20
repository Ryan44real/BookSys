package com.tem.booksys.utils;



import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Base64;


public class Base64Img {
//    public static void main(String[] args) {
////        System.out.println(strImg);
//        String[] strImg = new String[1];
//        GenerateImage(strImg);
//    }
    public static boolean GenerateImage(String imgStr) {// 对字节数组字符串进行Base64解码并生成图片
        String imgFilePath = "C:/Users/Rain/Desktop/BYSJ";
        if (imgStr == null) // 图像数据为空
        {
            System.out.println("空的");
            return false;
        }
        Base64.Decoder decoder = Base64.getDecoder();
        try {
            // Base64解码
            byte[] bytes = decoder.decode(imgStr);
            for (int i = 0; i < bytes.length; ++i) {
                if (bytes[i] < 0) {// 调整异常数据
                    bytes[i] += 256;
                }
            }
            // 生成jpeg图片
            OutputStream out = new FileOutputStream(imgFilePath);
            out.write(bytes);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}