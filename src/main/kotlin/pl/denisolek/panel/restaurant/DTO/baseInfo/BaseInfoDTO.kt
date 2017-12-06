package pl.denisolek.panel.restaurant.DTO.baseInfo

import org.hibernate.validator.constraints.NotBlank
import org.springframework.http.HttpStatus
import pl.denisolek.Exception.ServiceException
import pl.denisolek.core.address.Address
import pl.denisolek.core.restaurant.BusinessHour
import pl.denisolek.core.restaurant.Restaurant
import pl.denisolek.core.restaurant.SpecialDate
import pl.denisolek.panel.restaurant.DTO.SpecialDateDTO
import java.time.DayOfWeek
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

data class BaseInfoDTO(
        var settings: BaseInfoSettingsDTO,

        @field:Pattern(regexp = "^[a-z A-Z0-9&]+(-[a-z A-Z0-9&]+)?\$", message = "Name accepts a-z A-Z 0-9 - & and spaces.")
        @field:NotBlank
        var name: String,

        @field:NotBlank
        @field:Size(min = 5)
        @field:Pattern(regexp = PHONE_MATCHER)
        var phoneNumber: String,

        var type: Restaurant.RestaurantType,

        var address: AddressDTO,

        var businessHours: MutableMap<DayOfWeek, BusinessHour>,

        var specialDates: List<SpecialDateDTO>
) {
    companion object {
        internal const val PHONE_MATCHER = "(\\(?\\+[\\d]{2}\\(?)?([ .-]?)([0-9]{3})([ .-]?)([0-9]{3})\\4([0-9]{3})"

        fun mapToExistingRestaurant(restaurant: Restaurant, baseInfoDTO: BaseInfoDTO): Restaurant {
            restaurant.settings?.localization = baseInfoDTO.settings.localization
            restaurant.settings?.specialDates = baseInfoDTO.settings.specialDates
            restaurant.name = baseInfoDTO.name
            restaurant.phoneNumber = baseInfoDTO.phoneNumber
            restaurant.type = baseInfoDTO.type
            restaurant.address = getUpdatedAddress(restaurant, baseInfoDTO)

            updateSpecialDates(restaurant, baseInfoDTO)

            restaurant.businessHours.forEach {
                it.value.openTime = baseInfoDTO.businessHours[it.key]?.openTime ?: throw ServiceException(HttpStatus.BAD_REQUEST, "Invalid openTime for ${it.key}")
                it.value.closeTime = baseInfoDTO.businessHours[it.key]?.closeTime ?: throw ServiceException(HttpStatus.BAD_REQUEST, "Invalid closeTime for ${it.key}")
                it.value.isClosed = baseInfoDTO.businessHours[it.key]?.isClosed ?: throw ServiceException(HttpStatus.BAD_REQUEST, "Invalid isClosed for ${it.key}")
            }
            return restaurant
        }

        private fun getUpdatedAddress(restaurant: Restaurant, baseInfoDTO: BaseInfoDTO): Address {
            return Address(
                    id = restaurant.address.id,
                    streetName = baseInfoDTO.address.streetName,
                    buildingNumber = baseInfoDTO.address.buildingNumber,
                    postalCode = baseInfoDTO.address.postalCode,
                    latitude = baseInfoDTO.address.latitude,
                    longitude = baseInfoDTO.address.longitude
            )
        }

        private fun updateSpecialDates(restaurant: Restaurant, baseInfoDTO: BaseInfoDTO) {
            restaurant.specialDates.removeIf { specialDate ->
                !baseInfoDTO.specialDates.any { it.id == specialDate.id }
            }

            baseInfoDTO.specialDates.forEach { (id, date, businessHour) ->
                if (businessHour.closeTime.isBefore(businessHour.openTime)) throw ServiceException(HttpStatus.BAD_REQUEST, "Close time must be greater than open time.")
                restaurant.specialDates.firstOrNull { it.id == id || it.date == date }?.let {
                    it.businessHour.openTime = businessHour.openTime
                    it.businessHour.closeTime = businessHour.closeTime
                    it.businessHour.isClosed = businessHour.isClosed
                    it.date = date
                    it
                } ?: restaurant.specialDates.add(SpecialDate(
                        date = date,
                        restaurant = restaurant,
                        businessHour = BusinessHour(
                                openTime = businessHour.openTime,
                                closeTime = businessHour.closeTime,
                                isClosed = businessHour.isClosed
                        ))
                )
            }
        }
    }
}
