package com.tank.springboot.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@PropertySource(value = "classpath:person.properties")
@Component
@ConfigurationProperties(prefix = "person")
public class Person {

    @Value("${person.name}")
    private String name;
    @Value("${person.age}")
    private int age;


    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
