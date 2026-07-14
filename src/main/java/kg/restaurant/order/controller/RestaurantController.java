package kg.restaurant.order.controller;

import kg.restaurant.order.model.Restaurant;
import kg.restaurant.order.repository.RestaurantRepository;
import kg.restaurant.order.service.TelegramService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantRepository restaurantRepository;
    private final TelegramService telegramService;

    public RestaurantController(
            RestaurantRepository restaurantRepository,
            TelegramService telegramService
    ) {
        this.restaurantRepository = restaurantRepository;
        this.telegramService = telegramService;
    }

    @GetMapping
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    @GetMapping("/{id}")
    public Restaurant getRestaurantById(@PathVariable Long id) {
        return restaurantRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Restaurant createRestaurant(@RequestBody Restaurant restaurant) {
        Restaurant saved = restaurantRepository.save(restaurant);
        telegramService.sendMessage(
                "🏢 Жаңы ресторан түзүлдү!\n"
                        + "Аты: " + saved.getName() + "\n"
                        + "ID: " + saved.getId()
        );
        return saved;
    }

    @PutMapping("/{id}")
    public Restaurant updateRestaurant(
            @PathVariable Long id,
            @RequestBody Restaurant updatedRestaurant
    ) {

        Restaurant restaurant = restaurantRepository.findById(id).orElse(null);
        if (restaurant == null) {
            return null;
        }

        String oldName = restaurant.getName();
        restaurant.setName(updatedRestaurant.getName());
        Restaurant saved = restaurantRepository.save(restaurant);

        telegramService.sendMessage(
                "✏️ Ресторан маалыматы өзгөртүлдү!\n"
                        + "ID: " + saved.getId() + "\n"
                        + "Эски аты: " + oldName + "\n"
                        + "Жаңы аты: " + saved.getName()
        );
        return saved;
    }

    @DeleteMapping("/{id}")
    public void deleteRestaurant(@PathVariable Long id) {
        Restaurant restaurant = restaurantRepository.findById(id).orElse(null);
        if (restaurant != null) {
            telegramService.sendMessage(
                    "🗑️ Ресторан өчүрүлдү!\n"
                            + "Аты: " + restaurant.getName() + " (ID: " + id + ")"
            );
        }
        restaurantRepository.deleteById(id);
    }
}
