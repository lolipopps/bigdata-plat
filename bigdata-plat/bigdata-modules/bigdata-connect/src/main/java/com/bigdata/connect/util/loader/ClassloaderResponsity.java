package com.bigdata.connect.util.loader;


import com.bigdata.connect.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ClassloaderResponsity {


    private ClassloaderResponsity() {
    }


    private final Map<String, ModuleClassLoader> responsityMap = new ConcurrentHashMap<>();


    public void addClassLoader(String moduleName, ModuleClassLoader moduleClassLoader) {
        responsityMap.put(moduleName, moduleClassLoader);
    }

    public boolean containsClassLoader(String key) {
        return responsityMap.containsKey(key);
    }

    public ModuleClassLoader getClassLoader(String key) {
        return responsityMap.get(key);
    }

    public void removeClassLoader(String moduleName) {
        ModuleClassLoader moduleClassLoader = responsityMap.get(moduleName);
        try {
            List<String> registeredBean = moduleClassLoader.getRegisteredBean();

            for (String beanName : registeredBean) {
                log.info("删除bean:" + beanName);
                SpringContextUtil.getBeanFactory().removeBeanDefinition(beanName);
            }

            moduleClassLoader.close();
            responsityMap.remove(moduleName);

        } catch (IOException e) {
            log.error("删除" + moduleName + "模块发生错误");
        }
    }


    private static class ClassloaderResponsityHodler {
        private static final ClassloaderResponsity instamce = new ClassloaderResponsity();
    }

    public static ClassloaderResponsity getInstance() {
        return ClassloaderResponsityHodler.instamce;
    }
}
