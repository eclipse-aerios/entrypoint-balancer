package org.aeros.exception.domain;

import static java.lang.String.format;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * Exception thrown when HLO could not initialize a service.
 */
@Getter
public class HLOClientException extends RuntimeException {

	final HttpStatus status;

	public HLOClientException(final String message, final HttpStatus status) {
		super(format("Could not initialize service in HLO. Root cause: %s.", message));
		this.status = status;
	}
}
