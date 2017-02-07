package com.wrp.controller;

import com.wrp.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by wuruiping on 2017/2/6.
 */
@Controller
public class LoginController {

    private static  final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LoginController.class);

    @Autowired
    UserService userService;

    @RequestMapping(path = {"/reg/"},method = {RequestMethod.POST})
    public String reg(Model model,
                      @RequestParam("username") String username,
                      @RequestParam("password") String password,
                      @RequestParam("next") String next,
                      @RequestParam(value="rememberme",defaultValue = "false") boolean rememberme,
                      HttpServletResponse response
                      ){
            try {
                Map<String, Object> map = userService.register(username, password);
                if (map.containsKey("ticket")) {
                    Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
                    cookie.setPath("/");
                    if(rememberme){
                        cookie.setMaxAge(3600*24*5);
                    }
                    response.addCookie(cookie);
                    if(StringUtils.isNotBlank(next)){
                        return "redirect:" + next;//注册之后返回刚才用户浏览的页面
                    }
                    return "redirect:/";

                }else{
                    model.addAttribute("msg",map.get("msg"));
                    return "login";
                }
            }catch (Exception e){
                logger.error("注册异常"+ e.getMessage());
                model.addAttribute("msg","服务器错误");
                return "login";
            }
    }

    @RequestMapping(path = {"/login/"},method = RequestMethod.POST)
    public String login(Model model,
                        @RequestParam("username") String username,
                        @RequestParam("password") String password,
                        @RequestParam(value="next", required = false) String next,
                        @RequestParam(value = "rememberme",defaultValue = "false") boolean rememberme,
                        HttpServletResponse response){

        try {
            Map<String, Object> map = userService.login(username, password);
            if (map.containsKey("ticket")) {
                Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
                cookie.setMaxAge(3600 * 24 * 5);
                cookie.setPath("/");
                response.addCookie(cookie);
//                if (StringUtils.isNotBlank("next")) {
//                    System.out.println("next 是 "+next);
//                    return "redirect:" + next;
//                }
                return "redirect:/";

            } else {
                model.addAttribute("msg", map.get("msg"));
                return "login";
            }
        }catch (Exception e){
            logger.error("登录异常" + e.getMessage());
            return "login";
        }
    }

    @RequestMapping(path = {"/reglogin"}, method = {RequestMethod.GET})
    public String regloginPage(Model model, @RequestParam(value = "next", required = false) String next) {
        model.addAttribute("next", next);
        return "login";
    }

    @RequestMapping(path = "/logout/",method = {RequestMethod.GET,RequestMethod.POST})
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        return "redirect:/";
    }
}