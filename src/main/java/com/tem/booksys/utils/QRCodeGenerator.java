package com.tem.booksys.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class QRCodeGenerator {

    private static final String CHARSET = "UTF-8";
    private static final int QR_CODE_SIZE = 200;

    // 生成二维码图片并保存到文件
    public static void generateQRCodeImage(String text, String filePath) throws WriterException, IOException {
        BitMatrix bitMatrix = createQRCodeBitMatrix(text);
        Path path = FileSystems.getDefault().getPath(filePath);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
    }

    // 生成二维码图片并返回BufferedImage对象
    private static BitMatrix createQRCodeBitMatrix(String text) throws WriterException {
        Map<EncodeHintType, Object> hintMap = new HashMap<>();
        hintMap.put(EncodeHintType.CHARACTER_SET, CHARSET);
        hintMap.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        return qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE, hintMap);
    }

    public static BufferedImage generateQRCodeImage(String text) throws WriterException {
        BitMatrix bitMatrix = createQRCodeBitMatrix(text);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    // 将BufferedImage转换为Base64编码的字符串
    public static String encodeImageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    public static void main(String[] args) {
        try {
            String text = "Hello Wo2ld!";
            String filePath = "QRCode.png";

            // 生成并保存二维码图片
//            generateQRCodeImage(text, filePath);
//            System.out.println("二维码已成功生成并保存到: " + filePath);

            // 生成二维码图片并获取Base64编码
            BufferedImage qrCodeImage = generateQRCodeImage(text);
            String base64Image = encodeImageToBase64(qrCodeImage);
            System.out.println("二维码的Base64编码: " + base64Image);
        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }
    }
}
