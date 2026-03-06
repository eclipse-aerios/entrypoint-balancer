package org.aeros.service;

import static org.aeros.fixtures.DomainFixtures.SINGLE_DOMAIN;
import static org.aeros.fixtures.DomainFixtures.TWO_ALTERNATED_DOMAINS;
import static org.aeros.fixtures.DomainFixtures.TWO_DOMAINS;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

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
public class DomainCacheServiceTest {

	@MockBean
	private DomainService domainService;

	@Autowired
	private DomainCacheService domainCacheService;

	@BeforeEach
	public void setUp() {
		domainCacheService.getCache().clear();
		domainCacheService.getOffloadedRequestsCount().clear();
		domainCacheService.getTotalRequestsCount().set(0L);
	}

	@Test
	@DisplayName("Test disabling domain.")
	void testDisableDomain() {
		when(domainService.getAllDomains()).thenReturn(TWO_DOMAINS);
		final Domain domainToDisable = TWO_DOMAINS.get(0);

		domainCacheService.refreshDomainsCache();
		assertEquals(2, domainCacheService.getAvailableDomains().size(), "Cache should contain 2 available domains.");

		domainCacheService.disableDomain(domainToDisable);
		assertEquals(1, domainCacheService.getAvailableDomains().size(), "Cache should contain 1 available domain.");
		assertFalse(domainCacheService.getCache().get(domainToDisable));
	}

	@Test
	@DisplayName("Test enabling domain.")
	void testEnablingDomain() {
		when(domainService.getAllDomains()).thenReturn(TWO_DOMAINS);
		final Domain domainToDisable = TWO_DOMAINS.get(0);

		domainCacheService.refreshDomainsCache();
		assertEquals(2, domainCacheService.getAvailableDomains().size(), "Cache should contain 2 available domains.");

		domainCacheService.disableDomain(domainToDisable);
		assertEquals(1, domainCacheService.getAvailableDomains().size(), "Cache should contain 1 available domain.");

		domainCacheService.enableDomain(domainToDisable);
		assertEquals(2, domainCacheService.getAvailableDomains().size(),
				"Cache should again have 2 available domains.");
	}

	@Test
	@DisplayName("Test refresh domains cache for empty cache.")
	void testRefreshCacheWhenCacheIsEmpty() {
		when(domainService.getAllDomains()).thenReturn(TWO_DOMAINS);

		assertTrue(domainCacheService.getAvailableDomains().isEmpty(), "Cache should be initially empty.");

		domainCacheService.refreshDomainsCache();
		assertEquals(2, domainCacheService.getAvailableDomains().size(),
				"After refreshing, cache should have 2 available domains.");
	}

	@Test
	@DisplayName("Test refresh domains cache for non-empty cache.")
	void testRefreshCacheWhenCacheIsNotEmpty() {
		when(domainService.getAllDomains()).thenReturn(TWO_DOMAINS);
		final Domain alteredDomain = TWO_DOMAINS.get(0);
		final Domain unchangedDomain = TWO_DOMAINS.get(1);

		domainCacheService.refreshDomainsCache();
		assertAll(() -> {
			final Domain domain1 = domainCacheService.getCache().keySet().stream()
					.filter(domain -> domain.equals(alteredDomain)).findFirst().orElseThrow();

			assertEquals(2, domainCacheService.getAvailableDomains().size(), "Cache should have 2 available domains.");
			assertEquals(5, domain1.getActiveConnections(), "Domain 1 should have initially 5 connections.");
			assertTrue(domainCacheService.getCache().get(alteredDomain), "Domain 1 should be initially enabled.");
		});

		when(domainService.getAllDomains()).thenReturn(TWO_ALTERNATED_DOMAINS);
		domainCacheService.disableDomain(alteredDomain);

		domainCacheService.refreshDomainsCache();
		assertAll(() -> {
			final Domain domain1 = domainCacheService.getCache().keySet().stream()
					.filter(domain -> domain.equals(alteredDomain)).findFirst().orElseThrow();
			final Domain domain2 = domainCacheService.getCache().keySet().stream()
					.filter(domain -> domain.equals(unchangedDomain)).findFirst().orElseThrow();

			assertEquals(1, domainCacheService.getAvailableDomains().size(), "Cache should have 1 available domain.");
			assertEquals(20, domain1.getActiveConnections(), "Domain 1 should update the number of connections to 20.");
			assertEquals(10, domain2.getActiveConnections(), "Domain 2 should still have 10 connections.");
			assertFalse(domainCacheService.getCache().get(alteredDomain), "Domain 1 should remain disabled.");
			assertTrue(domainCacheService.getCache().get(unchangedDomain), "Domain 2 should remain enabled.");
		});
	}

