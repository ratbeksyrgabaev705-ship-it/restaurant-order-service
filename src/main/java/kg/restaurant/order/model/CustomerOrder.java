package kg.restaurant.order.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
public class CustomerOrder {

    private static final ZoneId BISHKEK_ZONE =
            ZoneId.of("Asia/Bishkek");


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String customerName;


    private String phone;


    private String address;


    private String comment;


    /*
     * Азырынча заказдагы бардык тамактар
     * бир текст катары сакталат:
     *
     * "Лагман x2, Плов x1"
     */
    @Column(length = 3000)
    private String itemName;


    private Double quantity;


    private Double totalPrice;


    private Double paymentAmount;


    private String receiptImagePath;


    /*
     * Төлөмдүн абалы
     *
     * WAITING_PAYMENT - төлөм күтүлүүдө
     * PAID - төлөндү
     */
    private String paymentStatus = "WAITING_PAYMENT";


    /*
     * Заказдын абалы
     *
     * NEW - жаңы заказ
     * ACCEPTED - кабыл алынды
     * READY - даяр
     * COMPLETED - бүттү
     */
    private String orderStatus = "NEW";


    /*
     * Заказ түзүлгөн убакыт
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;



    public CustomerOrder() {

    }



    @PrePersist
    public void beforeSave() {


        if (createdAt == null) {

            createdAt =
                    LocalDateTime.now(BISHKEK_ZONE);
        }


        if (
                paymentStatus == null
                        || paymentStatus.isBlank()
        ) {

            paymentStatus =
                    "WAITING_PAYMENT";
        }



        if (
                orderStatus == null
                        || orderStatus.isBlank()
        ) {

            orderStatus =
                    "NEW";
        }



        if (quantity == null) {

            quantity = 0;
        }



        if (totalPrice == null) {

            totalPrice = 0.0;
        }



        if (paymentAmount == null) {

            paymentAmount = totalPrice;
        }

    }




    public Long getId() {

        return id;
    }



    public String getCustomerName() {

        return customerName;
    }



    public void setCustomerName(
            String customerName
    ) {

        this.customerName = customerName;
    }




    public String getPhone() {

        return phone;
    }



    public void setPhone(
            String phone
    ) {

        this.phone = phone;
    }




    public String getAddress() {

        return address;
    }



    public void setAddress(
            String address
    ) {

        this.address = address;
    }




    public String getComment() {

        return comment;
    }



    public void setComment(
            String comment
    ) {

        this.comment = comment;
    }




    public String getItemName() {

        return itemName;
    }



    public void setItemName(
            String itemName
    ) {

        this.itemName = itemName;
    }




    public Double getQuantity() {

        return quantity;
    }



    public void setQuantity(
            Double quantity
    ) {

        this.quantity = quantity;
    }




    public Double getTotalPrice() {

        return totalPrice;
    }



    public void setTotalPrice(
            Double totalPrice
    ) {

        this.totalPrice = totalPrice;
    }




    public Double getPaymentAmount() {

        return paymentAmount;
    }



    public void setPaymentAmount(
            Double paymentAmount
    ) {

        this.paymentAmount = paymentAmount;
    }




    public String getReceiptImagePath() {

        return receiptImagePath;
    }



    public void setReceiptImagePath(
            String receiptImagePath
    ) {

        this.receiptImagePath = receiptImagePath;
    }




    public String getPaymentStatus() {

        return paymentStatus;
    }



    public void setPaymentStatus(
            String paymentStatus
    ) {

        this.paymentStatus = paymentStatus;
    }




    public String getOrderStatus() {

        return orderStatus;
    }



    public void setOrderStatus(
            String orderStatus
    ) {

        this.orderStatus = orderStatus;
    }




    public LocalDateTime getCreatedAt() {

        return createdAt;
    }



    public void setCreatedAt(
            LocalDateTime createdAt
    ) {

        this.createdAt = createdAt;
    }

}