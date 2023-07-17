package com.hyeonuk.todo.todo.service;

import com.hyeonuk.todo.integ.exception.NotFoundException;
import com.hyeonuk.todo.integ.exception.ValidationException;
import com.hyeonuk.todo.member.exception.UserInfoNotFoundException;
import com.hyeonuk.todo.todo.dto.*;
import com.hyeonuk.todo.todo.exception.TodoException;

public interface TodoService {

    TodoSaveDTO.Response save(TodoSaveDTO.Request dto) throws TodoException, ValidationException, UserInfoNotFoundException, NotFoundException;

    TodoUpdateDTO.Response update(TodoUpdateDTO.Request dto) throws ValidationException, UserInfoNotFoundException, TodoException, NotFoundException;

    TodoToggleDTO.Response toggle(TodoToggleDTO.Request dto) throws TodoException, ValidationException, UserInfoNotFoundException, NotFoundException;

    TodoDeleteDTO.Response delete(TodoDeleteDTO.Request dto) throws ValidationException, UserInfoNotFoundException, NotFoundException, TodoException;
}
