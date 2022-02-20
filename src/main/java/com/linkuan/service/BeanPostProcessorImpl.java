package com.linkuan.service;

import com.spring.BeanPostProcessor;
import com.spring.annotation.Component;

import java.beans.Introspector;
import java.lang.reflect.Proxy;

/**
 * @author linkuan
 */
@Component
public class BeanPostProcessorImpl implements BeanPostProcessor {

    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println(beanName);
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) {

        // 对 OrderService进行增强
        if (beanName.equals(Introspector.decapitalize(OrderService.class.getSimpleName()))) {

            System.out.println("Proxy" + beanName);

            return Proxy.newProxyInstance(BeanPostProcessorImpl.class.getClassLoader(), bean.getClass().getInterfaces(), (proxy, method, args) -> {

                // 切面的逻辑
                System.out.println("invoke " + beanName + "的切面的逻辑");


                return method.invoke(bean, args);
            });

        }
        return bean;
    }
}
