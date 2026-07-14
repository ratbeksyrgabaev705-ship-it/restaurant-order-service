package kg.restaurant.order.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/cart")
    public String cart() {
        return "cart";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/admin-menu")
    public String adminMenu() {
        return "admin-menu";
    }

    @GetMapping("/receipt")
    public String receipt() {
        return "receipt";
    }
}