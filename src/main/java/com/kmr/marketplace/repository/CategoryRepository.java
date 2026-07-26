package com.kmr.marketplace.repository;

import com.kmr.marketplace.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByActiveTrueOrderByIdAsc();

    // Departments (top level) and leaf categories (shoppable), ordered for display.
    List<Category> findByActiveTrueAndParentIdIsNullOrderBySortOrderAscIdAsc();

    List<Category> findByActiveTrueAndParentIdIsNotNullOrderBySortOrderAscIdAsc();

    List<Category> findByActiveTrueAndParentIdOrderBySortOrderAscIdAsc(Long parentId);
}
