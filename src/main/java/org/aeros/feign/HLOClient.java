package org.aeros.feign;

import java.net.URI;

import org.aeros.config.FeignAuthConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "hlo-fe", configuration = FeignAuthConfig.class)
public interface HLOClient {

	@PostMapping(
			value = "/hlo_fe/services/{serviceId}",
			consumes = "application/yaml"
	)
	String passToscaToHLO(final URI baseURL,
			@PathVariable("serviceId") final String serviceId,
			@RequestBody String toscaYaml);

	@PutMapping(value = "/hlo_fe/services/{serviceId}")
	String passServiceIdToHLO(final URI baseURL, @PathVariable("serviceId") final String serviceId);
}
