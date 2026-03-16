package com.example.springsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.springsecurity.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}