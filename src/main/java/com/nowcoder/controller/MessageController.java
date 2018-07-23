package com.nowcoder.controller;

import com.nowcoder.dao.UserDAO;
import com.nowcoder.model.HostHolder;
import com.nowcoder.model.Message;
import com.nowcoder.model.User;
import com.nowcoder.model.ViewObject;
import com.nowcoder.service.MessageService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.ToutiaoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class MessageController {
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @Autowired
    MessageService messageService;

//添加一条消息
    @RequestMapping(path = {"/msg/addMessage"}, method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String addMessage(@RequestParam("fromId") int fromId,
                             @RequestParam("toId") int toId,
                             @RequestParam("content") String content) {
        try {
            Message msg = new Message();
            msg.setContent(content);
            msg.setCreatedDate(new Date());
            msg.setFromId(fromId);
            msg.setToId(toId);
//            msg.setConversationId(fromId < toId ?
//                    String.format("%d_%d", fromId, toId) : String.format("%d_%d", toId, fromId));
            messageService.addMessage(msg);
            return ToutiaoUtil.getJSONString(msg.getId());
        } catch (Exception e) {
            logger.error("增加评论失败" + e.getMessage());
            return ToutiaoUtil.getJSONString(1,"加入評論失敗");
        }
    }
//獲取消息的具體
    @RequestMapping(path = {"/msg/detail"}, method = {RequestMethod.GET})
    public String conversationDetail(Model model,@RequestParam("conversationId") String conversationId){
        try{
            List<ViewObject> messages=new ArrayList<>();
            List<Message> conversationList =messageService.getConversationDetail(conversationId,0,10);
            for(Message msg:conversationList){
                ViewObject vo=new ViewObject();
                vo.set("message",msg);
                User user=userService.getUser(msg.getFromId());
                if(user==null){
                    continue;
                }
                vo.set("headUrl",user.getHeadUrl());
                vo.set("username",user.getName());
                messages.add(vo);
            }
            model.addAttribute("messages",messages);
        }catch(Exception e){
            logger.error("获取站内信息失败！"+e.getMessage());
        }
        return "letterDetail";
    }
//获取两个用户之间的聊天记录，并按照conversationId分组，按聊天数量排序
    @RequestMapping(path={"/msg/list"},method = {RequestMethod.GET})
    public String conversationList(Model model){
        try{
            int localUserId=hostHolder.getUser().getId();//当前用户是已知的；
            List<ViewObject> conversations=new ArrayList<>();
            List<Message> conversationList=messageService.getConversationList(localUserId,0,10);

                for(Message msg:conversationList){
                    ViewObject vo=new ViewObject();
                    vo.set("conversation",msg);
                    int targetId=msg.getFromId()==localUserId?msg.getToId():msg.getFromId();
                    User user=userService.getUser(targetId);
//                    vo.set("headUrl",user.getHeadUrl());
//                    vo.set("userName",user.getName());
//                    vo.set("targetId",targetId);
//                    vo.set("totalCount",msg.getId());
                    vo.set("user",user);
                    vo.set("unReadCount",messageService.getUnreadCount(localUserId,msg.getConversationId()));
                    conversations.add(vo);
                }
                model.addAttribute("conversations",conversations);
                return "letter";

        }catch(Exception e){
            logger.error("获取站内聊天列表出错！"+e.getMessage());
        }
        return "letter";
    }
}
