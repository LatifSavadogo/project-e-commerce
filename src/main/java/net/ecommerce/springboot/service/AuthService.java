package net.ecommerce.springboot.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.repository.UserRepository;

@Service("authService")
public class AuthService {

	private final UserRepository userRepository;

	public AuthService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public User getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			return null;
		}
		Object principal = authentication.getPrincipal();
		if (!(principal instanceof UserDetails)) {
			return null;
		}
		String email = ((UserDetails) principal).getUsername();
		return userRepository.findByEmail(email).orElse(null);
	}

	public boolean isSelf(Integer iduser) {
		if (iduser == null) {
			return false;
		}
		User u = getCurrentUser();
		return u != null && iduser.equals(u.getIduser());
	}

	public String getCurrentUserEmail() {
		User u = getCurrentUser();
		return u != null ? u.getEmail() : null;
	}

	public boolean hasRole(String roleName) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			return false;
		}
		String key = "ROLE_" + roleName.toUpperCase();
		return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equalsIgnoreCase(key));
	}
}
