package com.linkuan.service;

import com.spring.annotation.Component;
import com.spring.Enum.ScopeEnum;
import com.spring.annotation.Scope;

/**
 * @author linkuan
 */
@Component("userService")
@Scope(ScopeEnum.singleton)
public class UserService {


    public void test() {
        System.out.println("test");
    }
}
