package pl.denisolek.core.menu.category

import pl.denisolek.core.menu.Menu
import pl.denisolek.core.menu.item.MenuItem
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
        var items: List<MenuItem> = listOf(),

        @ManyToOne
        var menu: Menu
)
