package org.aeros.mapper;

import org.aeros.rest.domain.DomainRest;
import org.aeros.service.domain.Domain;

/**
 * Mapper between REST and business Domain objects.
 */
public class DomainMapper {

	/**
	 * Map to parameters of DomainRest to Domain.
	 *
	 * @param domain            REST domain object
	 * @param weight            weight assigned to the given domain
	 * @param connectionsNumber number of active connections
	 * @return business Domain object
	 */
	public static Domain map(final DomainRest domain, final double weight, final long connectionsNumber) {
		return Domain.builder()
				.publicUrl(domain.getPublicUrl())
				.activeConnections(connectionsNumber)
				.weight(weight)
				.domainScore(connectionsNumber / weight)
				.build();
	}
}
