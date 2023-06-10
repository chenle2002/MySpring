package com.chenle.service;

import com.chenle.spring.interfaces.BeanPostProcessor;
import com.chenle.annotations.MyComponent;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@MyComponent
public class ChenleBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(String beanName, Object bean) {
        if (beanName.equals("userService")) {
            System.out.println("postProcessBeforeInitialization---------bean初始化前执行了该逻辑！");
        }

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(String beanName, Object bean) {

        if (beanName.equals("userService")) {
            Object proxyInstance = Proxy.newProxyInstance(ChenleBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    //先执行切面逻辑，再执行该对象对应的方法
                    System.out.println("postProcessAfterInitialization----------bean初始化后执行了切面逻辑");
                    return method.invoke(bean, args);
                }
            });

            return proxyInstance;
        }

        return bean;
    }
}
