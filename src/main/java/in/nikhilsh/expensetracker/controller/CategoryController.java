package in.nikhilsh.expensetracker.controller;

import in.nikhilsh.expensetracker.dto.CategoryDTO;
import in.nikhilsh.expensetracker.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/create")
    public ResponseEntity<?> createCategory(@RequestBody CategoryDTO categoryDTO){
        try{
            CategoryDTO createdCategory = categoryService.saveCategory(categoryDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getCategories(){
        try{
            List<CategoryDTO> categoriesList = categoryService.getCategoriesForCurrentUser();
            return ResponseEntity.ok(categoriesList);
        } catch (Exception e){
            throw new RuntimeException("Some exception occurred in fetching categories: " + e.getMessage());
        }
    }

    @GetMapping("/{type}")
    public ResponseEntity<List<CategoryDTO>> getCategoriesByTypeForCurrentUser(@PathVariable String type){
        List<CategoryDTO> categories = categoryService.getCategoriesByTypeForCurrentUser(type);
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<?> updateCategory(@PathVariable Long categoryId, @RequestBody CategoryDTO categoryDTO){
        try{
            CategoryDTO category = categoryService.updateCategoryForCurrentUser(categoryId, categoryDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(category);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", e.getMessage())
            );
        }
    }

}
