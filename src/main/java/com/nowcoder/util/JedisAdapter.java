package com.nowcoder.util;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import redis.clients.jedis.BinaryClient;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Tuple;

import java.util.List;


//该Jedis要继承InitializingBean接口，使springboot在初始化时，生成Jedis的池；
@Service
public class JedisAdapter implements InitializingBean{
    private static final Logger logger= LoggerFactory.getLogger(JedisAdapter.class);

    private Jedis jedis=null;
    private JedisPool jedisPool=null;

    //从Springboot初始化时初始化redis池
    @Override
    public void afterPropertiesSet() throws Exception {
        jedisPool =new JedisPool("localhost",6379);
    }
    private Jedis getJedis(){//从线程池中获取一个连接
        return jedisPool.getResource();
    }
//在以下业务中，评论的entityType与entityId生成key，当前的userId作为value；
    //1、用户点赞时的处理：
    public long sadd (String key,String value){
        Jedis jedis=null;
        try{
            jedis=jedisPool.getResource();
            return jedis.sadd(key,value);
        }catch (Exception e){
            logger.error("点赞发生异常"+e.getMessage());
            return 0;
        }finally {
            if(jedis!=null){
                jedis.close();//关闭线程，不然会卡住
            }
        }
    }
    //2、用户取消点赞：
    public long srem (String key,String value){
        Jedis jedis=null;
        try{
            jedis=jedisPool.getResource();
            return jedis.srem(key,value);
        }catch (Exception e){
            logger.error("取消点赞发生异常"+e.getMessage());
            return 0;
        }finally {
            if(jedis!=null){
                jedis.close();//关闭线程，不然会卡住
            }
        }
    }
    //3、当前用户是否已经点赞：
    public boolean sismember (String key,String value){
        Jedis jedis=null;
        try{
            jedis=jedisPool.getResource();
            return jedis.sismember(key,value);
        }catch (Exception e){
            logger.error("点赞发生异常"+e.getMessage());
            return false;
        }finally {
            if(jedis!=null){
                jedis.close();//关闭线程，不然会卡住
            }
        }
    }
    //4、判断当前点赞的个数
    public long scard (String key){
        Jedis jedis=null;
        try{
            jedis=jedisPool.getResource();
            return jedis.scard(key);
        }catch (Exception e){
            logger.error("点赞发生异常"+e.getMessage());
            return 0;
        }finally {
            if(jedis!=null){
                jedis.close();//关闭线程，不然会卡住
            }
        }
    }

// /以下业务实现异步化服务
    //向set中添加，并设定过期时间
    public void setex(String key, String value) {
    // 验证码, 防机器注册，记录上次注册时间，有效期3天
    Jedis jedis = null;
    try {
        jedis = jedisPool.getResource();
        jedis.setex(key, 10, value);
    } catch (Exception e) {
        logger.error("发生异常" + e.getMessage());
    } finally {
        if (jedis != null) {
            jedis.close();
        }
    }
}
    //redis的list的lpush：
    public long lpush(String key,String value){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.lpush(key, value);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            return 0;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
    //redis的list的阻塞式弹出：brpop
    public List<String> brpop(int timeout,String key){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.brpop(timeout,key);
        } catch (Exception e) {
            logger.error("brpop发生异常" + e.getMessage());
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
    //内部方法，将key，value写入redis中：
    public void set(String key,String value){
        Jedis jedis=null;
        try{
            jedis=jedisPool.getResource();
            jedis.set(key,value);
        }catch(Exception e){
            logger.error("redis的set操作异常"+e.getMessage());
        }finally {
            if(jedis!=null){
                jedis.close();
            }
        }
    }
    //内部方法，通过key取值
    public String get(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return getJedis().get(key);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
    //5、将key写入redis，将obj的Json字符串写入（类似序列化）
    public void setObject(String key,Object obj){
        set(key, JSON.toJSONString(obj));
    }
    //6、返序列化取出
    public <T> T getObject(String key,Class<T> clazz){
        String value =get(key);
        if(value!=null){
            return JSON.parseObject(value,clazz);
        }
        return null;
    }


//redis的基本操作，以下不是业务逻辑的一部分，打包时要将其注释掉，否则两个main，不知道找那个；
//    public static void print(int index,Object obj){
//        System.out.println(String.format("%d,%s",index,obj.toString()));
//    }
//    public  static  void main(String[]args){
//        Jedis jedis=new Jedis();
//        jedis.flushAll();//清空所有的数据库；
//    //redis的get与set
//        jedis.set("hello", "world");
//        jedis.rename("hello","newhello");
//        print(1,jedis.get("newhello"));
//        jedis.setex("hello2", 5, "world");//设置了过期的时间；
//        print(1,jedis.get("hello2"));
//    //数值的操作
//        jedis.set("pv", "100");
//        jedis.incr("pv");//pv值进行+1；
//        print(2, jedis.get("pv"));
//        jedis.decrBy("pv", 5);//pv值进行-5；
//        print(3, jedis.get("pv"));
//
//     // 列表操作, 最近来访, 粉丝列表，消息队列
//        String listName = "list";
//        for (int i = 0; i < 10; ++i) {//list的添加
//            jedis.lpush(listName, "a" + String.valueOf(i));
//        }
//        print(4, jedis.lrange(listName, 0, 12)); // 最近来访10个id
//        print(5, jedis.llen(listName));//大小
//        print(6, jedis.lpop(listName));//出栈pop，从开始pop；
//        print(7, jedis.llen(listName));//还剩下9个
//        print(8, jedis.lrange(listName, 2, 6)); // 最近来访5个 id
//        print(9, jedis.lindex(listName, 3));//剩下a5
//        //插在a4的前面和后面；
//        print(10, jedis.linsert(listName, BinaryClient.LIST_POSITION.AFTER, "a4", "xx"));
//        print(10, jedis.linsert(listName, BinaryClient.LIST_POSITION.BEFORE, "a4", "bb"));
//        print(11, jedis.lrange(listName, 0, 12));
//
//     // hash, 可变字段
//        String userKey = "userxx";
//        jedis.hset(userKey, "name", "jim");
//        jedis.hset(userKey, "age", "12");
//        jedis.hset(userKey, "phone", "18666666666");
//        print(12, jedis.hget(userKey, "name"));
//        print(13, jedis.hgetAll(userKey));
//        jedis.hdel(userKey, "phone");//删除字段
//        print(14, jedis.hgetAll(userKey));
//        print(15, jedis.hexists(userKey, "email"));
//        print(16, jedis.hexists(userKey, "age"));
//        print(17, jedis.hkeys(userKey));
//        print(18, jedis.hvals(userKey));
//        //set if not exist；不存在则补全
//        jedis.hsetnx(userKey, "school", "zju");
//        jedis.hsetnx(userKey, "name", "yxy");
//        print(19, jedis.hgetAll(userKey));
//
//    // 集合，点赞用户群, 共同好友
//        String likeKey1 = "newsLike1";
//        String likeKey2 = "newsLike2";
//        for (int i = 0; i < 10; ++i) {
//            jedis.sadd(likeKey1, String.valueOf(i));
//            jedis.sadd(likeKey2, String.valueOf(i * 2));
//        }
//        print(20, jedis.smembers(likeKey1));
//        print(21, jedis.smembers(likeKey2));
//        print(22, jedis.sunion(likeKey1, likeKey2));//求并集，不重复；
//        print(23, jedis.sdiff(likeKey1, likeKey2));//前者有，后者没有的；
//        print(24, jedis.sinter(likeKey1, likeKey2));//求交集
//        print(25, jedis.sismember(likeKey1, "12"));//是否存在
//        print(26, jedis.sismember(likeKey2, "12"));
//        jedis.srem(likeKey1, "5");
//        print(27, jedis.smembers(likeKey1));
//        // 从1移动到2
//        jedis.smove(likeKey2, likeKey1, "14");//将key2的14移动到key1的中；
//        print(28, jedis.smembers(likeKey1));
//        print(29, jedis.scard(likeKey1));//集合中的元素格式
//    //优先队列
//        String rankKey = "rankKey";
//        jedis.zadd(rankKey, 15, "Jim");
//        jedis.zadd(rankKey, 60, "Ben");
//        jedis.zadd(rankKey, 90, "Lee");
//        jedis.zadd(rankKey, 75, "Lucy");
//        jedis.zadd(rankKey, 80, "Mei");
//        print(30, jedis.zcard(rankKey));
//        print(31, jedis.zcount(rankKey, 61, 100));
//        // 改错卷了
//        print(32, jedis.zscore(rankKey, "Lucy"));
//        jedis.zincrby(rankKey, 2, "Lucy");//值增加2；
//        print(33, jedis.zscore(rankKey, "Lucy"));
//        jedis.zincrby(rankKey, 2, "Luc");//luc没有，新增了一个
//        print(34, jedis.zscore(rankKey, "Luc"));
//        print(35, jedis.zcount(rankKey, 0, 100));
//        // 1-4 名 Luc
//        print(36, jedis.zrange(rankKey, 0, 10));//从低到高的排名
//        print(36, jedis.zrange(rankKey, 1, 3));//从第一名到第三名，从小到大，还有第0名
//        print(36, jedis.zrevrange(rankKey, 1, 3));//从大到小，第一个到第三个；
//        //60以上的人，先排序
//        for (Tuple tuple : jedis.zrangeByScoreWithScores(rankKey, "60", "100")) {
//            print(37, tuple.getElement() + ":" + String.valueOf(tuple.getScore()));
//        }
//
//        print(38, jedis.zrank(rankKey, "Ben"));//Ben的排名，从小到大；
//        print(39, jedis.zrevrank(rankKey, "Ben"));
//
////redis是单线程的，但是提供了池的概念，默认是8条线程
//        JedisPool pool = new JedisPool();
//        for (int i = 0; i < 100; ++i) {
//            Jedis j = pool.getResource();
//            j.get("a");
//            print(i,"当前线程"+j.toString());
//            j.close();//关掉，重新复用；
//        }
//
//        String setKey = "zset";
//        jedis.zadd(setKey, 1, "a");
//        jedis.zadd(setKey, 1, "b");
//        jedis.zadd(setKey, 1, "c");
//        jedis.zadd(setKey, 1, "d");
//        jedis.zadd(setKey, 1, "e");
//        print(40, jedis.zlexcount(setKey, "-", "+"));
//        print(41, jedis.zlexcount(setKey, "(b", "[d"));//不包括b
//        print(42, jedis.zlexcount(setKey, "[b", "[d"));
//        jedis.zrem(setKey, "b");
//        print(43, jedis.zrange(setKey, 0, 10));
//        jedis.zremrangeByLex(setKey, "(c", "+");
//        print(44, jedis.zrange(setKey, 0, 2));
//    }

}
