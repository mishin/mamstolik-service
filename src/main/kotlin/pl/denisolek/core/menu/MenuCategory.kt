package pl.denisolek.core.menu

import pl.denisolek.core.restaurant.Restaurant
import javax.persistence.*

@Entity
data class MenuCategory(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Int? = null,
        var name: String,
        var description: String? = "",
        var position: Int,

        @OneToMany(mappedBy = "category", cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
        var items: MutableSet<MenuItem> = mutableSetOf(),

        @ManyToOne
        var restaurant: Restaurant
)
