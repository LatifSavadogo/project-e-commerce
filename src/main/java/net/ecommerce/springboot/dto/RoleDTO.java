package net.ecommerce.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class RoleDTO {
    private Integer idrole;
    
    @NotBlank(message = "Le libellé du rôle est obligatoire")
    private String librole;
    
    @NotBlank(message = "La description du rôle est obligatoire")
    private String descrole;
    
    private LocalDateTime dateupdate;
    private String userupdate;

    // Constructeurs
    public RoleDTO() {}

    public RoleDTO(Integer idrole, String librole, String descrole, LocalDateTime dateupdate, String userupdate) {
        this.idrole = idrole;
        this.librole = librole;
        this.descrole = descrole;
        this.dateupdate = dateupdate;
        this.userupdate = userupdate;
    }

    // Constructeur simplifié pour la création
    public RoleDTO(String librole, String descrole) {
        this.librole = librole;
        this.descrole = descrole;
    }

    public static RoleDTO fromEntity(net.ecommerce.springboot.model.Role role) {
        return new RoleDTO(
            role.getIdrole(),
            role.getLibrole(),
            role.getDescrole(),
            role.getDateupdate(),
            role.getUserupdate()
        );
    }

    // Getters et setters
    public Integer getIdrole() { return idrole; }
    public void setIdrole(Integer idrole) { this.idrole = idrole; }

    public String getLibrole() { return librole; }
    public void setLibrole(String librole) { this.librole = librole; }

    public String getDescrole() { return descrole; }
    public void setDescrole(String descrole) { this.descrole = descrole; }

    public LocalDateTime getDateupdate() { return dateupdate; }
    public void setDateupdate(LocalDateTime dateupdate) { this.dateupdate = dateupdate; }

    public String getUserupdate() { return userupdate; }
    public void setUserupdate(String userupdate) { this.userupdate = userupdate; }
}