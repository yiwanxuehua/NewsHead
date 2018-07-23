package com.nowcoder.service;

import com.nowcoder.dao.NewsDAO;
import com.nowcoder.model.News;
import com.nowcoder.util.ToutiaoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/**
 * Created by nowcoder on 2016/7/2.
 */
@Service
public class NewsService {
    @Autowired
    private NewsDAO newsDAO;


    //获取该用户所发布的资讯
    public List<News> getLatestNews(int userId, int offset, int limit) {
        return newsDAO.selectByUserIdAndOffset(userId, offset, limit);
    }
    //获取对应newsId的资讯的详细信息
    public News getById(int newsId){
        return newsDAO.getById(newsId);
    }

    //上传图片处理
    public String saveImage(MultipartFile file) throws IOException {
        int dosPos=file.getOriginalFilename().lastIndexOf(".");//获取上传图片的后缀名；
        if(dosPos<0){
            return null;//图片不符合命名要求，返回null；
        }
        //获取到小写的后缀名；
        String fileExt=file.getOriginalFilename().substring(dosPos+1).toLowerCase();
        //如果后缀名不符合规范，返回
        if(!ToutiaoUtil.isFileAllowed(fileExt)){
            return null;
        }
        //所有上传的图片都需要重新命名**.后缀
        String fileName= UUID.randomUUID().toString().replaceAll("-","")+"."+fileExt;
        //通过copy的方式复制流中的图片到"D:/upload/"文件夹下，并以filename命名；
        Files.copy(file.getInputStream(),new File(ToutiaoUtil.IMAGE_DIR+fileName).toPath(),
                StandardCopyOption.REPLACE_EXISTING);
        return ToutiaoUtil.TOUTIAO_DOMAIN + "image?name=" + fileName;
    }
    //咨询发布处理
    public int addNews(News news){
        newsDAO.addNews(news);
        return news.getId();
    }

    //更新new中的评论数量
    public int updateCommentCount(int id,int count){
        return newsDAO.updateCommentCount(id,count);
    }
    //更新news中的喜欢数量
    public int updateLikeCount(int id,int count){
        return newsDAO.updateLikeCount(id, count);
    }
}
