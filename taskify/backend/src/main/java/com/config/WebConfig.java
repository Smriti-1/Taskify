package com.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    private final DefaultAdminInitialise defaultAdminInitialise;

    WebConfig(DefaultAdminInitialise defaultAdminInitialise) {
        this.defaultAdminInitialise = defaultAdminInitialise;
    }

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		// TODO Auto-generated method stub

		registry.addMapping("/**")
//		.allowedOrigins("*")
//		.allowedOrigins("http://127.0.0.1:5500/")
		.allowedOrigins("http://localhost:5500" , "http://127.0.0.1:5500/")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*")
		.allowCredentials(true);
	}
}
