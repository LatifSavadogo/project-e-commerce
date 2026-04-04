package net.ecommerce.springboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

	/** Nom référencé par @SecurityRequirement si besoin sur un contrôleur précis. */
	public static final String SESSION_COOKIE_SCHEME = "sessionCookie";

	@Bean
	public OpenAPI ecommerceOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Ecomarket API")
						.description("""
								API REST de la marketplace Ecomarket.

								**Session :** connectez-vous avec `POST /api/auth/login` depuis cette interface. \
								Le cookie `JSESSIONID` est alors envoyé automatiquement pour les requêtes suivantes \
								(même origine que le serveur).

								**Multipart :** création / mise à jour d’articles : champs formulaire + fichier(s) `photo` ou `photos`.

								**Accès Swagger :** si `app.security.swagger-open=false`, connectez-vous d’abord (`POST /api/auth/login`) \
								puis ouvrez Swagger sur le même hôte pour que le cookie de session soit envoyé.""")
						.version("v1")
						.contact(new Contact().name("Ecomarket").email("support@ecomarket.local"))
						.license(new License().name("Projet académique / interne").url("https://opensource.org/licenses/MIT")))
				.components(new Components().addSecuritySchemes(SESSION_COOKIE_SCHEME,
						new SecurityScheme()
								.type(SecurityScheme.Type.APIKEY)
								.in(SecurityScheme.In.COOKIE)
								.name("JSESSIONID")
								.description("Rempli automatiquement après login sur cette page ; sinon copiez la valeur du cookie depuis les outils développeur.")));
	}
}
