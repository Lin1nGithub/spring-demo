package com.linkuan.service;

import com.spring.ApplicationContext;
import com.spring.annotation.Autowired;
import com.spring.annotation.Component;
import com.spring.Enum.ScopeEnum;
import com.spring.annotation.Scope;
import com.spring.aware.BeanNameAware;

/**
 * @author linkuan
 */
@Component("")
@Scope(ScopeEnum.prototype)
public class OrderService implements OrderServiceInterface, BeanNameAware {

    @Autowired
    private UserService userService;

    private String beanName;

    @Override
    public void test() {
        System.out.println("test");
    }

    @Override
    public void setBeanName(String name) {
        System.out.println("OrderService setBeanName");
        this.beanName = name;
    }
}
