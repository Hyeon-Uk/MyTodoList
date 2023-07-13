package com.hyeonuk.todo.todo.repository;

import com.hyeonuk.todo.todo.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category,Long> {
}
