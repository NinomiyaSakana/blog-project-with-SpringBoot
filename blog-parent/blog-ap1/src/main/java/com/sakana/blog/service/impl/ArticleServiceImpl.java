package com.sakana.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sakana.blog.dao.dos.Archives;
import com.sakana.blog.dao.mapper.ArticleBodyMapper;
import com.sakana.blog.dao.mapper.ArticleMapper;
import com.sakana.blog.dao.mapper.ArticleTagMapper;
import com.sakana.blog.dao.pojo.Article;
import com.sakana.blog.dao.pojo.ArticleBody;
import com.sakana.blog.dao.pojo.ArticleTag;
import com.sakana.blog.dao.pojo.SysUser;
import com.sakana.blog.service.*;
import com.sakana.blog.utils.UserThreadLocal;
import com.sakana.blog.vo.*;
import com.sakana.blog.vo.params.ArticleParam;
import com.sakana.blog.vo.params.PageParams;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ArticleServiceImpl implements ArticleService {
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private TagService tagService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private ArticleTagMapper articleTagMapper;

    public Result listArticle(PageParams pageParams){
        Page<Article> page = new Page<>(pageParams.getPage(),pageParams.getPageSize());
        IPage<Article> articleIPage = this.articleMapper.listArticle(page,
                pageParams.getCategoryId(),
                pageParams.getTagId(),
                pageParams.getYear(),
                pageParams.getMonth());
        return Result.success(copyList(articleIPage.getRecords(),true,true));
    }

//    @Override
//    public Result listArticle(PageParams pageParams) {
//        /**
//         * 1、分页查询article数据库表
//         */
//        Page<Article> page = new Page<>(pageParams.getPage(), pageParams.getPageSize());
//        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
//        if (pageParams.getCategoryId() != null) {
//            //and category_id=#{categoryId}
//            queryWrapper.eq(Article::getCategoryId, pageParams.getCategoryId());
//        }
//        List<Long> articleIdList = new ArrayList<>();
//        if (pageParams.getTagId() != null) {
//            //加入标签 条件查询
//            //article表中 并没有tag字段
//            //article_tag article_id  1: tag_id
//            LambdaQueryWrapper<ArticleTag> articleTagLambdaQueryWrapper = new LambdaQueryWrapper<>();
//            articleTagLambdaQueryWrapper.eq(ArticleTag::getTagId, pageParams.getTagId());
//            List<ArticleTag> articleTags = articleTagMapper.selectList(articleTagLambdaQueryWrapper);
//            for (ArticleTag articleTag : articleTags) {
//                articleIdList.add(articleTag.getArticleId());
//            }
//            if (articleIdList.size() > 0) {
//                //add id in(1,2,3)
//                queryWrapper.in(Article::getId,articleIdList);
//            }
//
//        }
//        //是否置顶进行排序
//        //倒序排列，创建时间排序
//        //order by create_date desc
//        queryWrapper.orderByDesc(Article::getWeight, Article::getCreateDate);
//        //得到page
//        //mybatis plus提供了selectPage方法，可以直接分页查询
//        Page<Article> articlePage = articleMapper.selectPage(page, queryWrapper);
//        //得到其中的list
//        List<Article> records = articlePage.getRecords();
//        //能直接返回吗
//        //很明显不能
//        List<ArticleVo> articleVoList = copyList(records, true, true);
//        return Result.success(articleVoList);
//    }

    @Override
    public Result hotArticle(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getViewCounts);
        queryWrapper.select(Article::getId, Article::getTitle);
        queryWrapper.last("limit" + limit);
        //select id title from article order by view_counts desc limit 5
        List<Article> article = articleMapper.selectList(queryWrapper);
        return Result.success(copyList(article, false, false));
    }

    @Override
    public Result newArticles(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        //跟上面的对比，最新文章和最热文章只是改了getCreateDate，就可以根据date来排序了
        queryWrapper.orderByDesc(Article::getCreateDate);
        queryWrapper.select(Article::getId, Article::getTitle);
        queryWrapper.last("limit" + limit);
        //select id title from article order by creat_date desc limit 5
        List<Article> article = articleMapper.selectList(queryWrapper);
        return Result.success(copyList(article, false, false));
    }

    @Override
    public Result listArchives() {
        List<Archives> archivesList = articleMapper.listArchives();
        return Result.success(archivesList);
    }

    @Autowired
    private ThreadService threadService;

    @Override
    public Result findArticleById(Long articleId) {
        /**
         * 1、根据id查询文章信息
         * 2、根据bodyId和categoryId去做关联查询
         * 3、返回articleVo对象
         */
        Article article = this.articleMapper.selectById(articleId);
        //把artivle转化为articleVo
        ArticleVo articleVo = copy(article, true, true, true, true);
        //查看完文章了，新增阅读数，有没有问题呢？
        //查看完文章之后，本应该返回数据，这时候做了一个更新操作
        //更新时加写锁，会阻塞其他的读操作，性能会比较低
        //更新增加了此次接口的耗时，一旦更新出问题，不能影响文章的操作
        //想到线程池的应用，可以把更新操作扔到线程池中执行，和主线程不相关
        //更新文章浏览记录数updateArticleViewCount
        threadService.updateArticleViewCount(articleMapper, article);
        return Result.success(articleVo);
    }

    @Override
    public Result publish(ArticleParam articleParam) {
        //发布的用户是当前登陆用户
        SysUser sysUser = UserThreadLocal.get();
        /**
         * 1、发布文章，目的是构建article对象
         * 2、作者id是当前的登录用户，用UserThreadLocal来拿，此接口要加入到登录拦截中
         * 3、标签，要将标签加入关联列表中
         * 4、内容存储article bodyid
         */
        Article article = new Article();
        article.setAuthorId(sysUser.getId());
        article.setCategoryId(Long.parseLong(articleParam.getCategory().getId().toString()));
        article.setCreateDate(System.currentTimeMillis());
        article.setCommentCounts(0);
        article.setSummary(articleParam.getSummary());
        article.setTitle(articleParam.getTitle());
        article.setViewCounts(0);
        article.setWeight(Article.Article_Common);
        article.setBodyId(-1L);
        //插入之后会生成一个文章id
        this.articleMapper.insert(article);

        List<TagVo> tags = articleParam.getTags();
        if (tags != null) {
            for (TagVo tag : tags) {
                Long articleId = article.getId();
                ArticleTag articleTag = new ArticleTag();
                articleTag.setTagId(Long.parseLong(tag.getId()));
                //强转Long.parseLong
                articleTag.setArticleId(articleId);
                //保存tag
                articleTagMapper.insert(articleTag);
            }
        }
        //body 内容存储
        ArticleBody articleBody = new ArticleBody();
        articleBody.setContent(articleParam.getBody().getContent());
        articleBody.setContentHtml(articleParam.getBody().getContentHtml());
        articleBody.setArticleId(article.getId());
        articleBodyMapper.insert(articleBody);
        //先插入才会产生id
        article.setBodyId(articleBody.getId());
        articleMapper.updateById(article);
        Map<String, String> map = new HashMap<>();
        map.put("id", article.getId().toString());
        //用map转化成string可以没有精度损失
        return Result.success(map);
    }

    private List<ArticleVo> copyList(List<Article> records, boolean isTag, boolean isAuthor) {
        List<ArticleVo> articleVoList = new ArrayList<>();
        for (Article record : records) {
            articleVoList.add(copy(record, isTag, isAuthor, false, false));
        }
        return articleVoList;
    }

    //重载，不更改上面的方法
    private List<ArticleVo> copyList(List<Article> records, boolean isTag, boolean isAuthor, boolean isBody, boolean isCategory) {
        List<ArticleVo> articleVoList = new ArrayList<>();
        for (Article record : records) {
            articleVoList.add(copy(record, isTag, isAuthor, isBody, isCategory));
        }
        return articleVoList;
    }

    @Autowired
    private CategoryService categoryService;

    private ArticleVo copy(Article article, boolean isTag, boolean isAuthor, boolean isBody, boolean isCategory) {
        ArticleVo articleVo = new ArticleVo();
        articleVo.setId(String.valueOf(article.getId()));
        //BeanUtils属性，spring提供的
        BeanUtils.copyProperties(article, articleVo);
        //时间格式copy不过来，所以直接提取article中的时间
        //转换成yyyy-mm-dd HH:mm的格式
        articleVo.setCreateDate(new DateTime(article.getCreateDate()).toString("yyyy-mm-dd HH:mm"));
        //并不是所有的接口都需要标签，作者信息
        if (isTag) {
            Long articleId = article.getId();
            articleVo.setTags(tagService.findTagsByArticleId(articleId));
        }
        if (isAuthor) {
            Long authorId = article.getAuthorId();
            articleVo.setAuthor(sysUserService.findUserById(authorId).getNickname());
        }
        if (isBody) {
            Long bodyId = article.getBodyId();
            articleVo.setBody(findArticleBodyById(bodyId));
        }
        if (isCategory) {
            Long categoryId = article.getCategoryId();
            //单独建立了一个类型来处理它，不在article中
            articleVo.setCategory(categoryService.findCategoryById(categoryId));
        }
        return articleVo;
    }

    @Autowired
    private ArticleBodyMapper articleBodyMapper;

    private ArticleBodyVo findArticleBodyById(Long bodyId) {
        ArticleBody articleBody = articleBodyMapper.selectById(bodyId);
        ArticleBodyVo articleBodyVo = new ArticleBodyVo();
        articleBodyVo.setContent(articleBody.getContent());
        return articleBodyVo;
    }
}
