package org.aeros.service;

import java.util.List;

import org.aeros.service.domain.Domain;
import org.aeros.service.domain.WeightingFunctionType;

/**
 * Service with methods responsible for the communication with Orion client.
 */
public interface DomainService {

	/**
	 * Method returns a list of all current continuum domains.
	 *
	 * @return List of domain
	 */
	List<Domain> getAllDomains();

	/**
	 * Method retrieves current type of weighting function.
	 *
	 * @return current type of weighting function.
	 */
	WeightingFunctionType getWeightingFunctionType();

	/**
	 * Method changes applied weighting function.
	 */
	void changeWeightingFunction(final String newWeightingFunctionType);
}
