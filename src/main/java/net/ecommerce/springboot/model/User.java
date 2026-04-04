package net.ecommerce.springboot.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name="User")
public class User {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer iduser;
	
	@Column(name= "nom", nullable=false)
	private String nom;
	
	@Column(name= "prenom", nullable=false)
	private String prenom;
	
	@Column(name= "email", nullable=false)
	private String email;
	
	@Column(name= "password", nullable=false)
	private String password;
	
	@Column(name= "cnib")
	private String cnib;
	
	@ManyToOne
	@JoinColumn(name = "idrole")
    private Role role;
	
	@ManyToOne
	@JoinColumn(name="idpays")
	private Pays pays;
	
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
    
    public User() {}
    
    public User(Integer iduser, String nom, String prenom, String email, 
    		String password, String cnib, Pays pays, Role role, String userupdate, LocalDateTime dateupdate) {
    	this.iduser=iduser;
    	this.nom=nom;
    	this.prenom=prenom;
    	this.email=email;
    	this.password=password;
    	this.cnib=cnib;
    	this.pays=pays;
    	this.role=role;
    	this.userupdate=userupdate;
    	this.dateupdate=dateupdate;
    }
    
    public Integer getIduser() {
    	return iduser;
    }
    public void setIduser(Integer iduser) {
    	this.iduser=iduser;
    }
    
    public String getNom() {
    	return nom;
    }
    public void setNom(String nom) {
    	this.nom=nom;
    }
    public String getPrenom() {
    	return prenom;
    }
    public void setPrenom(String prenom) {
    	this.prenom=prenom;
    }
    public String getEmail() {
    	return email;
    }
    public void setEmail(String email) {
    	this.email=email;
    }
    public String getPassword() {
    	return password;
    }
    public void setPassword(String password) {
    	this.password=password;
    }
    public String getCnib() {
    	return cnib;
    }
    public void setCnib(String cnib) {
    	this.cnib=cnib;
    }
    public Role getRole() {
    	return role;
    }
    
    public void setRole(Role role) {
    	this.role=role;
    }
    public Pays getPays() {
    	return pays;
    }
    public void setPays(Pays pays) {
    	this.pays=pays;
    }
    
    public String getUserupdate() {
    	return userupdate;
    }
    
    public void setUserupdate(String userupdate) {
    	this.userupdate=userupdate;
    }
    
    public LocalDateTime getDateupdate() {
    	return dateupdate;
    }
    public void setDateupdate(LocalDateTime dateupdate) {
    	this.dateupdate=dateupdate;
    }
    
}
