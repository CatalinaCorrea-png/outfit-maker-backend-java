package ar.outfitmaker.service;

import ar.outfitmaker.dto.CategoryDTO;
import ar.outfitmaker.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategories() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(CategoryDTO::from)
                .toList();
    }
}
