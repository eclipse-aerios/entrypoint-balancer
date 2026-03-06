package org.aeros.service;

import static java.util.Comparator.comparingDouble;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.aeros.exception.domain.NoDomainFoundException;
import org.aeros.service.domain.Domain;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.Getter;

@Service
public class LoadBalancerServiceImpl implements LoadBalancerService {

	private static final Logger logger = getLogger(LoadBalancerServiceImpl.class);

	private final DomainCacheService domainCache;
	private final HLOService hloService;

	protected final AtomicInteger overloadCounter;
	protected Domain lastDomainSelection;
	protected boolean isOverloadDetected;

	@Getter
	@Value("${load-balancer.max-assignments}")
	private int maxAssignments;

	public LoadBalancerServiceImpl(final DomainCacheService domainCache, final HLOService hloService) {
		this.domainCache = domainCache;
		this.hloService = hloService;
		this.overloadCounter = new AtomicInteger(0);
		this.isOverloadDetected = false;
	}

	@Override
	public String passToscaToSelectedHLO(final String serviceId, final String toscaRest) {
		final Domain selectedDomain = selectHLOForService();
		return hloService.passToscaToHLO(selectedDomain.getPublicUrl(), serviceId, toscaRest);
	}

	@Override
	public String reOrchestrateServiceInToSelectedHLO(String serviceId) {
		final Domain selectedDomain = selectHLOForService();
		return hloService.passServiceIdToHLO(selectedDomain.getPublicUrl(), serviceId);
	}

	@Override
	public void changeAssignmentLimit(int newLimit) {
		maxAssignments = newLimit;
		logger.info("Number of maximal assignments was changed to {}.", maxAssignments);
	}

	private Domain selectHLOForService() {
		domainCache.refreshDomainsCache();

		final List<Domain> availableDomains = domainCache.getAvailableDomains();
		final Domain selectedDomain = availableDomains.stream()
				.min(comparingDouble(Domain::getDomainScore))
				.orElseThrow(NoDomainFoundException::new);
		final boolean hasMultipleDomains = availableDomains.size() > 1;

		if (isOverloadDetected) {
			handleDistributionOverload();
		} else if (hasMultipleDomains) {
			checkForDistributionOverload(selectedDomain);
		}

		updateOffloadingInformation(selectedDomain);
		return selectedDomain;
	}

	private void checkForDistributionOverload(final Domain selectedDomain) {
		ofNullable(lastDomainSelection)
				.filter(domain -> domain.equals(selectedDomain) && overloadCounter.incrementAndGet() >= maxAssignments)
				.ifPresentOrElse(this::markDomainAsOverloaded, () -> updateLastDomainSelection(selectedDomain));
	}

	private void handleDistributionOverload() {
		if (overloadCounter.decrementAndGet() < 1) {
			domainCache.enableDomain(lastDomainSelection);
			isOverloadDetected = false;
		}
	}

	private void markDomainAsOverloaded(final Domain domain) {
		domainCache.disableDomain(lastDomainSelection);
		isOverloadDetected = true;
		overloadCounter.set(maxAssignments - 1);
	}

	private void updateLastDomainSelection(final Domain selectedDomain) {
		if(isNull(lastDomainSelection) || !lastDomainSelection.equals(selectedDomain)) {
			lastDomainSelection = selectedDomain;
			overloadCounter.set(1);
		}
	}

	private void updateOffloadingInformation(final Domain selectedDomain) {
		domainCache.increaseOffloadedRequestCount(selectedDomain);

		domainCache.getCurrentOffloadingRatio()
				.ifPresentOrElse(ratio -> logger.info("Current average offloading ratio is: {}", ratio),
						() -> logger.info("Offloading ratio is not applicable (infrastructure contains a single domain)"));

	}

}
