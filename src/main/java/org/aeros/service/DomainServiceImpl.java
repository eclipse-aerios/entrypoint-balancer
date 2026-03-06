package org.aeros.service;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.groupingBy;
import static org.aeros.mapper.DomainMapper.map;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.aeros.exception.domain.UnsupportedWeightingFunctionType;
import org.aeros.feign.OrionClient;
import org.aeros.rest.domain.InfrastructureElementRest;
import org.aeros.rest.domain.ServiceComponentRest;
import org.aeros.service.domain.Domain;
import org.aeros.service.domain.WeightingFunctionType;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.Getter;

@Service
public class DomainServiceImpl implements DomainService {

	private static final Logger logger = getLogger(DomainServiceImpl.class);
	private static final String STATUS_PREFIX = "urn:ngsi-ld:ServiceComponentStatus:%s";

	private final OrionClient orionClient;

	@Getter
	@Value("${load-balancer.weighting-function}")
	private WeightingFunctionType weightingFunctionType;

	public DomainServiceImpl(final OrionClient orionClient) {
		this.orionClient = orionClient;
	}

	@Override
	public List<Domain> getAllDomains() {
		final Map<String, List<InfrastructureElementRest>> domainsMap = stream(orionClient.getInfrastructureElements())
				.collect(groupingBy(InfrastructureElementRest::getDomain));

		return domainsMap.entrySet().stream()
				.map(entry -> map(
						orionClient.getDomainById(entry.getKey()),
						computeWeight(entry.getValue()),
						getActiveConnections(entry.getValue())))
				.toList();
	}

	@Override
	public void changeWeightingFunction(final String newWeightingFunctionType) {
		try {
			weightingFunctionType = WeightingFunctionType.valueOf(newWeightingFunctionType);
			logger.info("Weighting function was changed to {}.", newWeightingFunctionType);
		} catch (final IllegalArgumentException e) {
			throw new UnsupportedWeightingFunctionType(newWeightingFunctionType);
		}
	}

	private long getActiveConnections(final List<InfrastructureElementRest> infrastructureElements) {
		final Predicate<ServiceComponentRest> isFinished = serviceComponent ->
				serviceComponent.getServiceComponentStatus().equals(format(STATUS_PREFIX, "Finished"));

		return infrastructureElements.stream()
				.map(infrastructureElement -> orionClient.getServicesForIE(infrastructureElement.getId()))
				.flatMap(Arrays::stream)
				.filter(not(isFinished))
				.count();
	}

	private double computeWeight(final List<InfrastructureElementRest> infrastructureElements) {
		return switch (weightingFunctionType) {
			case CPU -> computeWeightUsingCPUCores(infrastructureElements);
			case RAM_AND_CPU -> computeWeightUsingCPUCoresAndRAM(infrastructureElements);
		};
	}

	private double computeWeightUsingCPUCores(final List<InfrastructureElementRest> infrastructureElements) {
		final long totalCpuCores = infrastructureElements.stream()
				.mapToLong(InfrastructureElementRest::getCpuCores)
				.sum();
		final double totalAvailableCpuCores = infrastructureElements.stream()
				.mapToDouble(this::getAvailableCpuCores)
				.sum();

		return totalAvailableCpuCores / totalCpuCores;
	}

	private double computeWeightUsingCPUCoresAndRAM(final List<InfrastructureElementRest> infrastructureElements) {
		final long totalCpuCores = infrastructureElements.stream()
				.mapToLong(InfrastructureElementRest::getCpuCores)
				.sum();
		final double totalAvailableCpuCores = infrastructureElements.stream()
				.mapToDouble(this::getAvailableCpuCores)
				.sum();
		final long totalRam = infrastructureElements.stream()
				.mapToLong(InfrastructureElementRest::getRamCapacity)
				.sum();
		final double totalAvailableRam = infrastructureElements.stream()
				.mapToDouble(InfrastructureElementRest::getAvailableRam)
				.sum();

		final double cpuPercentage = totalAvailableCpuCores / totalCpuCores;
		final double ramPercentage = totalAvailableRam / totalRam;

		return (cpuPercentage + ramPercentage) / 2;
	}

	private double getAvailableCpuCores(final InfrastructureElementRest infrastructureElement) {
		return infrastructureElement.getCpuCores() * (1 - ((double) infrastructureElement.getCurrentCpuUsage() / 100));
	}
}
