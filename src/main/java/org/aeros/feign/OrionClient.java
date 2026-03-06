package org.aeros.feign;

import org.aeros.config.FeignAuthConfig;
import org.aeros.rest.domain.DomainRest;
import org.aeros.rest.domain.InfrastructureElementRest;
import org.aeros.rest.domain.ServiceComponentRest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "orion-ld", configuration = FeignAuthConfig.class)
public interface OrionClient {

	@GetMapping(value = "/entities/{id}?format=simplified&type=Domain")
	DomainRest getDomainById(@PathVariable("id") final String domainId);

	@GetMapping(value = "/entities?format=simplified&type=InfrastructureElement")
	InfrastructureElementRest[] getInfrastructureElements();

	@GetMapping(value = "/entities?format=simplified&type=ServiceComponent&q=infrastructureElement==\"{infrastructureElement}\"")
	ServiceComponentRest[] getServicesForIE(
			@PathVariable("infrastructureElement") final String infrastructureElementId);
}