	@Test
	@DisplayName("Test refresh domains cache with domain removed.")
	void testRefreshCacheWhenDomainIsRemoved() {
		when(domainService.getAllDomains()).thenReturn(TWO_DOMAINS);

		final Domain domainToRemove = TWO_DOMAINS.get(1);

		domainCacheService.refreshDomainsCache();
		assertAll(() -> {
			assertEquals(2, domainCacheService.getCache().size(), "Cache should have 2 available domains.");
			assertTrue(domainCacheService.getCache().containsKey(domainToRemove), "Domain 2 should be in cache.");
		});

		when(domainService.getAllDomains()).thenReturn(SINGLE_DOMAIN);

		domainCacheService.refreshDomainsCache();
		assertAll(() -> {
			assertEquals(1, domainCacheService.getCache().size(), "After refresh, cache should have 1 domain.");
			assertFalse(domainCacheService.getCache().containsKey(domainToRemove), "Domain 2 should not be in cache.");
		});
	}

	@Test
	@DisplayName("Test increase domain request count when initially empty.")
	void testIncreaseDomainRequestCountWhenEmpty() {
		final Domain testDomain = TWO_DOMAINS.get(0);

		assertAll(() -> {
			assertTrue(domainCacheService.getOffloadedRequestsCount().isEmpty(),
					"Cache should be initially empty.");
			assertEquals(0, domainCacheService.getTotalRequestsCount().get(),
					"Total number of offloaded requests should be initially zero.");
		});

		domainCacheService.increaseOffloadedRequestCount(testDomain);

		assertAll(() -> {
			assertEquals(1, domainCacheService.getOffloadedRequestsCount().size(),
					"After increasing request count, cache should be populated.");
			assertEquals(1L, domainCacheService.getOffloadedRequestsCount().get(testDomain),
					"Test domain should be assigned 1 request.");
			assertEquals(1L, domainCacheService.getTotalRequestsCount().get(),
					"Number of total requests should be increased to 1");
		});
	}

	@Test
	@DisplayName("Test increase domain request count when already present.")
	void testIncreaseDomainRequestCountWhenAlreadyPresent() {
		final Domain testDomain = TWO_DOMAINS.get(0);

		domainCacheService.increaseOffloadedRequestCount(testDomain);

		assertEquals(1, domainCacheService.getOffloadedRequestsCount().size(),
				"Size of the cache should be 1 after increasing the number of requests.");

		domainCacheService.increaseOffloadedRequestCount(testDomain);

		assertAll(() -> {
			assertEquals(1, domainCacheService.getOffloadedRequestsCount().size(),
					"After increasing request count, size of the cache should remain the same.");
			assertEquals(2L, domainCacheService.getOffloadedRequestsCount().get(testDomain),
					"Test domain should be assigned 2 requests.");
			assertEquals(2L, domainCacheService.getTotalRequestsCount().get(),
					"Number of total requests should be increased to 2");
		});

	}

