package com.nowcoder.service;

import com.nowcoder.dao.LoginTicketDAO;
import com.nowcoder.dao.UserDAO;
import com.nowcoder.model.LoginTicket;
import com.nowcoder.model.User;
import com.nowcoder.util.ToutiaoUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by nowcoder on 2016/7/2.
 */
@Service
public class UserService {
    @Autowired
    private UserDAO userDAO;

    @Autowired
    private LoginTicketDAO loginTicketDAO;

    //用户注册
    public Map<String,Object> register(String username, String password){
        Map<String,Object> map=new HashMap<>();
        if(StringUtils.isBlank(username)){
            map.put("msgname","用户名不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("msgpwd","密码不能为空");
            return map;
        }
        User user=userDAO.selectByName(username);
        if(user!=null){
            map.put("msgname","用户名已被注册");
            return map;
        }
        //到这里注册的信息监测通过，生成新的USer，插入数据库
        user =new User();
        user.setName(username);
        user.setSalt(UUID.randomUUID().toString().substring(0,5));
        //这里我们在七牛云中存储了20个头像信息，用来生成用户的随机头像；
        String head=String.format("http://pboymtjpl.bkt.clouddn.com/%d.jpg",new Random().nextInt(20));
        user.setHeadUrl(head);
        user.setPassword(ToutiaoUtil.MD5(password+user.getSalt()));
        userDAO.addUser(user);
        //登陆
        String ticket=addLoginTicket(user.getId());
        map.put("ticket",ticket);
        return map;
    }
//登陆
    public Map<String,Object> login(String username, String password){
        Map<String,Object> map=new HashMap<>();
        if(StringUtils.isBlank(username)){
            map.put("msgname","用户名不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("msgpwd","密码不能为空");
            return map;
        }
        User user=userDAO.selectByName(username);
        if(user==null){
            map.put("msgname","用户名不存在");
            return map;
        }
        //到这，登陆的用户信息是正确的，需验证密码；
        if(!ToutiaoUtil.MD5(password+user.getSalt()).equals(user.getPassword())){
            map.put("msgpwd","密码不正确");
            return map;
        }
        map.put("userId",user.getId());
        //根据userna在数据库中查到的user对象，获取其；
        String ticket=addLoginTicket(user.getId());
        map.put("ticket",ticket);
        return map;
    }
    //该函数负责生成一个新的LoginTicket并add到login_ticket数据表中；
    private String addLoginTicket(int userId){
        LoginTicket ticket=new LoginTicket();
        ticket.setUserId(userId);
        Date date=new Date();
        date.setTime(date.getTime()+1000*3600*24);
        ticket.setExpired(date);
        ticket.setStatus(0);
        ticket.setTicket(UUID.randomUUID().toString().replaceAll("-",""));
        loginTicketDAO.addTicket(ticket);
        return ticket.getTicket();

    }
    //根据id获取用户
    public User getUser(int id) {
        return userDAO.selectById(id);
    }

//用户登出操作,将当前的状态设置为非0即可，0是登录状态；
    public void logout(String ticket){
        loginTicketDAO.updateStatus(ticket,1);
    }
}
