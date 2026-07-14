package kg.restaurant.order.controller;

import kg.restaurant.order.model.CustomerOrder;
import kg.restaurant.order.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
@CrossOrigin("*")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/today")
    public Map<String, Object> todayReport() {
        return reportService.buildTodayReport();
    }

    @GetMapping("/daily")
    public Map<String, Object> dailyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return reportService.buildDailyReport(date);
    }

    @GetMapping("/monthly")
    public Map<String, Object> monthlyReport(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return reportService.buildMonthlyReport(year, month);
    }

    @GetMapping("/daily-orders")
    public List<CustomerOrder> dailyOrders(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return reportService.findOrdersForDate(date);
    }
}
