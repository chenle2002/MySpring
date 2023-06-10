package com.chenle.service;

import com.chenle.annotations.Autowired;
import com.chenle.spring.interfaces.BeanNameAware;
import com.chenle.spring.interfaces.InitializingBean;
import com.chenle.annotations.MyComponent;

/**
 * @Author 陈乐
 * @Date 2023/6/8 21:17
 * @Version 1.0
 */
@MyComponent("userService")
public class Userservice implements BeanNameAware, InitializingBean, UserInterface {

    @Autowired
    OrderService orderService;

    String beanName;

    String xxx;

    public void test() {
        System.out.println("@Autowired----------使用@Autowired注入的orderservice为：" + orderService + ";依赖注入成功！");
    }

    @Override
    public void setBeanName(String beanName) {
        System.out.println("BeanNameAware----------该Service实现了BeanNameAware接口，执行了进行名字设定的aware回调");
        this.beanName = beanName;
    }

    @Override
    public void afterPropertiesSet() {
        //....
        System.out.println("InitializingBean----------该Service实现了InitializingBean接口，执行了afterPropertiesSet方法");
    }
}
