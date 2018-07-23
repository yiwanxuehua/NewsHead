package com.nowcoder.service;

import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    JedisAdapter jedisAdapter;

    //1、获取当前的点赞状态，当前用户是否已经点赞或者点踩；
    public int getLikeStatus(int userId,int entiType,int entityId){

        String likeKey= RedisKeyUtil.getLikeKey(entityId,entiType);
        if(jedisAdapter.sismember(likeKey,String.valueOf(userId))){
            return 1;
        }
        //如果当前用户
        String disLikeKey=RedisKeyUtil.getDisLikeKey(entityId,entiType);
        return jedisAdapter.sismember(disLikeKey,String.valueOf(userId))?-1:0;
    }
    //2、如果用户喜欢当前咨询、jedis中插入该信息，并且从dislike中删除，最后返回喜欢的数量；
    public long like(int userId,int entiType,int entityId){

        String likeKey=RedisKeyUtil.getLikeKey(entityId,entiType);
        jedisAdapter.sadd(likeKey,String.valueOf(userId));

        String dislikeKey=RedisKeyUtil.getDisLikeKey(entityId,entiType);
        jedisAdapter.srem(dislikeKey,String.valueOf(userId));
        return jedisAdapter.scard(likeKey);
    }
    //3、如果用户不喜欢，则将用户加入不喜欢的Jedis中去；
    public long disLike(int userId, int entityType, int entityId) {
        // 在反对集合里增加
        String disLikeKey = RedisKeyUtil.getDisLikeKey(entityId, entityType);
        jedisAdapter.sadd(disLikeKey, String.valueOf(userId));
        // 从喜欢里删除
        String likeKey = RedisKeyUtil.getLikeKey(entityId, entityType);
        jedisAdapter.srem(likeKey, String.valueOf(userId));
        return jedisAdapter.scard(likeKey);
    }
}
