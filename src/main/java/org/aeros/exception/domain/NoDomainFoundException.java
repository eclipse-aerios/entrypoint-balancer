package org.aeros.exception.domain;

/**
 * Exception thrown when no domain was found by LoadBalancer.
 */
public class NoDomainFoundException extends RuntimeException {

	public NoDomainFoundException() {
		super("Couldn't find the HLO to allocate the request.");
	}
}
