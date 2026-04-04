//package net.ecommerce.springboot.controller;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import net.ecommerce.springboot.model.User;
//import net.ecommerce.springboot.service.AuthService;
//
//import java.util.HashMap;
//import java.util.Map;
//@RestController
//@RequestMapping("/api/auth")
//public class AuthController {
//    
//    @Autowired
//    private AuthService authService;
//
//    @GetMapping("/check")
//    public ResponseEntity<?> checkAuth() {
//        User currentUser = authService.getCurrentUser();
//        
//        if (currentUser != null) {
//            Map<String, Object> response = new HashMap<>();
//            response.put("authenticated", true);
//            response.put("user", Map.of(
//                "id", currentUser.getIduser(),
//                "nom", currentUser.getNom(),
//                "email", currentUser.getEmail(),
//                "role", currentUser.getRole().getLibrole()
//            ));
//            response.put("redirectUrl", getRedirectUrlByRole(currentUser.getRole().getLibrole()));
//            return ResponseEntity.ok(response);
//        }
//        
//        Map<String, Object> response = new HashMap<>();
//        response.put("authenticated", false);
//        return ResponseEntity.ok(response);
//    }
//
//    private String getRedirectUrlByRole(String role) {
//        switch (role) {
//            case "ADMIN":
//                return "/admin/dashboard";
//            case "USER":
//                return "/user/dashboard";
//            case "MANAGER":
//                return "/manager/dashboard";
//            default:
//                return "/dashboard";
//        }
//    }
//}