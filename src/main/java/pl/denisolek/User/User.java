package pl.denisolek.User;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Email;
import pl.denisolek.BaseEntity;
import pl.denisolek.Security.Authority;
import pl.denisolek.Views;
import pl.denisolek.core.restaurant.Restaurant;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
@Entity
@Table(name = "[user]")
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {

	@Column(updatable = false, nullable = false)
	@Size(max = 50)
	@Email
	String email;
	String name;
	@JsonView(Views.User.class)
	String surname;
	@JsonView(Views.User.class)
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "restaurant_id")
	Restaurant restaurant;
	@JsonView(Views.User.class)
	AccountState accountState;
	@Size(min = 8, max = 80)
	private String password;
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
			name = "user_authority",
			joinColumns = @JoinColumn(name = "username"),
			inverseJoinColumns = @JoinColumn(name = "authority")
	)
	private Set<Authority> authorities;
}
