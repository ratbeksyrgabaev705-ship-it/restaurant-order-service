package kg.restaurant.order.repository;

import kg.restaurant.order.model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomerOrderRepository
        extends JpaRepository<CustomerOrder, Long> {

    /*
     * Белгилүү бир күн же ай боюнча
     * заказдарды убакыт аралыгы менен алуу.
     */
    List<CustomerOrder>
    findByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(
            LocalDateTime start,
            LocalDateTime end
    );

    /*
     * Отчет үчүн аткарылган заказдар гана.
     */
    List<CustomerOrder>
    findByOrderStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(
            String orderStatus,
            LocalDateTime start,
            LocalDateTime end
    );

    /*
     * Активдүү заказдарды жаңыдан эскиге карай алуу.
     */
    List<CustomerOrder>
    findByOrderStatusNotInOrderByCreatedAtDesc(
            List<String> excludedStatuses
    );
}