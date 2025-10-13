package in.nikhilsh.expensetracker.service;

import in.nikhilsh.expensetracker.dto.ExpenseDTO;
import in.nikhilsh.expensetracker.dto.IncomeDTO;
import in.nikhilsh.expensetracker.dto.RecentTransactionDTO;
import in.nikhilsh.expensetracker.entity.Profile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ExpenseService expenseService;

    private final IncomeService incomeService;

    private final ProfileService profileService;

    public Map<String, Object> getDashboardData(){
        Profile currentProfile = profileService.getCurrentProfile();
        Map<String, Object> dashboardData = new LinkedHashMap<>();
        List<IncomeDTO> topIncomesList = incomeService.getTopFiveIncomesForCurrentProfile();
        List<ExpenseDTO> topExpenseList = expenseService.getTopFiveExpensesForCurrentProfile();

        List<RecentTransactionDTO> recentTransactionsList = concat(
                topIncomesList.stream().map(income ->
                        RecentTransactionDTO.builder()
                                .id(income.getId())
                                .profileId(currentProfile.getId())
                                .icon(income.getIcon())
                                .name(income.getName())
                                .amount(income.getAmount())
                                .date(income.getDate())
                                .createdAt(income.getCreatedAt())
                                .updatedAt(income.getUpdatedAt())
                                .type("income")
                                .build()),
                topIncomesList.stream().map(expense ->
                        RecentTransactionDTO.builder()
                                .id(expense.getId())
                                .profileId(currentProfile.getId())
                                .icon(expense.getIcon())
                                .name(expense.getName())
                                .amount(expense.getAmount())
                                .date(expense.getDate())
                                .createdAt(expense.getCreatedAt())
                                .updatedAt(expense.getUpdatedAt())
                                .type("expense")
                                .build())
                        .sorted((a,b)->{
                            int cmp = b.getDate().compareTo(a.getDate());
                            if(cmp == 0 && a.getCreatedAt()!=null && b.getCreatedAt()!=null){
                                return b.getCreatedAt().compareTo(a.getCreatedAt());
                            }
                            return cmp;
                        })
        ).toList();

        dashboardData.put("totalBalance",
                incomeService.getTotalIncomeForCurrentProfile()
                        .subtract(expenseService.getTotalExpenseForCurrentProfile()));
        dashboardData.put("totalIncome",
                incomeService.getTotalIncomeForCurrentProfile());
        dashboardData.put("totalExpense",
                expenseService.getTotalExpenseForCurrentProfile());
        dashboardData.put("recent5Expenses", topExpenseList);
        dashboardData.put("recent5Incomes", topIncomesList);
        dashboardData.put("recentTransactions", recentTransactionsList);

        return dashboardData;
    }
}
