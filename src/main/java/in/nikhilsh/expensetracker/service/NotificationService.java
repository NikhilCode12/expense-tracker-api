package in.nikhilsh.expensetracker.service;

import in.nikhilsh.expensetracker.dto.ExpenseDTO;
import in.nikhilsh.expensetracker.entity.Profile;
import in.nikhilsh.expensetracker.repository.ProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final ProfileRepository profileRepository;

    private final EmailService emailService;

    private final ExpenseService expenseService;

    @Value("${expense.tracker.frontend.url}")
    private String frontendUrl;

    @Scheduled(cron = "0 0 22 * * *", zone = "IST")
    public void sendDailyIncomeExpenseReminder(){
        log.info("Job started: sendDailyIncomeExpenseReminder()");
        List<Profile> allProfiles = profileRepository.findAll();

        allProfiles.forEach(profile -> {
            String subject = "Daily Reminder: Log Your Incomes & Expenses";

            String body = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; color: #333; line-height: 1.6; margin: 0; padding: 0;">
                    <div style="max-width: 600px; margin: 40px auto; padding: 20px; border: 1px solid #eee; border-radius: 8px;">
                        <h2 style="text-align: center; color: #333;">Hi %s,</h2>
            
                        <p>This is your friendly reminder from <strong>Expense Tracker</strong>.</p>
                        <p>Please log today's incomes and expenses before <strong>midnight</strong> to keep your daily tracking accurate and up to date.</p>
            
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s"
                               style="background-color: #4CAF50; color: #ffffff; text-decoration: none; 
                                      padding: 12px 24px; border-radius: 6px; font-weight: 600; 
                                      font-size: 16px; display: inline-block;">
                                Log Your Entries Now
                            </a>
                        </div>
            
                        <p>Stay on top of your finances and reach your savings goals faster.</p>
            
                        <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                        <p style="font-size: 12px; color: #888; text-align: center;">Expense Tracker © 2025</p>
                    </div>
                </body>
                </html>
            """, profile.getFullName(), frontendUrl);

            emailService.sendMail(profile.getEmail(), subject, body);
            log.info("Reminder email sent to: {}", profile.getEmail());
            log.info("Job completed: sendDailyIncomeExpenseRemainder()");
        });
    }

    @Scheduled(cron = "0 0 23 * * *", zone = "IST")
    public void sendDailyExpenseSummary(){
        log.info("Job started: sendDailyExpenseSummary()");
        List<Profile> allProfiles = profileRepository.findAll();
        allProfiles.forEach(profile -> {
            List<ExpenseDTO> todayExpenses = expenseService.getExpensesForUserOnDate(profile.getId(), LocalDate.now(ZoneId.of("Asia/Kolkata")));
            if(!todayExpenses.isEmpty()){
                StringBuilder table = new StringBuilder();
                table.append("<table style='border-collapse: collapse; width: 100%;'>")
                        .append("<thead style='background-color: #f2f2f2;'>")
                        .append("<tr>")
                        .append("<th style='border: 1px solid #ddd; padding: 8px;'>Category</th>")
                        .append("<th style='border: 1px solid #ddd; padding: 8px;'>Name</th>")
                        .append("<th style='border: 1px solid #ddd; padding: 8px;'>Amount</th>")
                        .append("</tr>")
                        .append("</thead>")
                        .append("<tbody>");

                BigDecimal totalAmount = BigDecimal.ZERO;

                for (ExpenseDTO expense : todayExpenses) {
                    table.append("<tr>")
                            .append("<td style='border: 1px solid #ddd; padding: 8px;'>").append(expense.getCategoryName()).append("</td>")
                            .append("<td style='border: 1px solid #ddd; padding: 8px;'>").append(expense.getName()).append("</td>")
                            .append("<td style='border: 1px solid #ddd; padding: 8px;'>").append(expense.getAmount()).append("</td>")
                            .append("</tr>");

                    totalAmount = totalAmount.add(expense.getAmount());
                }

                table.append("<tr style='font-weight: bold;'>")
                        .append("<td colspan='3' style='border: 1px solid #ddd; padding: 8px;'>Total</td>")
                        .append("<td style='border: 1px solid #ddd; padding: 8px;'>").append(totalAmount).append("</td>")
                        .append("</tr>")
                        .append("</tbody>")
                        .append("</table>");

                String body = String.format("""
                <div style='font-family: Arial, sans-serif; font-size: 14px; color: #333;'>
                    <p>Hi %s,</p>
                    <p>Here’s your expense summary for today. Keep track of your spending and stay on top of your budget!</p>
                    %s
                    <p>Happy budgeting!<br/>- Expense Tracker Team</p>
                </div>
                """, profile.getFullName(), table);

                emailService.sendMail(profile.getEmail(), "Daily Expense Summary", body);
            }
        });

        log.info("Job finished: sendDailyExpenseSummary()");
    }

}
