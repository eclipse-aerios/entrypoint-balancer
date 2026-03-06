package org.aeros.service;

import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.aeros.service.domain.Domain;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import lombok.Getter;

@Service
@Getter
public class DomainCacheServiceImpl implements DomainCacheService {

	private static final Logger logger = getLogger(DomainCacheServiceImpl.class);

	protected final Map<Domain, Boolean> cache;
	protected final Map<Domain, Long> offloadedRequestsCount;
	protected final AtomicLong totalRequestsCount;
	protected final DomainService domainService;

	public DomainCacheServiceImpl(final DomainService domainService) {
		this.totalRequestsCount = new AtomicLong(0);
		this.domainService = domainService;
		this.cache = new HashMap<>();
		this.offloadedRequestsCount = new HashMap<>();
	}

	@Override
	public void refreshDomainsCache() {
		final Map<Domain, Boolean> updatedMap = domainService.getAllDomains().stream()
				.collect(toMap(domain -> domain, this::getAvailability));

		cache.clear();
		cache.putAll(updatedMap);

		updatedMap.keySet().forEach(domain -> offloadedRequestsCount.putIfAbsent(domain, 0L));
	}

	@Override
	public void disableDomain(final Domain domain) {
		cache.replace(domain, false);
	}

	@Override
	public void enableDomain(final Domain domain) {
		cache.replace(domain, true);
	}

	@Override
	public void increaseOffloadedRequestCount(Domain domain) {
		offloadedRequestsCount.put(domain, offloadedRequestsCount.getOrDefault(domain, 0L) + 1);
		totalRequestsCount.addAndGet(1);
	}

	@Override
	public List<Domain> getAvailableDomains() {
		return cache.entrySet().stream()
				.filter(Map.Entry::getValue)
				.map(Map.Entry::getKey)
				.toList();
	}

	@Override
	public Optional<Double> getCurrentOffloadingRatio() {
		final boolean hasSingleDomain = cache.size() <= 1;

		if (hasSingleDomain) {
			return Optional.empty();
		}

		return Optional.of(offloadedRequestsCount)
				.map(this::computeOffloadingRatio)
				.or(() -> Optional.of(0D));
	}

	private Double computeOffloadingRatio(final Map<Domain, Long> domainsRequests) {
		final long maxOffloaded = domainsRequests.values().stream()
				.mapToLong(Long::longValue)
				.max()
				.orElse(0L);

		return 1d - ((double) maxOffloaded / totalRequestsCount.longValue()) + 0.1;
	}

	private Boolean getAvailability(final Domain domain) {
		return cache.getOrDefault(domain, true);
	}
}
