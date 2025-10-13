package in.nikhilsh.expensetracker.service;

import in.nikhilsh.expensetracker.dto.IncomeDTO;
import in.nikhilsh.expensetracker.entity.Category;
import in.nikhilsh.expensetracker.entity.Income;
import in.nikhilsh.expensetracker.entity.Profile;
import in.nikhilsh.expensetracker.repository.CategoryRepository;
import in.nikhilsh.expensetracker.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final IncomeRepository incomeRepository;

    private final CategoryRepository categoryRepository;

    private final ProfileService profileService;

    public IncomeDTO addIncome(IncomeDTO incomeDTO){
        Profile currentProfile = profileService.getCurrentProfile();
        Category category = categoryRepository.findById(incomeDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        Income newIncome = toEntity(incomeDTO, category, currentProfile);
        newIncome = incomeRepository.save(newIncome);
        return toDTO(newIncome);
    }

    // get all incomes for the current profile
    public List<IncomeDTO> getIncomesForCurrentProfile(){
        Profile currentProfile = profileService.getCurrentProfile();
        List<Income> incomesList = incomeRepository.findByProfileIdOrderByDateDesc(currentProfile.getId());
        return incomesList.stream().map(this::toDTO).toList();
    }

    // get incomes for current profile based on start date and end date
    public List<IncomeDTO> getIncomesForCurrentProfileInDateRange(LocalDate startDate, LocalDate endDate){
        Profile currentProfile = profileService.getCurrentProfile();
        List<Income> incomesList = incomeRepository.findByProfileIdAndDateBetween(currentProfile.getId(), startDate, endDate);
        return incomesList.stream().map(this::toDTO).toList();
    }

    // delete income by id for the current profile
    public void deleteIncomeForCurrentProfile(Long incomeId){
        Profile currentProfile = profileService.getCurrentProfile();
        Income income = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new RuntimeException("No such income found."));

        if(!income.getProfile().getId().equals(currentProfile.getId())) {
            throw new RuntimeException("Unauthorized delete operation not allowed.");
        }
        incomeRepository.delete(income);
    }

    // get top 5 incomes for the current profile
    public List<IncomeDTO> getTopFiveIncomesForCurrentProfile(){
        Profile currentProfile = profileService.getCurrentProfile();
        List<Income> incomeList = incomeRepository.findTop5ByProfileIdOrderByDateDesc(currentProfile.getId());
        return incomeList.stream().map(this::toDTO).toList();
    }

    // get total income for the current profile
    public BigDecimal getTotalIncomeForCurrentProfile(){
        Profile currentProfile = profileService.getCurrentProfile();
        BigDecimal total = incomeRepository.findTotalIncomeByProfileId(currentProfile.getId());
        return total != null ? total : BigDecimal.ZERO;
    }

    // filter incomes
    public List<IncomeDTO> filterIncomes(LocalDate startDate, LocalDate endDate, String keyword, Sort sort){
        Profile profile = profileService.getCurrentProfile();
        List<Income> incomeList = incomeRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
                profile.getId(),
                startDate,
                endDate,
                keyword,
                sort
        );
        return incomeList.stream().map(this::toDTO).toList();
    }

    // notifications
    public List<IncomeDTO> getIncomesForUserOnDate(Long profileId, LocalDate date){
        List<Income> incomeList = incomeRepository.findByProfileIdAndDate(profileId, date);
        return incomeList.stream().map(this::toDTO).toList();
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
