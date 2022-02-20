package com.linkuan;

import com.linkuan.service.OrderServiceInterface;
import com.linkuan.service.UserService;
import com.spring.ApplicationContext;

/**
 * @author linkuan
 * 启动类
 */
public class Test {

    public static void main(String[] args) {

        // 扫描 -> 创建单例bean
        ApplicationContext applicationContext = new ApplicationContext(AppConfig.class);

        UserService userService = (UserService) applicationContext.getBean("userService");
        userService.test();

        OrderServiceInterface orderService = (OrderServiceInterface) applicationContext.getBean("orderService");
        orderService.test();
    }
}
