package com.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class Appconfig {

	@Bean
	public PasswordEncoder encode() {
		return new BCryptPasswordEncoder();
	}
}
