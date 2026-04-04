//package net.ecommerce.springboot.config;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import net.ecommerce.springboot.service.UserDetailsServiceImpl;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.AuthenticationFailureHandler;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
//import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
//import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//	private final UserDetailsServiceImpl userDetailsService;
//
//	public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
//		this.userDetailsService = userDetailsService;
//	}
//
//	@Bean
//	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
//		return authConfig.getAuthenticationManager();
//	}
//
//	@Bean
//	public PasswordEncoder passwordEncoder() {
//		return new BCryptPasswordEncoder();
//	}
//
//	@Bean
//	public CorsConfigurationSource corsConfigurationSource() {
//		CorsConfiguration configuration = new CorsConfiguration();
//		configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
//		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//		configuration.setAllowedHeaders(Arrays.asList("*"));
//		configuration.setAllowCredentials(true);
//
//		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//		source.registerCorsConfiguration("/**", configuration);
//		return source;
//	}
//
//	@Bean
//	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//	    http
//	        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//	        .csrf(csrf -> csrf.disable())
//	        .authorizeHttpRequests(auth -> auth
//	            .requestMatchers(
//	                "/api/auth/login",
//	                "/api/auth/check",
//	                "/api/auth/logout",
//	                "/login",
//	                "/error"
//	                
//	            ).permitAll()
//	            
//	            .requestMatchers("/api/v1/**").authenticated()
//	            .requestMatchers("/api/**").authenticated() 
//	            
//	            .anyRequest().authenticated()
//	        )
//	        .formLogin(form -> form
//	            .loginProcessingUrl("/api/auth/login")
//	            .successHandler(authenticationSuccessHandler())
//	            .failureHandler(authenticationFailureHandler())
//	            .permitAll()
//	        )
//	        .logout(logout -> logout
//	            .logoutUrl("/api/auth/logout")
//	            .logoutSuccessHandler(logoutSuccessHandler())
//	            .invalidateHttpSession(true)
//	            .deleteCookies("JSESSIONID")
//	            .permitAll()
//	        )
//	        // Empêche la redirection vers /login
//	        .exceptionHandling(exceptions -> exceptions
//	            .authenticationEntryPoint((request, response, authException) -> {
//	                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//	                response.setContentType("application/json");
//	                response.getWriter().write("{\"error\":\"Authentication required\"}");
//	            })
//	        );
//
//	    return http.build();
//	}
//
//	@Bean
//	public AuthenticationSuccessHandler authenticationSuccessHandler() {
//		return new SimpleUrlAuthenticationSuccessHandler() {
//			@Override
//			public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
//					Authentication authentication) throws IOException {
//
//				clearAuthenticationAttributes(request);
//
//				UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//
//				Map<String, Object> responseBody = new HashMap<>();
//				responseBody.put("success", true);
//				responseBody.put("message", "Connexion réussie");
//				responseBody.put("user", Map.of("username", userDetails.getUsername(), "authorities", userDetails
//						.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList())));
//
//				response.setContentType("application/json");
//				response.setCharacterEncoding("UTF-8");
//				response.getWriter().write(new ObjectMapper().writeValueAsString(responseBody));
//			}
//		};
//	}
//
//	@Bean
//	public AuthenticationFailureHandler authenticationFailureHandler() {
//		return (request, response, exception) -> {
//			response.setStatus(401);
//			response.setContentType("application/json");
//			response.setCharacterEncoding("UTF-8");
//
//			Map<String, String> errorResponse = new HashMap<>();
//			errorResponse.put("success", "false");
//			errorResponse.put("error", "Email ou mot de passe incorrect");
//
//			response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
//		};
//	}
//
//	@Bean
//	public LogoutSuccessHandler logoutSuccessHandler() {
//		return (request, response, authentication) -> {
//			response.setContentType("application/json");
//			response.setCharacterEncoding("UTF-8");
//
//			Map<String, String> responseBody = new HashMap<>();
//			responseBody.put("success", "true");
//			responseBody.put("message", "Déconnexion réussie");
//
//			response.getWriter().write(new ObjectMapper().writeValueAsString(responseBody));
//		};
//	}
//}