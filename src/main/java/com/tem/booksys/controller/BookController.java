package com.tem.booksys.controller;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.tem.booksys.entiy.Article;
import com.tem.booksys.entiy.PageBean;
import com.tem.booksys.entiy.Result;
import com.tem.booksys.mapper.BookMapper;
import com.tem.booksys.service.BookService;
import com.tem.booksys.utils.*;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;

import static java.lang.Thread.sleep;

@RestController
@RequestMapping("/article")
public class BookController {

    @Autowired
    private BookMapper bookMapper;
    private JavaCallPython javaCallPython;
    private Base64Img base64Img;
    @Autowired
    private BookService bookService;
    @PostMapping
    public Result add(@RequestBody Article article){
        System.out.println(article);
        bookService.add(article);

        return Result.success();
    }

    @GetMapping("/getBookList")
    public Result<PageBean<Article>> list(Integer pageNum,
                                          Integer pageSize,
                                          @RequestParam(required = false) String categoryId,
                                          @RequestParam(required = false) String state,
                                          @RequestParam(required = false) String title){
        PageBean<Article> pb = bookService.list(pageNum,pageSize,categoryId,title,state);

        return Result.success(pb);
    }
    @GetMapping("/detail")
    public Result<Article> detail(@RequestParam String id){
//        System.out.println("detail"+id);
        Article article = bookService.findById(id);
//        System.out.println(article);
        return Result.success(article);
    }

    @PutMapping
    public Result update(@RequestBody Article article){
        bookService.update(article);
        return Result.success();
    }

    @DeleteMapping
    public Result delete(@RequestParam String id){
        bookService.delete(id);
        return Result.success();
    }

    @GetMapping("/getBookContent")
    public Result<String> getBookContent(String bookName,String bookNum) throws JSONException, IOException {
        String res = ChineseGPT.GptResult(bookName,bookNum);
//        System.out.println(res);
        return Result.success(res);
    }
    @GetMapping("/getBookBarcode")
    public Result getBookBarcode(String base64) throws JSONException, IOException, NotFoundException, InterruptedException {
        String imgFilePath = "C:/Users/Rain/Desktop/BYSJ/"+base64;
        System.out.println(imgFilePath);
        sleep(1000);

        File file = new File(imgFilePath); // 替换为你的条形码图片路径
            BufferedImage bufferedImage = ImageIO.read(file);
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            com.google.zxing.Result result = new MultiFormatReader().decode(bitmap);
//            System.out.println("Barcode format : " + result.getBarcodeFormat());
//            System.out.println("Barcode content : " + result.getText());
//            System.out.println(ioe.toString());
//            System.out.println("No barcode found. Exception : " + e.toString());

        return Result.success();
    }

    @GetMapping("/getBookBarcodeByPy")
    public Result getBookBarcodeByPy(){
        System.out.println("getBookBarcodeByPy");
        String res =  JavaCallPython.barcode();
        System.out.println(res);
        return Result.success(res);
    }

    @GetMapping("/buildBookNumService")
    public Result buildBookNum(String isbn) throws NoSuchAlgorithmException {
//        System.out.println("学习先");
        Integer res = bookService.checkIsbn(isbn);
        if (res>1){
            //数据库中已经存在该书
            isbn=isbn+res;
        }
        Random random = new Random();
        Integer randomNumber = 100000 + random.nextInt(900000);
        if (isbn == null || !isbn.matches("\\d+")) {
            throw new IllegalArgumentException("Input must be a numeric string.");
        }

        // 使用SHA-256生成哈希值
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(isbn.getBytes());

        // 将字节数组转换成一个大整数
        BigInteger number = new BigInteger(1, hash);

        // 取模1000000以获取一个6位数字
        BigInteger mod = new BigInteger("1000000");
        BigInteger encryptedNumber = number.mod(mod);
        Integer result = Integer.valueOf(String.format("%06d", encryptedNumber));
//        System.out.println(isbn);
        return Result.success(result);
    }

    @GetMapping("/getBookNumService")
    public Result<Integer> getBookNum(){
        Integer res = bookService.getBookNum();
//        System.out.println(res);
        return Result.success(res);
    }

    @GetMapping("/getBookNumUseService")
    public Result<Integer> getBookNumUse(){
        Integer res = bookMapper.getBookNumUse();
        System.out.println(res);
        return Result.success(res);
    }
    @Autowired
    private FileUploadController fileUploadController;

    @GetMapping("/buildQrCode")
    public Result buildQrCode(String isbn) throws Exception {
        System.out.println(isbn);
//        QRCodeGenerator.generateQRCodeImage(isbn);
        QRCodeGenerator.generateQRCodeImage(isbn, isbn+".png");
        File file = new File(isbn+".png");
        System.out.println(file);
        InputStream input = new FileInputStream(file);
        String filename = UUID.randomUUID().toString()+"QRCode.png";
        String url = AliOssUtil.uploadFile(filename,input);
        return Result.success(url);
    }
    public static FileItem creatFileItem(File file) {

        DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory(16, null);
        FileItem fileItem = diskFileItemFactory.createItem("textField", "application/zip", true, file.getName());

        int bytesRead = 0;
        byte[] buffer = new byte[8192];

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            OutputStream outputStream = fileItem.getOutputStream();
            while ((bytesRead = fileInputStream.read(buffer, 0, 8192)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            Files.copy(file.toPath(), outputStream);
            outputStream.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileItem;
    }
}
