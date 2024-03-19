package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.properties.AliOssProperties;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.UUID;


@RestController
@RequestMapping("/admin/common")
@Slf4j
@Api(tags = "通用接口")
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        log.info("文件上传，文件名：{}", originalFilename);
        try {
            String fileName = UUID.randomUUID().toString() + originalFilename.substring(originalFilename.lastIndexOf("."));
            //调用阿里云OSS工具类进行文件上传
            String url = aliOssUtil.upload(file.getBytes(), fileName);
            log.info("文件上传完成，访问文件的url：{}", url);
            return Result.success(url);
        } catch (IOException e) {
            log.error("文件上传失败");
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
