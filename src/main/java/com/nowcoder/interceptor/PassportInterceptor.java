package com.nowcoder.interceptor;

import com.nowcoder.dao.LoginTicketDAO;
import com.nowcoder.dao.UserDAO;
import com.nowcoder.model.HostHolder;
import com.nowcoder.model.LoginTicket;
import com.nowcoder.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

//该拦截器用来拦截用户的访问请求，判断该用户是否是已注册用户；
@Component
public class PassportInterceptor implements HandlerInterceptor{//拦截器的实现方式之一
    //负责通过ticket获取数据库中的用户id
    @Autowired
    private LoginTicketDAO loginTicketDAO;
    //负责通过id获取数据库中对应的用户信息
    @Autowired
    private UserDAO userDAO;
    //用来保存线程本地User对象；
    @Autowired
   private  HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String ticket=null;
        //首先判断用户访问请求中是否包含“tickte”信息；
        if(httpServletRequest.getCookies()!=null){
            for(Cookie cookie:httpServletRequest.getCookies()){
                if(cookie.getName().equals("ticket")){
                    ticket=cookie.getValue();
                    break;
                }
            }
        }
        if(ticket!=null){
            LoginTicket loginTicket=loginTicketDAO.selectByTicket(ticket);
            if(loginTicket==null||loginTicket.getExpired().before(new Date())||loginTicket.getStatus()!=0){
                return true;//此时，相当于一个新的用户访问过来；
            }
            //至此，该用户信息存在与数据库中，需要取出；
            User user=userDAO.selectById(loginTicket.getUserId());//根据ticket中的id信息取出对应的用户；
            //需要将该用户封装到一个模型中，作为线程的本地变量，保证多线程
            hostHolder.setUser(user);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        if(modelAndView!=null && hostHolder.getUser()!=null){
            //在视图&模型中加入要返回的值User
            modelAndView.addObject("user",hostHolder.getUser());

        }
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        hostHolder.clear();//在调用controller之后需要将hostHolder清除，不然累积过多；
    }
}

