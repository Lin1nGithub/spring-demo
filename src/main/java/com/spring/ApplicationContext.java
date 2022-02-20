package com.spring;

import com.spring.Enum.ScopeEnum;
import com.spring.annotation.*;
import com.spring.aware.BeanNameAware;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author linkuan
 */
public class ApplicationContext {

    private Class configClz;

    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    private Map<String, Object> singletonObjects = new HashMap<>();

    List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    public ApplicationContext(Class clz) {

        this.configClz = clz;

        // 扫描
        this.scan(clz);

        // 遍历 创建单例bean
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if (isSingleton(beanDefinition.getScope())) {
                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);
            }
        }

    }

    private void scan(Class clz) {
        // 判断配置类 是否有注解 ComponentScan
        if (clz.isAnnotationPresent(ComponentScan.class)) {

            // 获取需要扫描的包路径
            ComponentScan componentScanAnnotation = (ComponentScan) clz.getAnnotation(ComponentScan.class);
            String path = componentScanAnnotation.value(); // com.linkuan.service
            System.out.println("componentScan path:" + path);

            // 通过加载编译好的class文件, 从而获取需要扫描的类。
            // 开发者写的类统一由 AppClassLoader管理。
            ClassLoader classLoader = ApplicationContext.class.getClassLoader();

            // method getResource 传入 值是文件目录, 所以需要将"."转化成"/"
            String filePath = path.replace(".", "/"); // com/linkuan/service
            System.out.println("classes file path:" + filePath);

            // com/linkuan/service --> 相对路径 相对于 AppClassLoader的目录, target/classes。
            // 所以能很方便的获取到 target/classes/com/linkuan/service目录下的文件, 即需要加载到容器中的
            URL resource = classLoader.getResource(filePath);
            File file = new File(resource.getFile());

            // 遍历文件
            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    File absoluteFile = f.getAbsoluteFile();
                    String absolutePath = f.getAbsolutePath();
                    // 打印类的绝对路径
                    System.out.println(absoluteFile.getName() + " absolute path: " + absolutePath);

                    //绝对路径: D:\workplace\IdeaProjects\spring-demo\target\classes\com\linkuan\service\UserService.class
                    // classLoader.loadClass(绝对路径的包名)
                    // 所以需要截取 com\linkuan\service\UserService 并且转化成 com.linkuan.service.UserService
                    String classPath = absolutePath.substring(absolutePath.indexOf("com"), absolutePath.lastIndexOf(".class"));

                    // 打印相对路径
                    System.out.println(absoluteFile.getName() + " relative path: " + absolutePath);
                    classPath = classPath.replace("\\", ".");
                    System.out.println(absoluteFile.getName() + " relative path: " + absolutePath);
                    try {
                        Class<?> clzz = classLoader.loadClass(classPath);// com.linkuan.service.UserService

                        if (clzz.isAnnotationPresent(Component.class)) {
                            Component componentAnnotation = clzz.getAnnotation(Component.class);
                            String beanName = componentAnnotation.value();

                            if (null == beanName || beanName.length() == 0) {
                                // jvm默认的方法 --> 获取类名的小写
                                beanName = Introspector.decapitalize(clzz.getSimpleName());
                            }

                            // 该类是否实现了增强接口
                            if (BeanPostProcessor.class.isAssignableFrom(clzz)) {
                                BeanPostProcessor beanPostProcessor = (BeanPostProcessor) clzz.getConstructor().newInstance();
                                beanPostProcessors.add(beanPostProcessor);
                            }

                            BeanDefinition beanDefinition = new BeanDefinition();
                            beanDefinition.setType(clzz);
                            beanDefinition.setBeanName(beanName);

                            if (clzz.isAnnotationPresent(Scope.class)) {
                                beanDefinition.setScope(clzz.getAnnotation(Scope.class).value());
                            }

                            beanDefinitionMap.put(beanName, beanDefinition);
                        }
                    }catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    public Object getBean(String beanName) {
        if (!beanDefinitionMap.containsKey(beanName)) {
            throw new NullPointerException();
        }
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        // 单例bean
        if (isSingleton(beanDefinition.getScope())) {

            Object singletonBean = singletonObjects.get(beanName);
            if (null == singletonBean) {
                singletonBean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, singletonBean);
            }
            return singletonBean;
        }
        // 原型 bean
        else {
            return createBean(beanName, beanDefinition);
        }
    }

    // userService.class --> 无参构造函数 --> 对象 --> 依赖注入(属性赋值) --> 初始化前(@PostConstruct) --> 初始化中(InitializingBean) --> 初始化后(AOP) --> 代理对象 ---> bean
    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class clz = beanDefinition.getType();
        try {
            // 无参构造函数
            Object instance = clz.getConstructor().newInstance();

            // 依赖注入
            for (Field field : clz.getDeclaredFields()) {

                field.setAccessible(true);

                if (field.isAnnotationPresent(Autowired.class)) {

                    field.set(instance, this.getBean(field.getName()));
                }
            }

            // 回调Aware
            if (instance instanceof BeanNameAware) {
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            // postProcessBeforeInitialization
            for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
                instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            }

            // 初始化中赋值 InitializingBean
            if (instance instanceof InitializingBean) {
                ((InitializingBean) instance).afterPropertiesSet();
            }

            // postProcessAfterInitialization
            for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
                instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            }


            return instance;
        }catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean isSingleton(ScopeEnum scope) {
        return scope == ScopeEnum.singleton;
    }
}
