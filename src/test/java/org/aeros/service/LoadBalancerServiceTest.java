package org.aeros.service;

import static java.net.URI.create;
import static org.aeros.fixtures.DomainFixtures.DOMAIN_URL_1;
import static org.aeros.fixtures.DomainFixtures.DOMAIN_URL_2;
import static org.aeros.fixtures.DomainFixtures.SINGLE_DOMAIN;
import static org.aeros.fixtures.DomainFixtures.TWO_DOMAINS;
import static org.aeros.fixtures.DomainFixtures.TWO_EQUIVALENT_DOMAINS;
import static org.aeros.fixtures.ServiceFixtures.SERVICE_ID;
import static org.aeros.fixtures.ServiceFixtures.TOSCA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.aeros.feign.HLOClient;
import org.aeros.service.domain.Domain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class LoadBalancerServiceTest {

	@MockBean
	private HLOClient hloClient;
	@MockBean
	private DomainService domainService;

	@Autowired
	private LoadBalancerServiceImpl loadBalancerService;

	@BeforeEach
	void setUp() {
		loadBalancerService.isOverloadDetected = false;
		loadBalancerService.overloadCounter.set(0);
		loadBalancerService.lastDomainSelection = null;
	}

	@Test
	@DisplayName("Test passing tosca to HLO when only one domain is present.")
	void testPassingToHLOWithSingleDomain() {
		when(domainService.getAllDomains()).thenReturn(SINGLE_DOMAIN);

		String result = loadBalancerService.passToscaToSelectedHLO(SERVICE_ID, TOSCA);

		assertEquals(DOMAIN_URL_1, result, "Load balancer should pass the 1st request to Domain 1.");

		loadBalancerService.passToscaToSelectedHLO(SERVICE_ID, TOSCA);
		result = loadBalancerService.passToscaToSelectedHLO(SERVICE_ID, TOSCA);

		assertEquals(DOMAIN_URL_1, result, "Upon repetition, load balancer should still pass the 2nd request to Domain 1.");
		verify(hloClient, times(3)).passToscaToHLO(create(DOMAIN_URL_1), SERVICE_ID, TOSCA);
	}

	@Test
	@DisplayName("Test passing tosca to HLO when two HLOs with equivalent weights are available.")
	void testPassingToHLOWhenWeightsAreEquivalent() {
		final List<Domain> testDomainList = new ArrayList<>(TWO_EQUIVALENT_DOMAINS);
		when(domainService.getAllDomains()).thenReturn(testDomainList);

		String result1 = loadBalancerService.passToscaToSelectedHLO(SERVICE_ID, TOSCA);
		simulateDomainServiceIncrement(DOMAIN_URL_2, testDomainList);
		String result2 = loadBalancerService.passToscaToSelectedHLO(SERVICE_ID, TOSCA);
		simulateDomainServiceIncrement(DOMAIN_URL_1, testDomainList);

		assertEquals(DOMAIN_URL_2, result1, "Load balancer should 1st assign request to Domain 1.");
		assertEquals(DOMAIN_URL_1, result2, "Load balancer should assign 2nd request to Domain 2.");

		result1 = loadBalancerService.passToscaToSelectedHLO(SERVICE_ID, TOSCA);
		simulateDomainServiceIncrement(DOMAIN_URL_2, testDomainList);
		result2 = loadBalancerService.passToscaToSelectedHLO(SERVICE_ID, TOSCA);
		simulateDomainServiceIncrement(DOMAIN_URL_1, testDomainList);

		assertEquals(DOMAIN_URL_2, result1, "Load balancer should repeat assignment of 3rd request to Domain 1.");
		assertEquals(DOMAIN_URL_1, result2, "Load balancer should repeat assignment of 4th request to Domain 2.");

		verify(hloClient, times(2)).passToscaToHLO(create(DOMAIN_URL_1), SERVICE_ID, TOSCA);
		verify(hloClient, times(2)).passToscaToHLO(create(DOMAIN_URL_2), SERVICE_ID, TOSCA);
	}

	@Test
	@DisplayName("Test passing tosca to HLO when two HLOs with unequal weights are available.")
	void testPassingToHLOWhenWeightsAreUnequal() {
		final List<Domain> testDomainList = new ArrayList<>(TWO_DOMAINS);
		when(domainService.getAllDomains()).thenReturn(testDomainList);

		String result1 = loadBalancerService.passToscaToSelectedHLO(SERVICE_ID, TOSCA);
		simulateDomainServiceIncrement(DOMAIN_URL_2, testDomainList);
		String result2 = loadBalancerService.passToscaToSelectedHLO(SERVICE_ID, TOSCA);
		simulateDomainServiceIncrement(DOMAIN_URL_2, testDomainList);

		assertEquals(DOMAIN_URL_2, result1, "Load balancer should 1st assign request to Domain 2.");
		assertEquals(DOMAIN_URL_2, result2, "Load balancer should assign 2nd request to Domain 2.");

		result1 = loadBalancerService.passToscaToSelectedHLO(SERVICE_ID, TOSCA);
		simulateDomainServiceIncrement(DOMAIN_URL_1, testDomainList);
		result2 = loadBalancerService.passToscaToSelectedHLO(SERVICE_ID, TOSCA);
		simulateDomainServiceIncrement(DOMAIN_URL_2, testDomainList);

		assertEquals(DOMAIN_URL_1, result1, "Load balancer should block Domain 2 and assign 3rd request to Domain 1.");
		assertEquals(DOMAIN_URL_2, result2, "Load balancer should unblock Domain 2 and assign to it 4th request.");

		verify(hloClient, times(3)).passToscaToHLO(create(DOMAIN_URL_2), SERVICE_ID, TOSCA);
		verify(hloClient, times(1)).passToscaToHLO(create(DOMAIN_URL_1), SERVICE_ID, TOSCA);
	}

	@Test
	@DisplayName("Test passing service ID to HLO when only one domain is present.")
	void testPassingServiceIdToHLOWithSingleDomain() {
		when(domainService.getAllDomains()).thenReturn(SINGLE_DOMAIN);

		String result = loadBalancerService.reOrchestrateServiceInToSelectedHLO(SERVICE_ID);

		assertEquals(DOMAIN_URL_1, result, "Load balancer should pass the 1st service Id to Domain 1.");

		loadBalancerService.reOrchestrateServiceInToSelectedHLO(SERVICE_ID);
		result = loadBalancerService.reOrchestrateServiceInToSelectedHLO(SERVICE_ID);

		assertEquals(DOMAIN_URL_1, result, "Upon repetition, load balancer should still pass the 2nd service Id to Domain 1.");
		verify(hloClient, times(3)).passServiceIdToHLO(create(DOMAIN_URL_1), SERVICE_ID);
	}

	@Test
	@DisplayName("Test passing service ID to HLO after request deployment and when two HLOs with unequal weights are available.")
	void testPassingServiceIdToHLOAfterDeployment() {
		final List<Domain> testDomainList = new ArrayList<>(TWO_DOMAINS);
		when(domainService.getAllDomains()).thenReturn(testDomainList);

		String result1 = loadBalancerService.passToscaToSelectedHLO(SERVICE_ID, TOSCA);
		simulateDomainServiceIncrement(DOMAIN_URL_2, testDomainList);
		String result2 = loadBalancerService.passToscaToSelectedHLO(SERVICE_ID, TOSCA);
		simulateDomainServiceIncrement(DOMAIN_URL_2, testDomainList);

		assertEquals(DOMAIN_URL_2, result1, "Load balancer should 1st assign request to Domain 2.");
		assertEquals(DOMAIN_URL_2, result2, "Load balancer should assign 2nd request to Domain 2.");

		result1 = loadBalancerService.passToscaToSelectedHLO(SERVICE_ID, TOSCA);
		simulateDomainServiceIncrement(DOMAIN_URL_1, testDomainList);
		result2 = loadBalancerService.reOrchestrateServiceInToSelectedHLO(SERVICE_ID);
		simulateDomainServiceIncrement(DOMAIN_URL_2, testDomainList);

		assertEquals(DOMAIN_URL_1, result1, "Load balancer should block Domain 2 and assign 3rd request to Domain 1.");
		assertEquals(DOMAIN_URL_2, result2, "Load balancer should unblock Domain 2 and assign to it 4th service Id.");

		verify(hloClient, times(2)).passToscaToHLO(create(DOMAIN_URL_2), SERVICE_ID, TOSCA);
		verify(hloClient, times(1)).passToscaToHLO(create(DOMAIN_URL_1), SERVICE_ID, TOSCA);
		verify(hloClient, times(1)).passServiceIdToHLO(create(DOMAIN_URL_2), SERVICE_ID);
	}

	private void simulateDomainServiceIncrement(final String domainUrl, final List<Domain> initialDomains) {
		final Domain updatedDomain = initialDomains.stream().filter(domain -> domain.getPublicUrl().equals(domainUrl))
				.findFirst().orElseThrow();
		initialDomains.remove(updatedDomain);
		initialDomains.add(updatedDomain.toBuilder()
				.activeConnections(updatedDomain.getActiveConnections() + 1)
				.domainScore((updatedDomain.getActiveConnections() + 1) / updatedDomain.getWeight()).build());
	}
}
