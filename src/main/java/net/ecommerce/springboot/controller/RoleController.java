package net.ecommerce.springboot.controller;

import net.ecommerce.springboot.dto.RoleDTO;
import net.ecommerce.springboot.model.Role;
import net.ecommerce.springboot.exception.ResourceNotFoundException;
import net.ecommerce.springboot.repository.RoleRepository;
//import net.ecommerce.springboot.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class RoleController {

    @Autowired
    private RoleRepository roleRepository;
    
//    @Autowired
//    private AuthService authService;


    // Lister tous les rôles
    @GetMapping("/roles")
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll()
                .stream()
                .map(RoleDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/roles")
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleDTO roleDTO) {
//    	String currentUsername = authService.getCurrentUsername();
    	Role role = new Role();
        role.setLibrole(roleDTO.getLibrole());
        role.setDescrole(roleDTO.getDescrole());
        
        role.setDateupdate(LocalDateTime.now());
//        role.setUserupdate(currentUsername); 
        role.setUserupdate("admin"); 

        Role savedRole = roleRepository.save(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(RoleDTO.fromEntity(savedRole));
    }

    @GetMapping("/roles/{idrole}")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable Integer idrole) {
        Role role = roleRepository.findById(idrole)
                .orElseThrow(() -> new ResourceNotFoundException("Le rôle avec l'id " + idrole + " n'existe pas"));
        return ResponseEntity.ok(RoleDTO.fromEntity(role));
    }
    
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/roles/{idrole}")
    public ResponseEntity<RoleDTO> updateRole(@PathVariable Integer idrole, 
                                             @Valid @RequestBody RoleDTO roleDTO) {
//    	String currentUsername = authService.getCurrentUsername();
    	Role role = roleRepository.findById(idrole)
                .orElseThrow(() -> new ResourceNotFoundException("Le rôle avec l'id " + idrole + " n'existe pas"));
 
        role.setLibrole(roleDTO.getLibrole());
        role.setDescrole(roleDTO.getDescrole());
        
        role.setDateupdate(LocalDateTime.now());
//        role.setUserupdate(currentUsername);
        role.setUserupdate("admin"); 

        Role updatedRole = roleRepository.save(role);
        return ResponseEntity.ok(RoleDTO.fromEntity(updatedRole));
    }

    //  5. Supprimer un rôle
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/roles/{idrole}")
    public ResponseEntity<?> deleteRole(@PathVariable Integer idrole) {
        Role role = roleRepository.findById(idrole)
                .orElseThrow(() -> new ResourceNotFoundException("Le rôle avec l'id " + idrole + " n'existe pas"));
        
        //  Vérifier s'il y a des utilisateurs avant suppression
        if (role.getUsers() != null && !role.getUsers().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("supprimé", false);
            errorResponse.put("message", "Impossible de supprimer le rôle : il a " + 
                             role.getUsers().size() + " utilisateur(s) associé(s)");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        roleRepository.delete(role);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("supprimé", true);
        return ResponseEntity.ok(response);
    }

    //  6. Récupérer les utilisateurs d'un rôle
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @GetMapping("/roles/{idrole}/utilisateurs")
    public ResponseEntity<?> getUtilisateursByRole(@PathVariable Integer idrole) {
        Role role = roleRepository.findById(idrole)
                .orElseThrow(() -> new ResourceNotFoundException("Le rôle avec l'id " + idrole + " n'existe pas"));
        
        List<Map<String, Object>> utilisateurs = role.getUsers()
                .stream()
                .map(utilisateur -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("iduser", utilisateur.getIduser());
                    userMap.put("nomuser", utilisateur.getNom());
                    userMap.put("email", utilisateur.getEmail());
                    return userMap;
                })
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("role", RoleDTO.fromEntity(role));
        response.put("utilisateurs", utilisateurs);
        response.put("nombreUtilisateurs", utilisateurs.size());
        
        return ResponseEntity.ok(response);
    }
}