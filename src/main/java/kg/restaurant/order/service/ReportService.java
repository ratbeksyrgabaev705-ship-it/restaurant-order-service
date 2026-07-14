package kg.restaurant.order.service;

import kg.restaurant.order.model.CustomerOrder;
import kg.restaurant.order.repository.CustomerOrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ReportService {

    private static final ZoneId BISHKEK_ZONE = ZoneId.of("Asia/Bishkek");

    private final CustomerOrderRepository repository;

    public ReportService(CustomerOrderRepository repository) {
        this.repository = repository;
    }

    public Map<String, Object> buildDailyReport(LocalDate date) {
        List<CustomerOrder> orders = findDeliveredOrdersForDate(date);
        return buildReportMap(orders, date.toString());
    }

    public Map<String, Object> buildTodayReport() {
        return buildDailyReport(LocalDate.now(BISHKEK_ZONE));
    }

    public Map<String, Object> buildMonthlyReport(int year, int month) {
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

        List<CustomerOrder> monthOrders = new ArrayList<>();
        List<Map<String, Object>> dailyReports = new ArrayList<>();

        for (LocalDate day = firstDay; !day.isAfter(lastDay); day = day.plusDays(1)) {
            List<CustomerOrder> dayOrders = findDeliveredOrdersForDate(day);
            monthOrders.addAll(dayOrders);

            Map<String, Object> dayReport = buildReportMap(dayOrders, day.toString());
            dailyReports.add(Map.of(
                    "date", day.toString(),
                    "totalOrders", dayReport.get("totalOrders"),
                    "totalQuantity", dayReport.get("totalQuantity"),
                    "totalRevenue", dayReport.get("totalRevenue")
            ));
        }

        Map<String, Object> report = buildReportMap(monthOrders, firstDay.toString().substring(0, 7));
        report.put("year", year);
        report.put("monthName", Month.of(month)
                .getDisplayName(TextStyle.FULL_STANDALONE, new Locale("ru")));
        report.put("dailyReports", dailyReports);
        report.remove("orders");
        return report;
    }

    public List<CustomerOrder> findOrdersForDate(LocalDate date) {
        return findDeliveredOrdersForDate(date);
    }

    private List<CustomerOrder> findDeliveredOrdersForDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return repository
                .findByOrderStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(
                        "DELIVERED",
                        start,
                        end
                );
    }

    private Map<String, Object> buildReportMap(List<CustomerOrder> orders, String dateLabel) {
        int totalQuantity = 0;
        double totalRevenue = 0;
        Map<String, Integer> soldItems = new LinkedHashMap<>();

        for (CustomerOrder order : orders) {
            totalQuantity += order.getQuantity() == null ? 0 : order.getQuantity();
            totalRevenue += order.getTotalPrice() == null ? 0 : order.getTotalPrice();
            addSoldItems(soldItems, order.getItemName(), order.getQuantity());
        }

        String topItem = soldItems.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        Map<String, Object> report = new HashMap<>();
        report.put("date", dateLabel);
        report.put("totalOrders", orders.size());
        report.put("totalQuantity", totalQuantity);
        report.put("totalRevenue", totalRevenue);
        report.put("averageOrderAmount", orders.isEmpty() ? 0 : totalRevenue / orders.size());
        report.put("topItem", topItem);
        report.put("soldItems", soldItems);
        report.put("orders", orders);
        return report;
    }

    private void addSoldItems(Map<String, Integer> soldItems, String itemName, Integer quantity) {
        if (itemName == null || itemName.isBlank()) {
            return;
        }

        for (String part : itemName.split(",")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            String name = trimmed;
            int itemQuantity = quantity == null ? 1 : quantity;

            int xIndex = trimmed.toLowerCase().lastIndexOf(" x");
            if (xIndex > 0) {
                name = trimmed.substring(0, xIndex).trim();
                try {
                    itemQuantity = Integer.parseInt(trimmed.substring(xIndex + 2).trim());
                } catch (NumberFormatException ignored) {
                    itemQuantity = quantity == null ? 1 : quantity;
                }
            }

            soldItems.merge(name, itemQuantity, Integer::sum);
        }
    }
}
