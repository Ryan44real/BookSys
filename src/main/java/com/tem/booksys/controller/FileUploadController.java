package com.tem.booksys.controller;

import com.tem.booksys.entiy.Result;
import com.tem.booksys.utils.AliOssUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
public class FileUploadController {

    @PostMapping("/load")
    public Result<String> upload(MultipartFile file) throws Exception {
        //把上传的文件转存到本地
        String originalFilename = file.getOriginalFilename();
        //保证文件的文件名是唯一的,uuid
        String filename = UUID.randomUUID().toString()+originalFilename.substring(originalFilename.lastIndexOf("."));
        String url = AliOssUtil.uploadFile(filename,file.getInputStream());
        return Result.success(url);

    }
}
