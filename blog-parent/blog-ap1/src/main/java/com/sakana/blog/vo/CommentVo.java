package com.sakana.blog.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

@Data
public class CommentVo {
    //// 分布式id 比较长，传到前端 会有精度损失，必须转为string类型 进行传输，就不会有问题了
    //@JsonSerialize(using= ToStringSerializer.class) //防止前端精度损失，把id转化为string
    private String id;

    private UserVo author;

    private String content;

    //子评论信息
    private List<CommentVo> childrens;

    private String createDate;

    private Integer level;

    //给谁评论
    private UserVo toUser;

}
