package com.chenle.test;

import com.chenle.config.AppConfig;
import com.chenle.service.UserInterface;
import com.chenle.spring.ChenleApplicationContext;

/**
 * @Author 陈乐
 * @Date 2023/6/8 21:17
 * @Version 1.0
 */
public class Test {
    public static void main(String[] args) {
        ChenleApplicationContext chenleApplicationContext = new ChenleApplicationContext(AppConfig.class);

//        Userservice userService = (Userservice) chenleApplicationContext.getBean("userService");
//        System.out.println(chenleApplicationContext.getBean("userService"));
//        System.out.println(chenleApplicationContext.getBean("userService"));
//        System.out.println(chenleApplicationContext.getBean("userService"));
//        System.out.println(chenleApplicationContext.getBean("orderService"));
        UserInterface userservice = (UserInterface) chenleApplicationContext.getBean("userService");
        userservice.test();
    }
}
