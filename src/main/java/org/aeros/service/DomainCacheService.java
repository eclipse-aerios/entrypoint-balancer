package org.aeros.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.aeros.service.domain.Domain;

/**
 * Service managing the cache storing information about HLO connections.
 */
public interface DomainCacheService {

	/**
	 * Method updates cache according to the list of current domains.
	 */
	void refreshDomainsCache();

	/**
	 * Method temporarily disables a given domain from the selection.
	 *
	 * @param domain domain to be disabled
	 */
	void disableDomain(final Domain domain);

	/**
	 * Method makes a given domain available for the selection.
	 *
	 * @param domain domain to be made available
	 */
	void enableDomain(final Domain domain);

	/**
	 * Methods increases the counter of offloaded requests for a particular domain.
	 *
	 * @param domain domain to which the request was offloaded
	 */
	void increaseOffloadedRequestCount(final Domain domain);

	/**
	 * Method returns list of domains that are currently marked as available.
	 *
	 * @return List of domains
	 */
	List<Domain> getAvailableDomains();

	/**
	 * Method returns the entire cached domain map.
	 *
	 * @return Map of domains
	 * @apiNote Method is primarily used for testing.
	 */
	Map<Domain, Boolean> getCache();

	/**
	 * Method returns the entire domain offloading requests map.
	 *
	 * @return Map of domains
	 * @apiNote Method is primarily used for testing.
	 */
	Map<Domain, Long> getOffloadedRequestsCount();

	/**
	 * Method returns the total number of offloaded requests
	 *
	 * @return number of offloaded requests
	 * @apiNote Method is primarily used for testing.
	 */
	AtomicLong getTotalRequestsCount();

	/**
	 * Method returns current offloading ratio.
	 * <p/> If there is a single domain available then offloading ratio is empty (cannot be computed due to single domain).
	 * <p/> If only a single domain was receiving requests, then the offloading ratio is 0.
	 * <p/> In other case, the offloading ratio is computed as an average of percentage of offloaded requests across remaining domains.
	 *
	 * @return percentage offloading rati
	 */
	Optional<Double> getCurrentOffloadingRatio();
}
