package org.aeros.mapper;

import static org.aeros.fixtures.DomainFixtures.DOMAIN_URL_1;
import static org.aeros.fixtures.DomainFixtures.prepareDomainRest1;
import static org.aeros.mapper.DomainMapper.map;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.aeros.rest.domain.DomainRest;
import org.aeros.service.domain.Domain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DomainMapperTest {

	@Test
	@DisplayName("Test mapping from DomainRest to Domain.")
	void testDomainMapperMap() {
		final DomainRest testDomainRest = prepareDomainRest1();
		final Domain result = map(testDomainRest, 0.2, 10);

		assertEquals(DOMAIN_URL_1, result.getPublicUrl());
		assertEquals(10, result.getActiveConnections());
		assertEquals(0.2, result.getWeight());
		assertEquals(50, result.getDomainScore());
	}
}
