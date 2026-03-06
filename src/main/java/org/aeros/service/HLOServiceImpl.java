package org.aeros.service;

import static java.net.URI.create;

import org.aeros.exception.domain.HLOClientException;
import org.aeros.feign.HLOClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import feign.FeignException;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class HLOServiceImpl implements HLOService {

	private final HLOClient hloClient;

	@Override
	public String passToscaToHLO(final String baseURL, final String serviceId, final String tosca) {
		try {
			hloClient.passToscaToHLO(create(baseURL), serviceId, tosca);
		} catch (final FeignException exception) {
			throw new HLOClientException(exception.getMessage(), HttpStatus.valueOf(exception.status()));
		}
		return baseURL;
	}

	@Override
	public String passServiceIdToHLO(final String baseURL, final String serviceId) {
		try {
			hloClient.passServiceIdToHLO(create(baseURL), serviceId);
		} catch (final FeignException exception) {
			throw new HLOClientException(exception.getMessage(), HttpStatus.valueOf(exception.status()));
		}
		return baseURL;
	}
}
