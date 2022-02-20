

package com.spring.annotation;

import com.sun.istack.internal.Nullable;

public interface BeanPostProcessor {


	@Nullable
	default Object postProcessBeforeInitialization(Object bean, String beanName) {
		return bean;
	}

	@Nullable
	default Object postProcessAfterInitialization(Object bean, String beanName) {
		return bean;
	}

}
