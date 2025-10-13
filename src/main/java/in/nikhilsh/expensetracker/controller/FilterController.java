package in.nikhilsh.expensetracker.controller;

import in.nikhilsh.expensetracker.dto.ExpenseDTO;
import in.nikhilsh.expensetracker.dto.FilterDTO;
import in.nikhilsh.expensetracker.dto.IncomeDTO;
import in.nikhilsh.expensetracker.service.ExpenseService;
import in.nikhilsh.expensetracker.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/filter")
public class FilterController {

    private final IncomeService incomeService;

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<?> filterTransactions(@RequestBody FilterDTO filter){
        LocalDate startDate = filter.getStartDate() != null ? filter.getStartDate() : LocalDate.MIN;
        LocalDate endDate = filter.getEndDate() != null ? filter.getEndDate() : LocalDate.now();
        String keyword = filter.getKeyword() != null ? filter.getKeyword() : "";
        String sortField = filter.getSortField() != null ? filter.getSortField() : "date";
        Sort.Direction direction = "desc".equalsIgnoreCase(filter.getSortOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortField);

        if("income".equalsIgnoreCase(filter.getType())){
            List<IncomeDTO> filteredIncomes = incomeService.filterIncomes(startDate, endDate, keyword, sort);
            return ResponseEntity.ok(filteredIncomes);
        } else if("expense".equalsIgnoreCase(filter.getType())) {
            List<ExpenseDTO> filteredExpenses = expenseService.filterExpenses(startDate, endDate, keyword, sort);
            return ResponseEntity.ok(filteredExpenses);
        } else{
            return ResponseEntity.badRequest().body("Invalid type. Must be income or expense.");
        }
    }
}
