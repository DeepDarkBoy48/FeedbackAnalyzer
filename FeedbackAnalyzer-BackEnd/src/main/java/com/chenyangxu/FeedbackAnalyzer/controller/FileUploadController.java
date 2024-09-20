package com.chenyangxu.FeedbackAnalyzer.controller;

import com.chenyangxu.FeedbackAnalyzer.pojo.entity.Result;
import com.chenyangxu.FeedbackAnalyzer.utils.AliOssUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
public class FileUploadController {

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) throws Exception {
        String originalFilename = file.getOriginalFilename();
        //保证文件的名字是唯一的,从而防止文件覆盖
        String filename = UUID.randomUUID().toString()+originalFilename.substring(originalFilename.lastIndexOf("."));

        //把文件的内容存储到本地磁盘上
        //file.transferTo(new File("C:\\Users\\Administrator\\Desktop\\files\\"+filename));

        //阿里云oss
        String url = AliOssUtil.uploadFile(filename,file.getInputStream());
        return Result.success(url);
    }
}
