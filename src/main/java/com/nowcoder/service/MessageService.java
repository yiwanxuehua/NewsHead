package com.nowcoder.service;

import com.nowcoder.dao.MessageDao;
import com.nowcoder.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    MessageDao messageDao;

//增加对话
    public int addMessage(Message message){
        return messageDao.addMessage(message);
    }
//获取某用户的会话列表
    public List<Message> getConversationList(int userId,int offset,int limit){
        return messageDao.getConversationList(userId,offset,limit);
    }

//获取某具体会话
    public List<Message> getConversationDetail(String conversationId,int offset,int limit){
        return messageDao.getConversationDetail(conversationId,offset,limit);
    }
//获取为读消息数量
    public int getUnreadCount(int userId,String conservationId){
        return messageDao.getConversationUnReadCount(userId,conservationId);
    }
}
