package net.ecommerce.springboot.model;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Role")
public class Role {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idrole;

	@Column(name = "librole", nullable = false)
	private String librole;

	@Column(name = "descrole", nullable = false)
	private String descrole;

	@Column(name = "userupdate", nullable = false)
	private String userupdate;

	@Column(name = "dateupdate", nullable = false)
	private LocalDateTime dateupdate;

	@PrePersist
	public void prePersist() {
		dateupdate = LocalDateTime.now();
		userupdate = "admin"; 
	}

	@PreUpdate
	public void preUpdate() {
		dateupdate = LocalDateTime.now();
		userupdate = "admin"; 
	}

	@OneToMany(mappedBy = "role")
	private List<User> users;

	public Role() {
	}

	public Role(Integer idrole, String descrole, String librole, String userupdate, LocalDateTime dateupdate) {
		this.idrole = idrole;
		this.librole = librole;
		this.descrole = descrole;
		this.userupdate = userupdate;
		this.dateupdate = dateupdate;
	}

	public Integer getIdrole() {

		return idrole;
	}

	public void setIdrole(Integer idrole) {
		this.idrole = idrole;
	}

	public String getLibrole() {

		return librole;
	}

	public void setLibrole(String librole) {

		this.librole = librole;
	}

	public String getDescrole() {
		return descrole;
	}

	public void setDescrole(String descrole) {
		this.descrole = descrole;
	}

	public String getUserupdate() {

		return userupdate;
	}

	public void setUserupdate(String userupdate) {

		this.userupdate = userupdate;
	}

	public LocalDateTime getDateupdate() {

		return dateupdate;
	}

	public void setDateupdate(LocalDateTime dateupdate) {

		this.dateupdate = dateupdate;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public User addUser(User user) {
		getUsers().add(user);
		user.setRole(this);

		return user;
	}

	public User removeUser(User user) {
		getUsers().remove(user);
		user.setRole(null);

		return user;
	}

}