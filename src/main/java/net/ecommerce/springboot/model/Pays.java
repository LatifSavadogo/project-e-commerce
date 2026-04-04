package net.ecommerce.springboot.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "Pays")
public class Pays {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer idpays;
	
	@Column(name="libpays")
	private String libpays;
	
	@Column(name="descpays")
	private String descpays;
	
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
	
    @OneToMany(mappedBy = "iduser")
    private List<User> users;

    
	public Pays() {}
	
	public Pays(Integer idpays, String libpays, String descpays, String userupdate, LocalDateTime dateupdate) {
		this.idpays=idpays;
		this.libpays=libpays;
		this.descpays=descpays;
		this.userupdate=userupdate;
		this.dateupdate=dateupdate;
	}
	
	public Integer getIdpays() {
		return idpays;
	}
	
	public void setIdpays(Integer idpays) {
		this.idpays=idpays;
	}
	
	public String getLibpays() {
		return libpays;
	}
	
	public void setLibpays(String libpays) {
		this.libpays=libpays;
	}
	
	public String getDescpays() {
		return descpays;
	}
	
	public void setDescpays(String descpays) {
		this.descpays=descpays;
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
    
    public User addUsers(User users) {
        getUsers().add(users);
        users.setPays(this);
        return users;
    }

    public User removeUsers(User users) {
        getUsers().remove(users);
        users.setPays(null);
        return users;
    }
}
