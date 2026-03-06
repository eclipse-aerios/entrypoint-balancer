package org.aeros.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;

public class FeignAuthConfig {

	private static final String AUTH_HEADER = "Authorization";

	@Bean
	public RequestInterceptor requestInterceptor() {
		return requestTemplate -> {
			final ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			final String authHeader = Optional.ofNullable(attributes)
					.map(ServletRequestAttributes::getRequest)
					.map(req -> req.getHeader(AUTH_HEADER))
					.orElse("");

			requestTemplate.header(AUTH_HEADER, authHeader);
			requestTemplate.header("aerOS", "true");
		};
	}
}
