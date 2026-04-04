package net.ecommerce.springboot.service;

import java.util.Collections;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;

	public UserDetailsServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("Utilisateur inconnu : " + email));
		if (user.getRole() == null) {
			throw new UsernameNotFoundException("Aucun rôle pour : " + email);
		}
		List<GrantedAuthority> authorities = Collections.singletonList(
				new SimpleGrantedAuthority("ROLE_" + user.getRole().getLibrole().toUpperCase()));
		return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
				.password(user.getPassword())
				.authorities(authorities)
				.build();
	}
}
