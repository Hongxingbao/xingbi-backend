package com.sing.init.controller;

import com.sing.init.common.BaseResponse;
import com.sing.init.common.ErrorCode;
import com.sing.init.common.ResultUtils;
import com.sing.init.utils.AliOssUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.UUID;

@RestController
@Slf4j
@Api(tags = "通用接口")
@RequestMapping("/common")
public class CommonController {

    @Resource
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    public BaseResponse<String> upload(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            //截取文件名后缀
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String objectName = UUID.randomUUID().toString() + extension;
            String url = aliOssUtil.upload(file.getBytes(), objectName);
            log.info("url:",url);
            return ResultUtils.success(url);
        } catch (IOException e) {
            log.error("文件上传失败:{}",e);
        }
        return ResultUtils.error(ErrorCode.UPLOAD_FAILED);
    }
}
