package com.nowcoder.async.handler;

import com.nowcoder.async.EventHandler;
import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventType;
import com.nowcoder.model.Message;
import com.nowcoder.model.User;
import com.nowcoder.service.MessageService;
import com.nowcoder.service.UserService;
import org.apache.velocity.util.ArrayListWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

//将该handler注册
@Component
public class LikeHandler implements EventHandler{

    @Autowired
    private UserService userService;
    @Autowired
    private MessageService messageService;

    //异步事件中，要将事件包装为message发送给事件接受者；
    @Override
    public void doHandle(EventModel model) {
        Message message=new Message();
        User user=userService.getUser(model.getActorId());
        //消息内容的设置：
        message.setToId(/*model.getEntityOwnerId()*/model.getActorId());
        message.setContent("用户"+user.getName()+"点赞了你的资讯:" +
                "http:127.0.0.1:8080/news/"+String.valueOf(model.getEntityId()));
        System.out.println("点赞了");
        message.setFromId(3);//匿名用户；
        message.setCreatedDate(new Date());
        //将该点赞信息，添加到信息表中；
        messageService.addMessage(message);
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        //返回一个包含LIKE的List；
        return Arrays.asList(EventType.LIKE);
    }
}
