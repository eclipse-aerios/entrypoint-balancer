package org.aeros.fixtures;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static java.nio.charset.Charset.defaultCharset;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.StreamUtils.copyToString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.aeros.rest.domain.DomainRest;
import org.aeros.service.domain.Domain;
import org.springframework.http.HttpStatus;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;

public class DomainFixtures {

	public static final String DOMAIN_URL_1 = "https://test-domain-1.aeros-project.eu";
	public static final List<Domain> SINGLE_DOMAIN = List.of(prepareDomain1());
	public static final String DOMAIN_URL_2 = "https://test-domain-2.aeros-project.eu";
	public static final List<Domain> TWO_DOMAINS = List.of(prepareDomain1(), prepareDomain2());
	public static final List<Domain> TWO_EQUIVALENT_DOMAINS = List.of(
			prepareDomain1(),
			prepareDomain2().toBuilder().weight(0.1).activeConnections(5).domainScore(50).build()
	);
	public static final List<Domain> TWO_ALTERNATED_DOMAINS = List.of(
			prepareDomain1().toBuilder().activeConnections(20).build(),
			prepareDomain2()
	);

	public static DomainRest prepareDomainRest1() {
		return new DomainRest(
				"urn:ngsi-ld:Domain:01",
				"test domain rest 1",
				DOMAIN_URL_1,
				new ArrayList<>(List.of("owner1", "owner2")),
				false,
				"Functional"
		);
	}

	public static DomainRest prepareDomainRest2() {
		return new DomainRest(
				"urn:ngsi-ld:Domain:02",
				"test domain rest 2",
				DOMAIN_URL_2,
				new ArrayList<>(),
				true,
				"Functional"
		);
	}

	public static ResponseDefinitionBuilder prepareDomainRest1Response() throws IOException {
		return aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader("Content-Type", APPLICATION_JSON_VALUE)
				.withBody(copyToString(DomainFixtures.class.getClassLoader()
								.getResourceAsStream("test-responses/get-domain-1-response.json"),
						defaultCharset()));
	}

	public static ResponseDefinitionBuilder prepareDomainRest2Response() throws IOException {
		return aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader("Content-Type", APPLICATION_JSON_VALUE)
				.withBody(copyToString(DomainFixtures.class.getClassLoader()
								.getResourceAsStream("test-responses/get-domain-2-response.json"),
						defaultCharset()));
	}

	private static Domain prepareDomain1() {
		return Domain.builder()
				.weight(0.1)
				.activeConnections(5)
				.domainScore(50)
				.publicUrl(DOMAIN_URL_1)
				.build();
	}

	private static Domain prepareDomain2() {
		return Domain.builder()
				.weight(0.3)
				.activeConnections(10)
				.domainScore(33)
				.publicUrl(DOMAIN_URL_2)
				.build();
	}
}
