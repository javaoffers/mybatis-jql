package com.javaoffers.brief.modelhelper.jdbc.spring;

import com.javaoffers.brief.modelhelper.constants.ConfigPropertiesConstants;
import com.javaoffers.brief.modelhelper.context.SmartBriefContext;
import com.javaoffers.brief.modelhelper.context.SmartBriefProperties;
import com.javaoffers.brief.modelhelper.exception.BriefException;
import com.javaoffers.brief.modelhelper.interceptor.JqlInterceptor;
import com.javaoffers.brief.modelhelper.jdbc.BriefTransaction;
import com.javaoffers.brief.modelhelper.mapper.BriefMapper;
import com.javaoffers.brief.modelhelper.utils.BriefUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * spring brief context
 */
public class SpringBriefContext extends SmartBriefContext {

    private ConfigurableListableBeanFactory beanFactory;

    public SpringBriefContext(ConfigurableListableBeanFactory beanFactory) {
        super(beanFactory.getBean(DataSource.class), null);
        this.beanFactory = beanFactory;
    }

    @Override
    public void fresh() {
        parseSpringConfig();
        super.fresh();
    }

    /**
     * Parse the configuration in the spring environment and support brief
     */
    private void parseSpringConfig() {
        try {
            // {@code ConfigPropertiesConstants}
            SmartBriefProperties briefProperties = this.getBriefProperties();
            Environment environment = beanFactory.getBean(Environment.class);
            Field[] declaredFields = ConfigPropertiesConstants.class.getDeclaredFields();
            for(Field field : declaredFields){
                field.setAccessible(true);
                String property = (String)field.get(null);
                String propertyTmp = property.replaceAll(":", ".");
                String value = environment.getProperty(propertyTmp, "");
                if(StringUtils.isNotBlank(value)){
                    briefProperties.put(property, value);
                }
            }
            //spring-jdbc-factory
            briefProperties.setJdbcExecutorFactory(SpringJdbcExecutorFactory.class.getName());
        }catch (Exception e){
            e.printStackTrace();
            throw new BriefException(e.getMessage());
        }
    }

    @Override
    protected void initLoader() {
        //加载内部的,brief本身的
        super.initLoader();

        //加载spring容器中的JqlInterceptor
        String[] beanNamesForType = beanFactory.getBeanNamesForType(JqlInterceptor.class);
        if(beanNamesForType==null || beanNamesForType.length == 0){
            for(String beanName : beanNamesForType){
                this.getJqlInterceptors().add(beanFactory.getBean(beanName,JqlInterceptor.class));
            }
        }


    }

    /**
     * 事务由spring控制
     */
    @Override
    public BriefTransaction getBriefTransaction() {
        throw new UnsupportedOperationException(" Transactions are controlled by Spring");
    }

    @Override
    public BriefMapper getBriefMapper(Class briefMapperClass) {
        BriefMapper briefMapper = super.getBriefMapper(briefMapperClass);
        if(briefMapper == null){
            super.getCacheMapper().putIfAbsent(briefMapperClass,BriefUtils.newCrudMapper(briefMapperClass));
            briefMapper = super.getBriefMapper(briefMapperClass);
        }
        return briefMapper;
    }
}