	@Test
	@DisplayName("Test increase domain request count when other domain is present.")
	void testIncreaseDomainRequestCountWhenOtherDomainIsPresent() {
		final Domain testDomain = TWO_DOMAINS.get(0);
		final Domain testDomain2 = TWO_DOMAINS.get(1);

		domainCacheService.increaseOffloadedRequestCount(testDomain);
		domainCacheService.increaseOffloadedRequestCount(testDomain2);

		assertEquals(2, domainCacheService.getOffloadedRequestsCount().size(),
				"Size of the cache should be 2 after increasing the number of requests for 2 domains.");

		domainCacheService.increaseOffloadedRequestCount(testDomain2);

		assertAll(() -> {
			assertEquals(2, domainCacheService.getOffloadedRequestsCount().size(),
					"After increasing request count, size of the cache should remain the same.");
			assertEquals(1L, domainCacheService.getOffloadedRequestsCount().get(testDomain),
					"Test domain 1 should be assigned 1 request.");
			assertEquals(2L, domainCacheService.getOffloadedRequestsCount().get(testDomain2),
					"Test domain 2 should be assigned 2 request.");
			assertEquals(3L, domainCacheService.getTotalRequestsCount().get(),
					"Number of total requests should be increased to 3");
		});
	}

	@Test
	@DisplayName("Test computing offloading ratio for no domains in system.")
	void testGetCurrentOffloadingRatioWhenNoDomainInSystem() {
		assertTrue(domainCacheService.getCurrentOffloadingRatio().isEmpty(),
				"Offloading ratio should not be computed when there are no domains.");
	}

	@Test
	@DisplayName("Test computing offloading ratio for single domain in system.")
	void testGetCurrentOffloadingRatioWhenSingleDomainInSystem() {
		when(domainService.getAllDomains()).thenReturn(List.of(TWO_DOMAINS.get(0)));
		domainCacheService.refreshDomainsCache();

		final Domain testDomain = TWO_DOMAINS.get(0);
		domainCacheService.increaseOffloadedRequestCount(testDomain);

		assertTrue(domainCacheService.getCurrentOffloadingRatio().isEmpty(),
				"Offloading ratio should not be computed when there is a single domain.");
	}

	@Test
	@DisplayName("Test computing offloading ratio for single domain utilized.")
	void testGetCurrentOffloadingRatioWhenSingleDomainUtilized() {
		when(domainService.getAllDomains()).thenReturn(TWO_DOMAINS);
		domainCacheService.refreshDomainsCache();

		final Domain testDomain = TWO_DOMAINS.get(0);
		domainCacheService.increaseOffloadedRequestCount(testDomain);
		domainCacheService.increaseOffloadedRequestCount(testDomain);

		assertAll(() -> {
			assertFalse(domainCacheService.getOffloadedRequestsCount().isEmpty(),
					"The offloading ratio should be computed for multiple domains present.");
			assertEquals(0.1, domainCacheService.getCurrentOffloadingRatio().get(),
					"Offloading ratio should be 0% when a single domain is utilized.");
		});
	}

	@Test
	@DisplayName("Test computing offloading ratio for multiple domains.")
	void testGetCurrentOffloadingRatioWhenMultipleDomains() {
		when(domainService.getAllDomains()).thenReturn(TWO_DOMAINS);
		domainCacheService.refreshDomainsCache();

		final Domain testDomain = TWO_DOMAINS.get(0);
		final Domain testDomain2 = TWO_DOMAINS.get(1);
		domainCacheService.increaseOffloadedRequestCount(testDomain);
		domainCacheService.increaseOffloadedRequestCount(testDomain2);
		domainCacheService.increaseOffloadedRequestCount(testDomain2);

		assertAll(() -> {
			assertFalse(domainCacheService.getOffloadedRequestsCount().isEmpty(),
					"The offloading ratio should be computed for multiple domains present.");
			assertEquals(0.49, domainCacheService.getCurrentOffloadingRatio().get(), 0.1,
					"Offloading ratio should be 0% when a single domain is utilized.");
		});
	}
}
