package com.spring;

import com.spring.Enum.ScopeEnum;

/**
 * @author linkuan
 */
public class BeanDefinition {

    private Class type;

    private String beanName;

    private ScopeEnum scope;

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public ScopeEnum getScope() {
        return scope;
    }

    public void setScope(ScopeEnum scope) {
        this.scope = scope;
    }
}
