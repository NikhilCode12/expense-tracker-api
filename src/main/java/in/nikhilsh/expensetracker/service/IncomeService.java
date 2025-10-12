package in.nikhilsh.expensetracker.service;

import in.nikhilsh.expensetracker.dto.CategoryDTO;
import in.nikhilsh.expensetracker.dto.IncomeDTO;
import in.nikhilsh.expensetracker.dto.ProfileDTO;
import in.nikhilsh.expensetracker.entity.Category;
import in.nikhilsh.expensetracker.entity.Income;
import in.nikhilsh.expensetracker.entity.Profile;
import in.nikhilsh.expensetracker.repository.CategoryRepository;
import in.nikhilsh.expensetracker.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final IncomeRepository incomeRepository;

    private final CategoryRepository categoryRepository;

    private final ProfileService profileService;

    public IncomeDTO addIncome(IncomeDTO incomeDTO){
        Profile currentProfile = profileService.getCurrentProfile();
        Category category = categoryRepository.findById(incomeDTO.getId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        Income newIncome = toEntity(incomeDTO, category, currentProfile);
        newIncome = incomeRepository.save(newIncome);
        return toDTO(newIncome);
    }

    // helper methods
    public IncomeDTO toDTO(Income incomeEntity){
        return IncomeDTO.builder()
                .id(incomeEntity.getId())
                .name(incomeEntity.getName())
                .icon(incomeEntity.getIcon())
                .categoryName(incomeEntity.getCategory().getName() != null ? incomeEntity.getCategory().getName() : "N/A")
                .categoryId(incomeEntity.getCategory().getId() != null ? incomeEntity.getCategory().getId() : null)
                .amount(incomeEntity.getAmount())
                .date(incomeEntity.getDate())
                .createdAt(incomeEntity.getCreatedAt())
                .updatedAt(incomeEntity.getUpdatedAt())
                .build();
    }

    public Income toEntity(IncomeDTO incomeDTO, Category category, Profile profile){
        return Income.builder()
                .id(incomeDTO.getId())
                .name(incomeDTO.getName())
                .icon(incomeDTO.getIcon())
                .amount(incomeDTO.getAmount())
                .createdAt(incomeDTO.getCreatedAt())
                .updatedAt(incomeDTO.getUpdatedAt())
                .date(incomeDTO.getDate())
                .profile(profile)
                .category(category)
                .build();
    }
}
