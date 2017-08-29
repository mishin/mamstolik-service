package pl.denisolek.guest.restaurant

import io.swagger.annotations.Api
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import pl.denisolek.core.config.API_BASE_PATH
import pl.denisolek.guest.restaurant.DTO.RestaurantDTO
import java.time.LocalDateTime

@Api(value = "GuestRestaurantApi")
interface GuestRestaurantApi {
    companion object {
        const val RESTAURANTS_BASE_PATH = "$API_BASE_PATH/restaurants"
    }

    @GetMapping(RESTAURANTS_BASE_PATH)
    fun searchRestaurants(@RequestParam city: String,
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam date: LocalDateTime,
                          @RequestParam peopleNumber: Int): List<RestaurantDTO>
}