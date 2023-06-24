package com.hyeonuk.todo.email.repository;

import com.hyeonuk.todo.email.entity.EmailAuthentication;
import org.springframework.data.repository.CrudRepository;

public interface EmailAuthenticationRepository extends CrudRepository<EmailAuthentication,String> {
}
