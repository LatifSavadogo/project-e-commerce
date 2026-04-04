package net.ecommerce.springboot.config;

import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import net.ecommerce.springboot.service.session.ActiveSessionCounter;

@Configuration
public class HttpSessionListenerConfig {

	@Bean
	public ServletListenerRegistrationBean<HttpSessionListener> activeSessionHttpListener(ActiveSessionCounter counter) {
		HttpSessionListener listener = new HttpSessionListener() {
			@Override
			public void sessionCreated(HttpSessionEvent se) {
				counter.sessionCreated();
			}

			@Override
			public void sessionDestroyed(HttpSessionEvent se) {
				counter.sessionDestroyed();
			}
		};
		return new ServletListenerRegistrationBean<>(listener);
	}
}
