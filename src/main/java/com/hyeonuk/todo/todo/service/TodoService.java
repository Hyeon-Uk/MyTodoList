package com.hyeonuk.todo.todo.service;

import com.hyeonuk.todo.integ.exception.NotFoundException;
import com.hyeonuk.todo.integ.exception.ValidationException;
import com.hyeonuk.todo.member.exception.UserInfoNotFoundException;
import com.hyeonuk.todo.todo.dto.CategorySaveDTO;
import com.hyeonuk.todo.todo.dto.TodoSaveDTO;
import com.hyeonuk.todo.todo.dto.TodoToggleDTO;
import com.hyeonuk.todo.todo.dto.TodoUpdateDTO;
import com.hyeonuk.todo.todo.exception.TodoException;

public interface TodoService {

    TodoSaveDTO.Response save(TodoSaveDTO.Request dto) throws TodoException, ValidationException, UserInfoNotFoundException, NotFoundException;

    TodoUpdateDTO.Response update(TodoUpdateDTO.Request dto);

    TodoToggleDTO.Response toggle(TodoToggleDTO.Request dto);
}
