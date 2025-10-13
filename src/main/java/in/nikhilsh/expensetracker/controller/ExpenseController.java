package in.nikhilsh.expensetracker.controller;

import in.nikhilsh.expensetracker.dto.ExpenseDTO;
import in.nikhilsh.expensetracker.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping("/add")
    public ResponseEntity<?> createExpense(@RequestBody ExpenseDTO expenseDTO){
        try{
            ExpenseDTO newExpense = expenseService.addExpense(expenseDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(newExpense);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", "Category not found")
            );
        }
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDTO>> getAllExpensesForCurrentProfile(){
        List<ExpenseDTO> allExpenses = expenseService.getExpensesForCurrentProfile();
        return ResponseEntity.ok(allExpenses);
    }

    @GetMapping(params = {"startDate", "endDate"})
    public ResponseEntity<List<ExpenseDTO>> getExpensesForCurrentProfileInDateRange(@RequestParam LocalDate startDate,@RequestParam LocalDate endDate){
        List<ExpenseDTO> allExpenseInDateRange = expenseService.getExpensesForCurrentProfileInDateRange(startDate, endDate);
        return ResponseEntity.ok(allExpenseInDateRange);
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<?> deleteExpenseForCurrentProfile(@PathVariable Long expenseId){
        try{
            expenseService.deleteExpenseForCurrentProfile(expenseId);
            return ResponseEntity.ok(
                        Map.of("message","Expense deleted successfully",
                                "status", true)
                );
        } catch (Exception e){
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getTotalExpenseForCurrentProfile(){
        BigDecimal totalExpense = expenseService.getTotalExpenseForCurrentProfile();
        return ResponseEntity.ok(totalExpense);
    }

    @GetMapping("/top5")
    public ResponseEntity<List<ExpenseDTO>> getTopFiveExpensesForCurrentProfile(){
        List<ExpenseDTO> expenseList = expenseService.getTopFiveExpensesForCurrentProfile();
        return ResponseEntity.ok(expenseList);
    }
}
