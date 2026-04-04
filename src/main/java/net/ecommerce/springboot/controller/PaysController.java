package net.ecommerce.springboot.controller;

import net.ecommerce.springboot.dto.PaysDTO;
import net.ecommerce.springboot.dto.RoleDTO;
import net.ecommerce.springboot.model.Pays;
import net.ecommerce.springboot.model.Role;
import net.ecommerce.springboot.exception.ResourceNotFoundException;
import net.ecommerce.springboot.repository.PaysRepository;
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
public class PaysController {

    @Autowired
    private PaysRepository paysRepository;
    
//    @Autowired
//    private AuthService authService;


    // Lister tous les rôles
    @GetMapping("/pays")
    public List<PaysDTO> getAllPays() {
        return paysRepository.findAll()
                .stream()
                .map(PaysDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PostMapping("/pays")
    public ResponseEntity<PaysDTO> createPays(@Valid @RequestBody PaysDTO paysDTO) {
//    	String currentUsername = authService.getCurrentUsername();
    	Pays pays = new Pays();
    	pays.setLibpays(paysDTO.getLibpays());
    	pays.setDescpays(paysDTO.getDescpays());
        
    	pays.setDateupdate(LocalDateTime.now());
//        role.setUserupdate(currentUsername); 
    	pays.setUserupdate("admin"); 

    	Pays savedPays = paysRepository.save(pays);
        return ResponseEntity.status(HttpStatus.CREATED).body(PaysDTO.fromEntity(savedPays));
    }

    @GetMapping("/pays/{idpays}")
    public ResponseEntity<PaysDTO> getRoleById(@PathVariable Integer idpays) {
    	Pays pays = paysRepository.findById(idpays)
                .orElseThrow(() -> new ResourceNotFoundException("Le rôle avec l'id " + idpays + " n'existe pas"));
        return ResponseEntity.ok(PaysDTO.fromEntity(pays));
    }
    
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PutMapping("/pays/{idpays}")
    public ResponseEntity<PaysDTO> updateRole(@PathVariable Integer idpays, 
                                             @Valid @RequestBody PaysDTO paysDTO) {
//    	String currentUsername = authService.getCurrentUsername();
    	Pays pays = paysRepository.findById(idpays)
                .orElseThrow(() -> new ResourceNotFoundException("Le Pays avec l'id " + idpays + " n'existe pas"));
 
    	pays.setLibpays(paysDTO.getLibpays());
    	pays.setDescpays(paysDTO.getDescpays());
        
    	pays.setDateupdate(LocalDateTime.now());
//        role.setUserupdate(currentUsername);
    	pays.setUserupdate("admin"); 

    	Pays updatedPays = paysRepository.save(pays);
        return ResponseEntity.ok(PaysDTO.fromEntity(updatedPays));
    }

    //  5. Supprimer un pays
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @DeleteMapping("/pays/{idpays}")
    public ResponseEntity<?> deletePays(@PathVariable Integer idpays) {
    	Pays pays = paysRepository.findById(idpays)
                .orElseThrow(() -> new ResourceNotFoundException("Le pays avec l'id " + idpays + " n'existe pas"));
        
        //  Vérifier s'il y a des utilisateurs avant suppression
        if (pays.getUsers() != null && !pays.getUsers().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("supprimé", false);
            errorResponse.put("message", "Impossible de supprimer le pays : il a " + 
            		pays.getUsers().size() + " utilisateur(s) associé(s)");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        paysRepository.delete(pays);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("supprimé", true);
        return ResponseEntity.ok(response);
    }

    //  6. Récupérer les utilisateurs d'un pays
    @GetMapping("/pays/{idpays}/users")
    public ResponseEntity<?> getUtilisateursByPays(@PathVariable Integer idpays) {
    	Pays pays = paysRepository.findById(idpays)
                .orElseThrow(() -> new ResourceNotFoundException("Le pays avec l'id " + idpays + " n'existe pas"));
        
        List<Map<String, Object>> users = pays.getUsers()
                .stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("iduser", user.getIduser());
                    userMap.put("nom", user.getNom());
                    userMap.put("email", user.getEmail());
                    return userMap;
                })
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("pays", PaysDTO.fromEntity(pays));
        response.put("utilisateurs", users);
        response.put("nombreUtilisateurs", users.size());
        
        return ResponseEntity.ok(response);
    }
}