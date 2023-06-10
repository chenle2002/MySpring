package com.chenle.spring;

import com.chenle.annotations.Autowired;
import com.chenle.annotations.MyComponent;
import com.chenle.annotations.MyComponentScan;
import com.chenle.annotations.Scope;
import com.chenle.spring.interfaces.BeanNameAware;
import com.chenle.spring.interfaces.BeanPostProcessor;
import com.chenle.spring.interfaces.InitializingBean;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author 陈乐
 * @Date 2023/6/8 21:17
 * @Version 1.0
 */
public class ChenleApplicationContext {

    private ConcurrentHashMap<String, ChenleBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    //单例池
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();
    private ArrayList<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();
    private Class configClass;


    public ChenleApplicationContext(Class configClass) {
        this.configClass = configClass;

        //扫描--->BeanDefinition-->beanDefinitionMap
        //查看类上是否有MyComponetScan注解，并得到注解的信息（value）
        if (configClass.isAnnotationPresent(MyComponentScan.class)) {

            MyComponentScan componentScanAnnotation = (MyComponentScan) configClass.getAnnotation(MyComponentScan.class);

            //Component扫描路径 com.chenle.service
            String path = componentScanAnnotation.value();

            //  com.chenle.service->com/chenle/service
            path = path.replace(".", "/");

            //获取类加载器,获取加载的路径
            ClassLoader classLoader = ChenleApplicationContext.class.getClassLoader();
            URL resource = classLoader.getResource(path);

            File file = new File(resource.getFile());

            //判断路径是不是文件夹
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                //筛选.class文件
                for (File f : files) {

                    //文件的绝对路径
                    String fileName = f.getAbsolutePath();

                    //判断.class文件是不是一个Bean，即MyComponent注解(使用类加载器反射)
                    if (fileName.endsWith(".class")) {
                        // com.chenle.service.Userservice
                        String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                        className = className.replace("\\", ".");
//                        System.out.println(className);

                        try {
                            Class<?> clazz = classLoader.loadClass(className);
//                            System.out.println(clazz);
                            //使用反射看这个类是不是存在MyComponent注解，是的话就是个Bean
                            if (clazz.isAnnotationPresent(MyComponent.class)) {


                                //如果实现了BeanPostProcessor接口，将其实例放到beanPostProcessorList
                                if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                                    BeanPostProcessor instance = (BeanPostProcessor) clazz.newInstance();
                                    beanPostProcessorList.add(instance);
                                }


                                //获取Mycomponnent注解的值作为beanName
                                MyComponent annotation = clazz.getAnnotation(MyComponent.class);
                                String beanName = annotation.value();

                                //如果使用Mycomponent注解时没表明value
                                if (beanName.equals("")) {
                                    //使用默认名字生成器，首字母小写等等
                                    beanName = Introspector.decapitalize(clazz.getSimpleName());
                                }

                                //BeanDefinition对象
                                ChenleBeanDefinition beanDefinition = new ChenleBeanDefinition();

                                //设置bean的属性
                                beanDefinition.setType(clazz);
                                //判断类是否有Scope注解，有的话是多例bean,将scope注解的值赋给beanDefinition对象
                                if (clazz.isAnnotationPresent(Scope.class)) {
                                    Scope scopeAnnotation = clazz.getAnnotation(Scope.class);
                                    beanDefinition.setScope(scopeAnnotation.value());
                                } else {
                                    beanDefinition.setScope("singleton");
                                }

                                beanDefinitionMap.put(beanName, beanDefinition);
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        // 实例化单例Bean
        for (String beanName : beanDefinitionMap.keySet()) {

            ChenleBeanDefinition beanDefinition = beanDefinitionMap.get(beanName);

            if (beanDefinition.getScope().equals("singleton")) {

                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);
            }
        }
    }


    private Object createBean(String beanName, ChenleBeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getType();

        try {
            //实例化
            Object instance = clazz.getConstructor().newInstance();

            //依赖注入
            for (Field f : clazz.getDeclaredFields()) {
                //寻找方法所有部分中带有Autowired注解的部分，将bean注入给它
                if (f.isAnnotationPresent(Autowired.class)) {
                    f.setAccessible(true);
                    f.set(instance, getBean(f.getName()));
                }
            }

            // Aware
            if (instance instanceof BeanNameAware) {
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            //初始化前
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessBeforeInitialization(beanName, instance);
            }

            // 初始化,调用这个bean的这个方法
            if (instance instanceof InitializingBean) {
                ((InitializingBean) instance).afterPropertiesSet();
            }

            //初始化后
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessAfterInitialization(beanName, instance);
            }

            return instance;

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }


    public Object getBean(String beanName) {

        ChenleBeanDefinition beanDefinition = beanDefinitionMap.get(beanName);

        //存在改beanName的beanDefinition
        if (beanDefinition == null) {
            throw new NullPointerException();
        } else {
            String scope = beanDefinition.getScope();

            if (scope.equals("singleton")) {
                //单例bean，先去单例池中找
                Object bean = singletonObjects.get(beanName);
                if (bean == null) {
                    Object o = createBean(beanName, beanDefinition);
                    singletonObjects.put(beanName, o);
                }
                return bean;
            } else {
                //多例bean
                return createBean(beanName, beanDefinition);
            }
        }
    }
}
