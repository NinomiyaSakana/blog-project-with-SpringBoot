package com.sakana.blog.controller;

import com.sakana.blog.common.aop.LogAnnotation;
import com.sakana.blog.common.cache.Cache;
import com.sakana.blog.service.ArticleService;
import com.sakana.blog.vo.Result;
import com.sakana.blog.vo.params.ArticleParam;
import com.sakana.blog.vo.params.PageParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

//json数据进行交互
@RestController
@RequestMapping("articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    /**
     * 首页文章列表
     * @param pageParams
     * @return
     */
    @PostMapping
    //加上次注解，代表要对此接口记录日志
    //LogAnnotation中定义module和operater的参数
    @LogAnnotation(module="文章",operator="获取文章列表")
    public Result listArticle(@RequestBody PageParams pageParams) {
        return articleService.listArticle(pageParams);
    }

    /**
     * 首页最热文章
     * @return
     */
    @PostMapping("hot")
    @Cache(expire = 5 * 60 * 1000,name = "hot_article") //加入缓存
    public Result hotArticle() {
        int limit = 5;
        return articleService.hotArticle(limit);
    }

    /**
     * 首页最新文章
     * @return
     */
    @PostMapping("hot")
    public Result newArticles() {
        int limit = 5;
        return articleService.newArticles(limit);
    }

    /**
     * 首页 文章归档
     * @return
     */
    @PostMapping("listArchives")
    public Result listArchives() {
        return articleService.listArchives();
    }

    @PostMapping("article/view/{id}")
    public Result findArticleById(@PathVariable("id") Long articleId) {
        return articleService.findArticleById(articleId);
    }

    ///articles/publish
    @PostMapping("publish")
    public Result publish(@RequestBody ArticleParam articleParam){
        //参数用ArticleParam来接收
        return articleService.publish(articleParam);
    }


}
