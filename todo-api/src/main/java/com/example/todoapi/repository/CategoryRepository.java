package com.example.todoapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.todoapi.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> { }