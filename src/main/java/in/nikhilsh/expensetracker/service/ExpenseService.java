package in.nikhilsh.expensetracker.service;

import in.nikhilsh.expensetracker.dto.ExpenseDTO;
import in.nikhilsh.expensetracker.entity.Category;
import in.nikhilsh.expensetracker.entity.Expense;
import in.nikhilsh.expensetracker.entity.Profile;
import in.nikhilsh.expensetracker.repository.CategoryRepository;
import in.nikhilsh.expensetracker.repository.ExpenseRepository;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    private final CategoryRepository categoryRepository;

    private final ProfileService profileService;

    public ExpenseDTO addExpense(ExpenseDTO expenseDTO){
        Profile currentProfile = profileService.getCurrentProfile();
        Category category = categoryRepository.findById(expenseDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        Expense newExpense = toEntity(expenseDTO, category, currentProfile);
        newExpense = expenseRepository.save(newExpense);
        return toDTO(newExpense);
    }

    // get all expenses for the current profile
    public List<ExpenseDTO> getExpensesForCurrentProfile(){
        Profile currentProfile = profileService.getCurrentProfile();
        List<Expense> expenseList = expenseRepository.findByProfileIdOrderByDateDesc(currentProfile.getId());
        return expenseList.stream().map(this::toDTO).toList();
    }

    // get expenses for the current profile in the given date range
    public List<ExpenseDTO> getExpensesForCurrentProfileInDateRange(LocalDate startDate, LocalDate endDate){
        Profile currentProfile = profileService.getCurrentProfile();
        List<Expense> expensesList = expenseRepository.findByProfileIdAndDateBetween(currentProfile.getId(), startDate, endDate);
        return expensesList.stream().map(this::toDTO).toList();
    }

    // delete expense by id for the current profile
    public void deleteExpenseForCurrentProfile(Long expenseId) {
        Profile currentProfile = profileService.getCurrentProfile();
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("No such expense found."));

        if(!expense.getProfile().getId().equals(currentProfile.getId())) {
            throw new RuntimeException("Unauthorized delete operation not allowed.");
        }
        expenseRepository.delete(expense);
    }

    // get top 5 expenses for the current profile
    public List<ExpenseDTO> getTopFiveExpensesForCurrentProfile(){
        Profile currentProfile = profileService.getCurrentProfile();
        List<Expense> expenseList = expenseRepository.findTop5ByProfileIdOrderByDateDesc(currentProfile.getId());
        return expenseList.stream().map(this::toDTO).toList();
    }

    // get total expense for the current profile
    public BigDecimal getTotalExpenseForCurrentProfile(){
        Profile currentProfile = profileService.getCurrentProfile();
        BigDecimal total = expenseRepository.findTotalExpenseByProfileId(currentProfile.getId());
        return total != null ? total : BigDecimal.ZERO;
    }

    // filter expenses
    public List<ExpenseDTO> filterExpenses(LocalDate startDate, LocalDate endDate, String keyword, Sort sort){
        Profile profile = profileService.getCurrentProfile();
        List<Expense> expenseList = expenseRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
                profile.getId(), startDate, endDate, keyword, sort
        );
        return expenseList.stream().map(this::toDTO).toList();
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
