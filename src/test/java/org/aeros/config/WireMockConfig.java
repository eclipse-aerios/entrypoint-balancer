package org.aeros.config;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import com.github.tomakehurst.wiremock.WireMockServer;

@TestConfiguration
@ActiveProfiles("test")
public class WireMockConfig {

	@Bean(initMethod = "start", destroyMethod = "stop")
	public WireMockServer mockOrion() {
		return new WireMockServer(options().port(80));
	}

	@Bean(initMethod = "start", destroyMethod = "stop")
	public WireMockServer mockHLO() {
		return new WireMockServer(options().port(81));
	}
}
