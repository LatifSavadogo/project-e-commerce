package net.ecommerce.springboot.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import net.ecommerce.springboot.exception.RestAccessDeniedHandler;
import net.ecommerce.springboot.exception.RestAuthenticationEntryPoint;
import net.ecommerce.springboot.service.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	private final UserDetailsServiceImpl userDetailsService;
	private final RestAuthenticationEntryPoint authenticationEntryPoint;
	private final RestAccessDeniedHandler accessDeniedHandler;

	/** Si false : /swagger-ui et /v3/api-docs exigent une session (défense en profondeur côté filtre). */
	@Value("${app.security.swagger-open:true}")
	private boolean swaggerOpen;

	public SecurityConfig(UserDetailsServiceImpl userDetailsService,
			RestAuthenticationEntryPoint authenticationEntryPoint,
			RestAccessDeniedHandler accessDeniedHandler) {
		this.userDetailsService = userDetailsService;
		this.authenticationEntryPoint = authenticationEntryPoint;
		this.accessDeniedHandler = accessDeniedHandler;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
//		configuration.setAllowedOrigins(List.of("http://localhost:5173"));
		configuration.setAllowedOriginPatterns(Arrays.asList(
				"http://localhost:*",
				"http://127.0.0.1:*",
				"http://192.168.11.106:*",
				"http://192.168.*.*:*",
				"http://10.*.*.*:*",
				"http://*.*.*.*:5173"));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setExposedHeaders(List.of("Set-Cookie"));
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.cors(c -> c.configurationSource(corsConfigurationSource()))
				.csrf(csrf -> csrf.disable())
				.sessionManagement(s -> s
						.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
						.sessionFixation(fix -> fix.changeSessionId()))
				.userDetailsService(userDetailsService)
				.exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint)
						.accessDeniedHandler(accessDeniedHandler))
				.authorizeHttpRequests(auth -> configureAuthorization(auth));

		return http.build();
	}

	private void configureAuthorization(
			org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer<? extends HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
		auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

		if (swaggerOpen) {
			auth.requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll();
		}

		auth.requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN");

		auth.requestMatchers("/api/auth/**").permitAll();

		// Catalogue public : uniquement liste, fiche, images (pas d’autre GET sous /articles)
		auth.requestMatchers(HttpMethod.GET, "/api/v1/articles").permitAll();
		auth.requestMatchers(HttpMethod.GET, "/api/v1/articles/*").permitAll();
		auth.requestMatchers(HttpMethod.GET, "/api/v1/articles/*/photo").permitAll();
		auth.requestMatchers(HttpMethod.GET, "/api/v1/articles/*/photo/*").permitAll();

		auth.requestMatchers(HttpMethod.GET, "/api/v1/roles", "/api/v1/roles/*").permitAll();
		auth.requestMatchers(HttpMethod.GET, "/api/v1/pays", "/api/v1/pays/*").permitAll();
		auth.requestMatchers(HttpMethod.GET, "/api/v1/familleArticles", "/api/v1/familleArticles/*").permitAll();
		auth.requestMatchers(HttpMethod.GET, "/api/v1/typeArticles", "/api/v1/typeArticles/*").permitAll();

		auth.anyRequest().authenticated();
	}
}
