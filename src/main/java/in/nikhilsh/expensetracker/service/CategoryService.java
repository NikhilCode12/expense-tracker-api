package in.nikhilsh.expensetracker.service;

import in.nikhilsh.expensetracker.dto.CategoryDTO;
import in.nikhilsh.expensetracker.dto.ProfileDTO;
import in.nikhilsh.expensetracker.entity.Category;
import in.nikhilsh.expensetracker.entity.Profile;
import in.nikhilsh.expensetracker.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ProfileService profileService;

    private final CategoryRepository categoryRepository;

    public CategoryDTO saveCategory(CategoryDTO categoryDTO){
        Profile currentProfile = profileService.getCurrentProfile();
        if(categoryRepository.existsByNameAndProfileId(categoryDTO.getName(), currentProfile.getId())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category with this name already exists.");
        }
        Category newCategory = toEntity(categoryDTO, currentProfile);
        newCategory = categoryRepository.save(newCategory);
        return toDTO(newCategory);
    }

    // get categories for current user
    public List<CategoryDTO> getCategoriesForCurrentUser(){
        Profile currentProfile = profileService.getCurrentProfile();
        List<Category> categoryList = categoryRepository.findByProfileId(currentProfile.getId());
        return categoryList.stream().map(this::toDTO).toList();
    }

    // get categories by type for current user
    public List<CategoryDTO> getCategoriesByTypeForCurrentUser(String type){
        Profile currentProfile = profileService.getCurrentProfile();
        List<Category> categoryList = categoryRepository.findByTypeAndProfileId(type, currentProfile.getId());
        return categoryList.stream().map(this::toDTO).toList();
    }

    // update category for current user
    public CategoryDTO updateCategoryForCurrentUser(Long categoryId, CategoryDTO categoryDTO){
        Profile currentProfile = profileService.getCurrentProfile();
        Category existingCategory = categoryRepository.
                findByIdAndProfileId(categoryId, currentProfile.getId())
                .orElseThrow(() -> new RuntimeException("Category not found or not accessible"));

        existingCategory.setName(categoryDTO.getName());
        existingCategory.setIcon(categoryDTO.getIcon());
        existingCategory.setType(categoryDTO.getType());
        Category savedCategory = categoryRepository.save(existingCategory);
        return toDTO(savedCategory);
    }

    // helper methods
    private Category toEntity(CategoryDTO dto, Profile profile) {
        return Category.builder()
                .id(dto.getId())
                .type(dto.getType())
                .name(dto.getName())
                .icon(dto.getIcon())
                .profile(profile)
                .build();
    }

    private CategoryDTO toDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .profileId(category.getProfile() != null ? category.getProfile().getId() : null)
                .type(category.getType())
                .name(category.getName())
                .icon(category.getIcon())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
