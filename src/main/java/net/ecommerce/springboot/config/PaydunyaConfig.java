package net.ecommerce.springboot.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PaydunyaProperties.class)
public class PaydunyaConfig {
}
