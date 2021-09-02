package com.sakana.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sakana.blog.dao.mapper.CommentMapper;
import com.sakana.blog.dao.pojo.Comment;
import com.sakana.blog.dao.pojo.SysUser;
import com.sakana.blog.service.CommentsService;
import com.sakana.blog.service.SysUserService;
import com.sakana.blog.utils.UserThreadLocal;
import com.sakana.blog.vo.CommentVo;
import com.sakana.blog.vo.Result;
import com.sakana.blog.vo.UserVo;
import com.sakana.blog.vo.params.CommentParam;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentsServiceImpl implements CommentsService {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private SysUserService sysUserService;

    @Override
    public Result commentsByArticleId(Long id) {
        /**
         * 1、根据文章id查询评论列表，从comments表中查询
         * 2、根据作者的id查询作者信息
         * 3、判断如果level==1 要去查询他有没有子评论
         * 4、如果有，根据评论id进行查询（parent_id）
         */
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        //根据文章id查询
        queryWrapper.eq(Comment::getArticleId, id);
        //首先要level是1
        queryWrapper.eq(Comment::getLevel, 1);
        List<Comment> comments = commentMapper.selectList(queryWrapper);
        //需要copy一下
        //
        List<CommentVo> commentVoList = copyList(comments);
        return Result.success(commentVoList);
    }

    @Override
    public Result comment(CommentParam commentParam) {
        SysUser sysUser = UserThreadLocal.get();
        Comment comment = new Comment();
        //设置文章id、作者、内容、创建时间
        comment.setArticleId(commentParam.getArticleId());
        comment.setAuthorId(sysUser.getId());
        comment.setContent(commentParam.getContent());
        comment.setCreateDate(System.currentTimeMillis());
        Long parent = commentParam.getParent();
        //如果level是0那么就是level=1
        if (parent == null || parent == 0) {
            comment.setLevel(1);
        } else {
            comment.setLevel(2);
        }
        comment.setParentId(parent == null ? 0 : parent);
        Long toUserId = commentParam.getToUserId();
        comment.setToUid(toUserId == null ? 0 : toUserId);
        //插入comment
        this.commentMapper.insert(comment);
        return Result.success(null);
    }

    //copy整个list并返回
    private List<CommentVo> copyList(List<Comment> comments) {
        //首先创建这个返回对象
        List<CommentVo> commentVoList = new ArrayList<>();
        for (Comment comment : comments) {
            commentVoList.add(copy(comment));
        }
        return commentVoList;
    }

    //copy一下list中的元素
    private CommentVo copy(Comment comment) {
        CommentVo commentVo = new CommentVo();
        BeanUtils.copyProperties(comment, commentVo);
        commentVo.setId(String.valueOf(comment.getId()));
        //只能copy类型相同
        Long articleId = comment.getArticleId();
        UserVo userVo = this.sysUserService.findUserVoById(articleId);
        commentVo.setAuthor(userVo);
        //子评论
        Integer level = comment.getLevel();
        //只有level是1的时候才有子评论（这里就做2层）
        if (level == 1) {
            Long id = comment.getId();
            List<CommentVo> commentVoList = findCommentsByParentId(id);
            commentVo.setChildrens(commentVoList);
        }
        //to user给谁评论
        if (level > 1) {
            Long toUid = comment.getToUid();
            UserVo toUserVo = this.sysUserService.findUserVoById(toUid);
            commentVo.setToUser(toUserVo);
        }
        return commentVo;
    }

    private List<CommentVo> findCommentsByParentId(Long id) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getParentId, id);
        queryWrapper.eq(Comment::getLevel, 2);
        return copyList(commentMapper.selectList(queryWrapper));
    }
}
