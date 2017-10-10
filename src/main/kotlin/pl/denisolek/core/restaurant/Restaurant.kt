package pl.denisolek.core.restaurant

import pl.denisolek.core.address.Address
import pl.denisolek.core.menu.Menu
import pl.denisolek.core.reservation.Reservation
import pl.denisolek.core.spot.Spot
import pl.denisolek.infrastructure.DateTimeInterval
import pl.denisolek.infrastructure.isAfterOrEqual
import pl.denisolek.infrastructure.isBeforeOrEqual
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import javax.persistence.*

@Entity
data class Restaurant(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Int? = null,
        var name: String,
        var description: String,
        var avgReservationTime: Duration,
        var rate: Float,
        var service_rate: Float,
        var food_rate: Float,
        var price_quality_rate: Float,
        var isActive: Boolean,

        @OneToOne(cascade = arrayOf(CascadeType.ALL))
        var address: Address,

        @OneToMany(mappedBy = "restaurant", cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
        var reservations: MutableList<Reservation> = mutableListOf(),

        @OneToMany(mappedBy = "restaurant", cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
        var spots: MutableList<Spot> = mutableListOf(),

        @OneToOne(cascade = arrayOf(CascadeType.ALL))
        var menu: Menu,

        @ElementCollection(fetch = FetchType.EAGER)
        @Enumerated(EnumType.STRING)
        @CollectionTable(name = "restaurant_kitchen", joinColumns = arrayOf(JoinColumn(name = "restaurantId")))
        @Column(name = "kitchen_type", nullable = false)
        var cuisineTypes: MutableSet<CuisineType> = mutableSetOf(),

        @ElementCollection(fetch = FetchType.EAGER)
        @Enumerated(EnumType.STRING)
        @CollectionTable(name = "restaurant_facility", joinColumns = arrayOf(JoinColumn(name = "restaurantId")))
        @Column(name = "facility", nullable = false)
        var facilities: MutableSet<Facilities> = mutableSetOf(),

        @OneToMany(cascade = arrayOf(CascadeType.ALL))
        @JoinTable(name = "restaurant_business_hour", joinColumns = arrayOf(JoinColumn(name = "restaurant_id")), inverseJoinColumns = arrayOf(JoinColumn(name = "business_hour_id")))
        @MapKeyEnumerated(EnumType.STRING)
        @MapKeyColumn(name = "day_of_week")
        var businessHours: MutableMap<DayOfWeek, BusinessHour> = mutableMapOf()

) {

    fun isOpenAt(date: LocalDateTime): Boolean {
        val businessHour = this.businessHours[date.dayOfWeek] ?: return false

        if (date.toLocalTime().isAfterOrEqual(businessHour.openTime) && date.toLocalTime().isBeforeOrEqual(businessHour.closeTime.minusMinutes(this.avgReservationTime.toMinutes())))
            return true
        return false
    }

    fun getAvailability(date: LocalDateTime, peopleNumber: Int): AvailabilityType {
        if (!isOpenAt(date))
            return AvailabilityType.CLOSED

        val spots = getAvailableSpotsAt(date)
        spots.forEach {
            when {
                it.capacity >= peopleNumber && it.minPeopleNumber <= peopleNumber -> return AvailabilityType.AVAILABLE
                it.capacity >= peopleNumber && it.minPeopleNumber > peopleNumber -> return AvailabilityType.POSSIBLE
            }
        }
        return AvailabilityType.NOT_AVAILABLE
    }

    fun getAvailableSpotsAt(searchDate: LocalDateTime): List<Spot> {

        val searchDateInterval = object : DateTimeInterval {
            override var startDateTime: LocalDateTime = searchDate
            override var endDateTime: LocalDateTime = searchDate.plus(avgReservationTime)
        }

        val takenSpots = this.reservations
                .filter { searchDateInterval.overlaps(it) }
                .flatMap { it.spots }

        return this.spots.filterNot { takenSpots.contains(it) }
    }

    enum class AvailabilityType {
        AVAILABLE,
        POSSIBLE,
        NOT_AVAILABLE,
        CLOSED
    }

    enum class CuisineType {
        POLISH,
        ITALIAN,
        SPANISH,
        PORTUGUESE,
        GREEK,
        FRENCH,
        ASIAN,
        VIETNAMESE,
        CHINESE,
        JAPANESE,
        EUROPEAN,
        AMERICAN,
        DUMPLINGS,
        PANCAKES,
        FAST_FOOD,
        BURGERS,
        SUSHI,
        PIZZA,
        HEALTHY_FOOD,
        VEGETARIAN,
        VEGAN,
        STEAK_HOUSE,
        MEXICAN,
        ENGLISH,
        CZECH,
        CROATIAN,
        BULGARIAN,
        UKRAINIAN,
        LITHUANIAN,
        AUSTRIAN,
        GERMAN,
        BELGIAN,
        RUSSIAN,
        HUNGARIAN,
        SLOVENIAN,
        INDIAN,
        TURKISH,
        THAI,
        INDONESIAN,
        SILESIAN,
        PASTA,
        SOUP,
        GRILLED_DISHES,
        BREAKFAST,
        DESSERTS,
        GLUTEN_FREE,
        BAR,
        CAFE
    }

    enum class Facilities {
        AIR_CONDITIONING,
        SMOKING_ROOM,
        NON_SMOKING_ROOM,
        TERRACE,
        GARDEN,
        PARKING,
        FREE_WIFI,
        TOILET_FOR_DISABLED,
        DISABLED_PEOPLE_IMPROVEMENTS,
        CHILDREN_MENU,
        CHILD_SEATS,
        PLAYGROUND,
        MONITORED_PARKING,
        BABY_CORNER,
        PETS_FRIENDLY,
        BILLIARDS,
        DARTS,
        TV,
        BICYCLES_STANDS,
        VIP_ROOM,
        SEPARATE_BANQUET_HALL,
        CLOSE_TO_HOTEL,
        ALCOHOL,
        TAKEAWAY,
        PAYMENT_AMERICAN_EXPRESS,
        PAYMENT_MASTERCARD,
        PAYMENT_VISA,
        BOWLING,
        PLASTIC_PLATES,
        BOILING_WATER,
        BABY_TOILET,
        PRESS,
        SPORTS_BROADCAST
    }
}