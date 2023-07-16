package com.hyeonuk.todo.todo.service;

import com.hyeonuk.todo.todo.dto.CategorySaveDTO;
import com.hyeonuk.todo.todo.dto.TodoSaveDTO;
import com.hyeonuk.todo.todo.dto.TodoToggleDTO;
import com.hyeonuk.todo.todo.dto.TodoUpdateDTO;

public interface TodoService {

    TodoSaveDTO.Response todoSave(TodoSaveDTO.Request dto);

    TodoUpdateDTO.Response todoUpdate(TodoUpdateDTO.Request dto);

    TodoToggleDTO.Response todoToggle(TodoToggleDTO.Request dto);
}
