package com.hyeonuk.todo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MyTodoListApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyTodoListApplication.class, args);
    }

}
