package org.aeros.exception.domain;

import static java.lang.String.format;

/**
 * Exception thrown when a function type which is not supported by the system is given in the configuration.
 */
public class UnsupportedWeightingFunctionType  extends RuntimeException {

	public UnsupportedWeightingFunctionType(final String type) {
		super(format("Type %s of weighting function is not supported.", type));
	}
}

