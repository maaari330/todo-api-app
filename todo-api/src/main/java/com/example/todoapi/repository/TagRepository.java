package com.example.todoapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.todoapi.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> { }