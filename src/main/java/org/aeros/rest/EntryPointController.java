package org.aeros.rest;

import static java.lang.String.format;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.aeros.rest.domain.ConfigurationRest;
import org.aeros.service.DomainService;
import org.aeros.service.LoadBalancerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/entrypoint-balancer")
@AllArgsConstructor
public class EntryPointController {

	final LoadBalancerService loadBalancerService;
	final DomainService domainService;

	@PostMapping(value = "/distribute/{serviceId}", consumes = "application/yaml")
	public ResponseEntity<String> distribute(@PathVariable("serviceId") String serviceId, @RequestBody String tosca) {
		final String hloDomain = loadBalancerService.passToscaToSelectedHLO(serviceId, tosca);
		return ResponseEntity.ok(format("Request was distributed successfully to HLO located in the domain %s.", hloDomain));
	}

	@PutMapping(value = "/distribute/{serviceId}")
	public ResponseEntity<String> reDistribute(@PathVariable("serviceId") String serviceId) {
		final String hloDomain = loadBalancerService.reOrchestrateServiceInToSelectedHLO(serviceId);
		return ResponseEntity.ok(format("Service Id was distributed successfully to HLO located in the domain %s.", hloDomain));
	}

	@PostMapping(value = "/configure", consumes = APPLICATION_JSON_VALUE)
	public ResponseEntity<String> changeConfiguration(@RequestBody ConfigurationRest configuration) {
		loadBalancerService.changeAssignmentLimit(configuration.getMaxAssignments());
		domainService.changeWeightingFunction(configuration.getWeightingFunctionType());
		return ResponseEntity.ok(format("New configuration has been applied (%s).", configuration));
	}

	@GetMapping(value = "/configure", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ConfigurationRest> getConfiguration() {
		final ConfigurationRest currentConfiguration = new ConfigurationRest(loadBalancerService.getMaxAssignments(),
				domainService.getWeightingFunctionType().name());
		return ResponseEntity.ok(currentConfiguration);
	}
}
