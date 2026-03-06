package org.aeros.fixtures;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static java.nio.charset.Charset.defaultCharset;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.StreamUtils.copyToString;

import java.io.IOException;

import org.aeros.rest.domain.InfrastructureElementRest;
import org.aeros.rest.domain.LocationRest;
import org.springframework.http.HttpStatus;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;

public class InfrastructureElementFixtures {

	public static final InfrastructureElementRest[] TWO_INFRASTRUCTURE_ELEMENTS = new InfrastructureElementRest[] {
			prepareInfrastructureElement(), prepareInfrastructureElement2(), prepareInfrastructureElement3()
	};

	public static ResponseDefinitionBuilder prepareIERestResponse() throws IOException {
		return aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader("Content-Type", APPLICATION_JSON_VALUE)
				.withBody(copyToString(InfrastructureElementFixtures.class.getClassLoader()
								.getResourceAsStream("test-responses/get-infrastructure-elements-response.json"),
						defaultCharset()));
	}

	public static ResponseDefinitionBuilder prepareEmptyIERestResponse() {
		return aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader("Content-Type", APPLICATION_JSON_VALUE)
				.withBody("[]");
	}

	private static InfrastructureElementRest prepareInfrastructureElement() {
		return new InfrastructureElementRest(
				"urn:ngsi-ld:InfrastructureElement:01",
				"urn:ngsi-ld:Domain:01",
				10,
				20,
				8192,
				5100,
				3092,
				100,
				95,
				true,
				"urn:ngsi-ld:CpuArchitecture:x64",
				"urn:ngsi-ld:OperatingSystem:Linux",
				"urn:ngsi-ld:InfrastructureElementTier:Edge",
				"urn:ngsi-ld:InfrastructureElementStatus:Ready",
				new LocationRest("Point", new Float[] { 0.3f, 0.4f })
		);
	}

	private static InfrastructureElementRest prepareInfrastructureElement2() {
		return new InfrastructureElementRest(
				"urn:ngsi-ld:InfrastructureElement:02",
				"urn:ngsi-ld:Domain:02",
				20,
				70,
				9000,
				3000,
				6000,
				100,
				90,
				false,
				"urn:ngsi-ld:CpuArchitecture:x64",
				"urn:ngsi-ld:OperatingSystem:Linux",
				"urn:ngsi-ld:InfrastructureElementTier:Edge",
				"urn:ngsi-ld:InfrastructureElementStatus:Ready",
				new LocationRest("Point", new Float[] { 0.3f, 0.4f })
		);
	}

	private static InfrastructureElementRest prepareInfrastructureElement3() {
		return new InfrastructureElementRest(
				"urn:ngsi-ld:InfrastructureElement:03",
				"urn:ngsi-ld:Domain:02",
				30,
				50,
				10000,
				5000,
				5000,
				50,
				40,
				true,
				"urn:ngsi-ld:CpuArchitecture:x64",
				"urn:ngsi-ld:OperatingSystem:Linux",
				"urn:ngsi-ld:InfrastructureElementTier:Edge",
				"urn:ngsi-ld:InfrastructureElementStatus:Ready",
				new LocationRest("Point", new Float[] { 0.3f, 0.4f })
		);
	}
}
