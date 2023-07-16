package com.hyeonuk.todo.todo.repository;

import com.hyeonuk.todo.member.entity.Member;
import com.hyeonuk.todo.todo.entity.Category;
import com.hyeonuk.todo.todo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TodoRepository extends JpaRepository<Todo,Long> {
    @Query("select t from Todo t " +
            "left join fetch t.member m " +
            "left join fetch t.category c " +
            "where m.id = :memberId " +
            "order by t.complete asc, t.createdAt desc")
    List<Todo> findTodosWithCategoriesByMemberId(@Param("memberId") String memberId);

    @Query("delete from Todo t where t.category = :category")
    void deleteAllByCategory(@Param("category")Category category);
}
