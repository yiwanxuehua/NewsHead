package com.nowcoder.controller;

import com.nowcoder.model.*;
import com.nowcoder.service.*;
import com.nowcoder.util.ToutiaoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Controller
public class NewsController {
    private static final Logger logger=LoggerFactory.getLogger(NewsController.class);

    @Autowired
    private NewsService newsService;
    @Autowired
    private UserService userService;
    @Autowired
    QiniuService qiniuService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private CommentService commentService;
    @Autowired
    private LikeService likeService;

//图片加载、下载功能
    @RequestMapping(path = {"/image"},method = {RequestMethod.GET})
    @ResponseBody
    public void getImage(@RequestParam("name") String imageName,
                         HttpServletResponse response){
        try{
            response.setContentType("image/jpeg");
            StreamUtils.copy(new FileInputStream(
                    new File(ToutiaoUtil.IMAGE_DIR+imageName)),
                    response.getOutputStream());
        }  catch (Exception e) {
            logger.error("读取图片错误" + imageName + e.getMessage());
        }
    }
//图片上传功能
    @RequestMapping(path ={"/uploadImage/"},method = {RequestMethod.POST})
    @ResponseBody
    public String uploadImage(@RequestParam("file")MultipartFile file){
        try{
            //String fileUrl=newsService.saveImage(file);
            String fileUrl = qiniuService.saveImage(file);
            if(fileUrl==null){
                return ToutiaoUtil.getJSONString(1,"上传图片失败");
            }
            return ToutiaoUtil.getJSONString(0,fileUrl);
        } catch (IOException e) {
            logger.error("上传图片失败"+e.getMessage());
            return ToutiaoUtil.getJSONString(1,"上传失败");
        }
    }

//咨询发布功能
    @RequestMapping(path = {"/user/addNews/"},method = {RequestMethod.POST})
    @ResponseBody
    public String addNews(@RequestParam("image") String image,
                          @RequestParam("title") String title,
                          @RequestParam("link") String link){
        try{
            News news=new News();
            news.setCreatedDate(new Date());
            news.setTitle(title);
            news.setImage(image);
            news.setLink(link);
            if(hostHolder.getUser()!=null){//如果用户已经登录
                news.setUserId(hostHolder.getUser().getId());
            }else{//没有登陆的话是匿名状态，3代表匿名，可以在前端处理；
                news.setUserId(3);
            }
            newsService.addNews(news);
            return ToutiaoUtil.getJSONString(0);
        }catch (Exception e){
            logger.error("添加咨询失败"+e.getMessage());
            return ToutiaoUtil.getJSONString(1,"咨询发布失败");
        }
    }
//资讯的浏览，显示资讯的主体内容
    @RequestMapping(path={"/news/{newsId}"},method = {RequestMethod.GET})
    public String newsDetail(@PathVariable("newsId") int newsId, Model model){
        try{
            News news=newsService.getById(newsId);
            if(news !=null){
                int localUserId=hostHolder.getUser()!=null?hostHolder.getUser().getId():0;
                if(localUserId!=0){
                    model.addAttribute("like",likeService.getLikeStatus(localUserId,EntityType.ENTITY_NEWS,news.getId()));
                }else{
                    model.addAttribute("like",0);
                }
                //加入评论；
                List<Comment> comments=commentService.getCommentsByEntity(news.getId(), EntityType.ENTITY_NEWS);
                List<ViewObject> commentVOs= new ArrayList<ViewObject>();//还要显示用户的头像信息；
                for(Comment comment:comments){
                    ViewObject commentVO=new ViewObject();
                    commentVO.set("comment",comment);
                    commentVO.set("user",userService.getUser(comment.getUserId()));
                    commentVOs.add(commentVO);
                }
                model.addAttribute("comments",commentVOs);
            }
            model.addAttribute("news",news);
            model.addAttribute("owner",userService.getUser(news.getUserId()));
        }catch (Exception e){
            logger.error("获取资讯明细错误" + e.getMessage());
        }
        return "detail";
    }

//增加评论
    @RequestMapping(path = {"/addComment"},method = {RequestMethod.POST})
    public String addComment(@RequestParam("newsId") int newsId,
                             @RequestParam("content") String content){
        try{
            Comment comment=new Comment();
            comment.setUserId(hostHolder.getUser().getId());
            comment.setContent(content);
            comment.setEntityType(EntityType.ENTITY_NEWS);
            comment.setEntityId(newsId);
            comment.setCreatedDate(new Date());
            comment.setStatus(0);
            commentService.addComment(comment);

            //更新news中的评论数量
            int count=commentService.getCommentCount(comment.getEntityId(),comment.getEntityType());
            newsService.updateCommentCount(comment.getEntityId(),count);
        }catch (Exception e){
            logger.error("提交评论错误" + e.getMessage());
        }
        return "redirect:/news/"+String.valueOf(newsId);
    }

}
