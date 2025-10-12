package in.nikhilsh.expensetracker.service;

import in.nikhilsh.expensetracker.dto.ExpenseDTO;
import in.nikhilsh.expensetracker.entity.Category;
import in.nikhilsh.expensetracker.entity.Expense;
import in.nikhilsh.expensetracker.entity.Profile;
import in.nikhilsh.expensetracker.repository.CategoryRepository;
import in.nikhilsh.expensetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    private final CategoryRepository categoryRepository;

    private final ProfileService profileService;

    public ExpenseDTO addExpense(ExpenseDTO expenseDTO){
        Profile currentProfile = profileService.getCurrentProfile();
        Category category = categoryRepository.findById(expenseDTO.getId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        Expense newExpense = toEntity(expenseDTO, category, currentProfile);
        newExpense = expenseRepository.save(newExpense);
        return toDTO(newExpense);
    }

    // helper methods
    public ExpenseDTO toDTO(Expense expenseEntity){
        return ExpenseDTO.builder()
                .id(expenseEntity.getId())
                .name(expenseEntity.getName())
                .icon(expenseEntity.getIcon())
                .categoryName(expenseEntity.getCategory().getName() != null ? expenseEntity.getCategory().getName() : "N/A")
                .categoryId(expenseEntity.getCategory().getId() != null ? expenseEntity.getCategory().getId() : null)
                .amount(expenseEntity.getAmount())
                .date(expenseEntity.getDate())
                .createdAt(expenseEntity.getCreatedAt())
                .updatedAt(expenseEntity.getUpdatedAt())
                .build();
    }

    public Expense toEntity(ExpenseDTO expenseDTO, Category category, Profile profile){
        return Expense.builder()
                .id(expenseDTO.getId())
                .name(expenseDTO.getName())
                .icon(expenseDTO.getIcon())
                .amount(expenseDTO.getAmount())
                .createdAt(expenseDTO.getCreatedAt())
                .updatedAt(expenseDTO.getUpdatedAt())
                .date(expenseDTO.getDate())
                .profile(profile)
                .category(category)
                .build();
    }
}
