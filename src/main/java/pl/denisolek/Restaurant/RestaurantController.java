package pl.denisolek.Restaurant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Controller
public class RestaurantController implements RestaurantApi {

    @Autowired
    RestaurantService restaurantService;

    @Autowired
    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @Override
    public List<Restaurant> getRestaurants() {
        return restaurantService.getRestaurants();
    }

    @Override
    public Restaurant getRestaurant(@PathVariable("restaurantId") Restaurant restaurant) {
        return restaurantService.getRestaurant(restaurant);
    }

    @Override
    public Restaurant addRestaurant(@RequestBody Restaurant restaurant) {
        return restaurantService.addRestaurant(restaurant);
    }
}
