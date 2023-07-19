package com.hyeonuk.todo.integ.config;

import com.hyeonuk.todo.integ.filter.xss.XssFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AppConfig {
    private final XssFilter xssFilter;

    @Bean
    public FilterRegistrationBean<XssFilter> filterRegistrationBean(){
        FilterRegistrationBean<XssFilter> xssFilterRegistrationBean = new FilterRegistrationBean();
        xssFilterRegistrationBean.setFilter(this.xssFilter);
        xssFilterRegistrationBean.setOrder(1);
        xssFilterRegistrationBean.addUrlPatterns("/*");
        xssFilterRegistrationBean.setName("xss-filter");
        return xssFilterRegistrationBean;
    }
}
