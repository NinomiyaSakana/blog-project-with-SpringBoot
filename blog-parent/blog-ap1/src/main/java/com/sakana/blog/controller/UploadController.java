package com.sakana.blog.controller;

import com.sakana.blog.utils.QiniuUtils;
import com.sakana.blog.vo.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("upload")
public class UploadController {
    @Autowired
    private QiniuUtils qiniuUtils;

    @PostMapping
    public Result uploda(@RequestParam("image") MultipartFile file) {
        //原始文件名称
        String originalFilename = file.getOriginalFilename();
        //要上传随机的文件名称，保持唯一性
        //不能用原始的文件名称
        //substringAfterLast得到png的后缀
        String fileName = UUID.randomUUID().toString() + "." + StringUtils.substringAfterLast(originalFilename, ".");
        //上传文件 上传到七牛云 云服务器 速度快 把图片发放到离用户最近的服务器上
        //可以降低自身应用服务器的带宽消耗
        boolean upload = qiniuUtils.upload(file, fileName);
        if(upload){
            return Result.success((QiniuUtils.url+fileName));
        }
        return Result.fail(20001,"上传失败");

    }

}
