package com.nowcoder.async;


import com.alibaba.fastjson.JSON;
import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//初始化时加载该服务
@Service
public class EventConsumer implements InitializingBean,ApplicationContextAware{

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);
    private  ApplicationContext applicationContext;
    //存放事件的类型+所有处理该事件的handler；
    private Map<EventType,List<EventHandler>> config=new HashMap<>();

    @Autowired
    JedisAdapter jedisAdapter;

    @Override
    public void afterPropertiesSet() throws Exception {
        //获取所有的事件处理器
        Map<String,EventHandler> beans=applicationContext.getBeansOfType(EventHandler.class);
        if(beans!=null){
            //循环取出每一个事件处理器：
            for(Map.Entry<String,EventHandler> entry:beans.entrySet()){
                List<EventType> eventTypes=entry.getValue().getSupportEventTypes();
                //获取事件的类型type，
                //如果config中没有type，就新建一个List用来保存EventHandler信息；
                //最后type对应的List中注册事件处理器
                for(EventType type:eventTypes){
                    if(!config.containsKey(type)){
                        config.put(type,new ArrayList<EventHandler>());
                    }
                    config.get(type).add(entry.getValue());
                }
            }
        }
        //启动线程去消费事件：
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){//不断从EVENT的缓存队列中取出EventModel开启线程
                    String key= RedisKeyUtil.getEventQueueKey();
                    //EventProducer中lpush的是<EVENT，EventModel>
                    List<String> messages=jedisAdapter.brpop(0,key);
                    if(messages!=null){
                        for(String message:messages){
                            if(message.equals(key)){
                                continue;
                            }
                            EventModel eventModel= JSON.parseObject(message,EventModel.class);
                            if(!config.containsKey(eventModel.getType())){
                                logger.error("不能识别的事件！");
                                continue;
                            }
                            for(EventHandler handler:config.get(eventModel.getType())){
                                handler.doHandle(eventModel);
                            }
                        }
                    }
                }
            }
        });
        thread.start();

    }

    //获取上下文
    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext=applicationContext;
    }
}
