package org.aeros.service;

import static org.aeros.fixtures.DomainFixtures.DOMAIN_URL_1;
import static org.aeros.fixtures.DomainFixtures.DOMAIN_URL_2;
import static org.aeros.fixtures.DomainFixtures.prepareDomainRest1;
import static org.aeros.fixtures.DomainFixtures.prepareDomainRest2;
import static org.aeros.fixtures.InfrastructureElementFixtures.TWO_INFRASTRUCTURE_ELEMENTS;
import static org.aeros.fixtures.ServiceFixtures.SERVICES_IE_1;
import static org.aeros.fixtures.ServiceFixtures.SERVICES_IE_2;
import static org.aeros.service.domain.WeightingFunctionType.RAM_AND_CPU;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import org.aeros.exception.domain.UnsupportedWeightingFunctionType;
import org.aeros.feign.OrionClient;
import org.aeros.rest.domain.InfrastructureElementRest;
import org.aeros.rest.domain.ServiceComponentRest;
import org.aeros.service.domain.Domain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class DomainServiceTest {

	@MockBean
	private OrionClient orionClient;

	@Autowired
	private DomainService domainService;

	@Test
	@DisplayName("Test getting all domains when there are not infrastructure elements.")
	void testAllDomainsWhenNoInfrastructureElements() {
		when(orionClient.getInfrastructureElements()).thenReturn(new InfrastructureElementRest[] {});
		assertTrue(domainService.getAllDomains().isEmpty());
	}

	@Test
	@DisplayName("Test getting all domains when there are infrastructure elements and services.")
	void testAllDomainsForInfrastructureElements() {
		prepareOrionClient();

		final List<Domain> result = domainService.getAllDomains();
		final Domain domain1 = result.stream()
				.filter(domain -> domain.getPublicUrl().equals(DOMAIN_URL_1))
				.findFirst()
				.orElseThrow();
		final Domain domain2 = result.stream()
				.filter(domain -> domain.getPublicUrl().equals(DOMAIN_URL_2))
				.findFirst()
				.orElseThrow();

		assertEquals(2, result.size(), "There should be 2 domains.");
		assertAll(() -> {
			assertEquals(1, domain1.getActiveConnections(), "Domain 1 should have 1 active service connected.");
			assertEquals(0.8, domain1.getWeight(), "Domain 1 should have weight 0.8.");
		});
		assertAll(() -> {
			assertEquals(1, domain2.getActiveConnections(), "Domain 2 should have 1 active service connected.");
			assertEquals(0.42, domain2.getWeight(), "Domain 2 should have weight 0.42.");
		});
	}

	@Test
	@DisplayName("Test getting all domains when there are infrastructure elements and services for RAM weighting function.")
	void testAllDomainsForInfrastructureElementsWithRamWeighting() {
		prepareOrionClient();
		domainService.changeWeightingFunction("RAM_AND_CPU");

		final List<Domain> result = domainService.getAllDomains();
		final Domain domain1 = result.stream()
				.filter(domain -> domain.getPublicUrl().equals(DOMAIN_URL_1))
				.findFirst()
				.orElseThrow();
		final Domain domain2 = result.stream()
				.filter(domain -> domain.getPublicUrl().equals(DOMAIN_URL_2))
				.findFirst()
				.orElseThrow();

		assertEquals(2, result.size(), "There should be 2 domains.");
		assertAll(() -> {
			assertEquals(1, domain1.getActiveConnections(), "Domain 1 should have 1 active service connected.");
			assertEquals(0.71, domain1.getWeight(), 0.01, "Domain 1 should have weight 0.71.");
		});
		assertAll(() -> {
			assertEquals(1, domain2.getActiveConnections(), "Domain 2 should have 1 active service connected.");
			assertEquals(0.42, domain2.getWeight(), 0.01, "Domain 2 should have weight 0.42.");
		});
	}

	@Test
	@DisplayName("Test change weighting function configuration for unsupported value.")
	void testChangeWeightingFunctionWhenUnsupported() {
		assertThrows(UnsupportedWeightingFunctionType.class, () -> domainService.changeWeightingFunction("WRONG"));
	}

	@Test
	@DisplayName("Test change weighting function configuration.")
	void testChangeWeightingFunction() {
		assertDoesNotThrow(() -> domainService.changeWeightingFunction("RAM_AND_CPU"));
		assertEquals(RAM_AND_CPU, domainService.getWeightingFunctionType(),
				"Weighting function type should be RAM_AND_CPU after configuration update.");
	}

	private void prepareOrionClient() {
		when(orionClient.getInfrastructureElements()).thenReturn(TWO_INFRASTRUCTURE_ELEMENTS);
		when(orionClient.getDomainById("urn:ngsi-ld:Domain:01")).thenReturn(prepareDomainRest1());
		when(orionClient.getDomainById("urn:ngsi-ld:Domain:02")).thenReturn(prepareDomainRest2());
		when(orionClient.getServicesForIE("urn:ngsi-ld:InfrastructureElement:01")).thenReturn(SERVICES_IE_1);
		when(orionClient.getServicesForIE("urn:ngsi-ld:InfrastructureElement:02")).thenReturn(SERVICES_IE_2);
		when(orionClient.getServicesForIE("urn:ngsi-ld:InfrastructureElement:03")).thenReturn(
				new ServiceComponentRest[] {});
	}
}
