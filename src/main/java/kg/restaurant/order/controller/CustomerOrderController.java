package kg.restaurant.order.controller;

import kg.restaurant.order.model.CustomerOrder;
import kg.restaurant.order.repository.CustomerOrderRepository;
import kg.restaurant.order.service.ReceiptStorageService;
import kg.restaurant.order.service.TelegramService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/orders")
@CrossOrigin("*")
public class CustomerOrderController {

    private final CustomerOrderRepository repo;
    private final ReceiptStorageService receiptStorageService;
    private final TelegramService telegramService;

    public CustomerOrderController(
            CustomerOrderRepository repo,
            ReceiptStorageService receiptStorageService,
            TelegramService telegramService
    ) {
        this.repo = repo;
        this.receiptStorageService = receiptStorageService;
        this.telegramService = telegramService;
    }

    @GetMapping
    public List<CustomerOrder> getAllOrders() {
        return repo.findAll();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomerOrder> createOrder(
            @RequestBody CustomerOrder order
    ) {
        prepareNewOrder(order);
        CustomerOrder savedOrder = repo.save(order);
        notifyNewOrder(savedOrder);
        return ResponseEntity.ok(savedOrder);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CustomerOrder> createOrderWithReceipt(
            @RequestParam String customerName,
            @RequestParam String phone,
            @RequestParam String address,
            @RequestParam(required = false) String comment,
            @RequestParam String itemName,
            @RequestParam Integer quantity,
            @RequestParam Double totalPrice,
            @RequestParam Double paymentAmount,
            @RequestParam("receipt") MultipartFile receipt
    ) throws IOException {

        CustomerOrder order = new CustomerOrder();
        order.setCustomerName(customerName);
        order.setPhone(phone);
        order.setAddress(address);
        order.setComment(comment);
        order.setItemName(itemName);
        order.setQuantity(quantity);
        order.setTotalPrice(totalPrice);
        order.setPaymentAmount(paymentAmount);

        prepareNewOrder(order);
        CustomerOrder savedOrder = repo.save(order);

        String receiptPath = receiptStorageService.saveReceipt(receipt);
        savedOrder.setReceiptImagePath(receiptPath);
        savedOrder = repo.save(savedOrder);

        notifyNewOrder(savedOrder);
        return ResponseEntity.ok(savedOrder);
    }

    @GetMapping("/active")
    public List<CustomerOrder> getActiveOrders() {
        return repo.findByOrderStatusNotInOrderByCreatedAtDesc(
                List.of("DELIVERED", "CANCELLED")
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerOrder> getOrderById(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<CustomerOrder> acceptOrder(@PathVariable Long id) {
        return updateStatus(id, "ACCEPTED");
    }

    @PutMapping("/{id}/cook")
    public ResponseEntity<CustomerOrder> cooking(@PathVariable Long id) {
        return updateStatus(id, "COOKING");
    }

    @PutMapping("/{id}/ready")
    public ResponseEntity<CustomerOrder> ready(@PathVariable Long id) {
        return updateStatus(id, "READY");
    }

    @PutMapping("/{id}/courier")
    public ResponseEntity<CustomerOrder> courier(@PathVariable Long id) {
        return updateStatus(id, "GIVEN_TO_COURIER");
    }

    @PutMapping("/{id}/deliver")
    public ResponseEntity<CustomerOrder> delivered(@PathVariable Long id) {
        CustomerOrder order = repo.findById(id).orElse(null);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        order.setOrderStatus("DELIVERED");

        if ("WAITING_PAYMENT".equals(order.getPaymentStatus())
                || "WAITING".equals(order.getPaymentStatus())) {
            order.setPaymentStatus("PAID");
        }

        return ResponseEntity.ok(repo.save(order));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<CustomerOrder> cancel(@PathVariable Long id) {
        return updateStatus(id, "CANCELLED");
    }

    private void prepareNewOrder(CustomerOrder order) {
        order.setOrderStatus("NEW");
        order.setPaymentStatus("WAITING_PAYMENT");
    }

    private void notifyNewOrder(CustomerOrder order) {
        Double amount = order.getPaymentAmount() != null
                ? order.getPaymentAmount()
                : order.getTotalPrice();

        telegramService.sendMessage(
                "🔔 ЖАҢЫ ЗАКАЗ\n\n"
                        + "№" + order.getId() + "\n"
                        + "👤 " + safe(order.getCustomerName()) + "\n"
                        + "📞 " + safe(order.getPhone()) + "\n"
                        + "💰 " + formatAmount(amount) + " сом"
        );
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "—" : value.trim();
    }

    private String formatAmount(Double amount) {
        if (amount == null) {
            return "0";
        }
        if (amount % 1 == 0) {
            return String.valueOf(amount.intValue());
        }
        return String.valueOf(amount);
    }

    private ResponseEntity<CustomerOrder> updateStatus(Long id, String status) {
        CustomerOrder order = repo.findById(id).orElse(null);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        order.setOrderStatus(status);
        return ResponseEntity.ok(repo.save(order));
    }
}
