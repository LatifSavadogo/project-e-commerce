package net.ecommerce.springboot.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PaysDTO {
private Integer idpays;
    
    @NotBlank(message = "Le libellé du pays est obligatoire")
    @Size(min = 2, max = 100, message = "Le libellé doit contenir entre 2 et 100 caractères")
    private String libpays;
    
    @NotBlank(message = "La description est obligatoire")
    @Size(min = 5, max = 500, message = "La description doit contenir entre 5 et 500 caractères")
    private String descpays;
    
    private LocalDateTime dateupdate;
    
    private String userupdate;
    
    private List<UserDTO> users;
    
    // Constructeurs
    public PaysDTO() {}
    
    
    public PaysDTO(Integer idpays, String libpays, String descpays,
                            LocalDateTime dateupdate, String userupdate) {
        this.idpays = idpays;
        this.libpays = libpays;
        this.descpays = descpays;
        this.dateupdate = dateupdate;
        this.userupdate = userupdate;
    }
    
    // Méthodes de conversion
    public static PaysDTO fromEntity(net.ecommerce.springboot.model.Pays pays) {
        if (pays == null) {
            return null;
        }
        
        PaysDTO dto = new PaysDTO();
        dto.setIdpays(pays.getIdpays());
        dto.setLibpays(pays.getLibpays());
        dto.setDescpays(pays.getDescpays());
        dto.setDateupdate(pays.getDateupdate());
        dto.setUserupdate(pays.getUserupdate());
        
        // Vérifier si getUsers() n'est pas null AVANT d'appeler .stream()
        if (pays.getUsers() != null) {
            List<UserDTO> userDTOs = pays.getUsers()
                .stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
            dto.setUsers(userDTOs);
        } else {
            dto.setUsers(Collections.emptyList());
        }
        
        return dto;
    }
    
    public static net.ecommerce.springboot.model.Pays toEntity(PaysDTO dto) {
        net.ecommerce.springboot.model.Pays pays = new net.ecommerce.springboot.model.Pays();
        pays.setIdpays(dto.getIdpays());
        pays.setLibpays(dto.getLibpays());
        pays.setDescpays(dto.getDescpays());
        pays.setUserupdate(dto.getUserupdate());
        pays.setDateupdate(dto.getDateupdate());
        
        return pays;
    }
    
    // Getters et Setters
    public Integer getIdpays() {
        return idpays;
    }
    
    public void setIdpays(Integer idpays) {
        this.idpays = idpays;
    }
    
    public String getLibpays() {
        return libpays;
    }
    
    public void setLibpays(String libpays) {
        this.libpays = libpays;
    }
    
    public String getDescpays() {
        return descpays;
    }
    
    public void setDescpays(String descpays) {
        this.descpays = descpays;
    }
    
    public LocalDateTime getDateupdate() {
        return dateupdate;
    }
    
    public void setDateupdate(LocalDateTime dateupdate) {
        this.dateupdate = dateupdate;
    }
    
    public String getUserupdate() {
        return userupdate;
    }
    
    public void setUserupdate(String userupdate) {
        this.userupdate = userupdate;
    }
    
    public List<UserDTO> getUsers() {
        return users;
    }
    
    public void setUsers(List<UserDTO> users) {
        this.users = users;
    }
}
