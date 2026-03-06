package org.aeros.exception;

import static java.lang.String.format;
import static java.lang.String.join;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

import org.aeros.exception.domain.HLOClientException;
import org.aeros.exception.domain.NoDomainFoundException;
import org.aeros.exception.domain.UnsupportedWeightingFunctionType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler({ NoDomainFoundException.class })
	public ResponseEntity<Object> handleNoDomainFound(final NoDomainFoundException exception) {
		logException(exception.getMessage());
		return ResponseEntity.status(NOT_FOUND).body(exception.getMessage());
	}

	@ExceptionHandler({ HLOClientException.class })
	public ResponseEntity<Object> handleServiceNotInitialized(final HLOClientException exception) {
		logException(exception.getMessage());
		return ResponseEntity.status(exception.getStatus()).body(exception.getLocalizedMessage());
	}

	@ExceptionHandler({ UnsupportedWeightingFunctionType.class })
	public ResponseEntity<Object> handleUnsupportedWeightingFunctionType(final UnsupportedWeightingFunctionType exception) {
		logException(exception.getMessage());
		return ResponseEntity.status(NOT_ACCEPTABLE).body(exception.getLocalizedMessage());
	}

	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(final HttpMediaTypeNotSupportedException ex,
			final HttpHeaders headers, final HttpStatusCode status, final WebRequest request) {
		final String messageToReturn = format("Unsupported Media Type. Expected type: %s. Received type: %s.",
				join(",", headers.getAccept().stream().map(MediaType::toString).toList()),
				request.getHeader("Content-Type"));
		logException(messageToReturn);
		return ResponseEntity.status(UNSUPPORTED_MEDIA_TYPE).body(messageToReturn);
	}

	private void logException(final String messageToLog) {
		logger.warn(format("Exception thrown: %s", messageToLog));
	}
}
