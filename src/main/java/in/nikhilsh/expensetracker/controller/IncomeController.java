package in.nikhilsh.expensetracker.controller;

import in.nikhilsh.expensetracker.dto.IncomeDTO;
import in.nikhilsh.expensetracker.service.IncomeService;
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
@RequestMapping("/incomes")
public class IncomeController {

    private final IncomeService incomeService;

    @PostMapping("/add")
    public ResponseEntity<?> createIncome(@RequestBody IncomeDTO incomeDTO){
        try{
            IncomeDTO newIncome = incomeService.addIncome(incomeDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(newIncome);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", "Category not found")
            );
        }
    }

    @GetMapping
    public ResponseEntity<List<IncomeDTO>> getAllIncomesForCurrentProfile(){
        List<IncomeDTO> allIncomes = incomeService.getIncomesForCurrentProfile();
        return ResponseEntity.ok(allIncomes);
    }

    @GetMapping(params = {"startDate", "endDate"})
    public ResponseEntity<List<IncomeDTO>> getIncomesForCurrentProfileInDateRange(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate){
        List<IncomeDTO> allIncomesInDateRange = incomeService.getIncomesForCurrentProfileInDateRange(startDate, endDate);
        return ResponseEntity.ok(allIncomesInDateRange);
    }

    @DeleteMapping("/{incomeId}")
    public ResponseEntity<?> deleteIncomeForCurrentProfile(@PathVariable Long incomeId){
        try{
            incomeService.deleteIncomeForCurrentProfile(incomeId);
            return ResponseEntity.ok(
                    Map.of("message","Income deleted successfully",
                            "status", true)
            );
        } catch (Exception e){
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/top5")
    public ResponseEntity<List<IncomeDTO>> getTopFiveIncomesForCurrentProfile(){
        List<IncomeDTO> incomeList = incomeService.getTopFiveIncomesForCurrentProfile();
        return ResponseEntity.ok(incomeList);
    }

    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getTotalIncomeForCurrentProfile(){
        BigDecimal totalIncome = incomeService.getTotalIncomeForCurrentProfile();
        return ResponseEntity.ok(totalIncome);
    }
}
