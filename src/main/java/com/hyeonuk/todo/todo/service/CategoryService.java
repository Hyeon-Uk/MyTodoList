package com.hyeonuk.todo.todo.service;

import com.hyeonuk.todo.integ.exception.NotFoundException;
import com.hyeonuk.todo.integ.exception.ValidationException;
import com.hyeonuk.todo.member.exception.UserInfoNotFoundException;
import com.hyeonuk.todo.todo.dto.CategoryDeleteDTO;
import com.hyeonuk.todo.todo.dto.CategorySaveDTO;
import com.hyeonuk.todo.todo.dto.CategoryUpdateDTO;
import com.hyeonuk.todo.todo.exception.CategoryException;

public interface CategoryService {
    CategorySaveDTO.Response save(CategorySaveDTO.Request dto) throws UserInfoNotFoundException, ValidationException, CategoryException;
    CategoryDeleteDTO.Response delete(CategoryDeleteDTO.Request dto) throws UserInfoNotFoundException, NotFoundException, ValidationException, CategoryException;
    CategoryUpdateDTO.Response update(CategoryUpdateDTO.Request dto) throws ValidationException, UserInfoNotFoundException, NotFoundException, CategoryException;
}
