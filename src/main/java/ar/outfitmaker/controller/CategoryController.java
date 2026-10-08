package ar.outfitmaker.controller;

import ar.outfitmaker.dto.CategoryDTO;
import ar.outfitmaker.service.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // Las opciones del select de categoría: salen de la base, no están fijas en el front
    @GetMapping
    public List<CategoryDTO> getCategories() {
        return categoryService.getCategories();
    }
}
