package org.aeros.fixtures;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static java.nio.charset.Charset.defaultCharset;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.StreamUtils.copyToString;

import java.io.IOException;
import java.util.Collections;

import org.aeros.rest.domain.ServiceComponentRest;
import org.springframework.http.HttpStatus;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;

public class ServiceFixtures {

	public static final ServiceComponentRest[] SERVICES_IE_1 = new ServiceComponentRest[] {
			prepareService1(), prepareService2()
	};
	public static final ServiceComponentRest[] SERVICES_IE_2 = new ServiceComponentRest[] { prepareService3() };
	public static final String URL = DomainFixtures.DOMAIN_URL_1;
	public static final String SERVICE_ID = "1";
	public static final String TOSCA = """
			tosca_definitions_version: tosca_simple_yaml_1_3
			   
			description: >
			    Test description.
			   
			node_templates:
			  simple_application:
			    type: tosca.nodes.Container.Application
			    requirements:
			      - network:
			          properties:
			            ports:
			              exposedporttest:
			                properties:
			                  protocol: [udp, tcp]
			                  source: 30
			    artifacts:
			      application_image:
			        file: busybox
			        type: tosca.artifacts.Deployment.Image.Container.Docker
			        repository: docker_hub
			   
			    interfaces:
			      Standard:
			        create:
			          implementation: application_image
			          inputs:
			            cliArgs:
			              - RTdeadline: 23
			            envVars:
			              - Var1: 22
			""";

	public static ResponseDefinitionBuilder prepareServicesForIE1Response() throws IOException {
		return aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader("Content-Type", APPLICATION_JSON_VALUE)
				.withBody(copyToString(InfrastructureElementFixtures.class.getClassLoader()
								.getResourceAsStream("test-responses/get-services-for-ie-1-response.json"),
						defaultCharset()));
	}

	public static ResponseDefinitionBuilder prepareServicesForIE2Response() throws IOException {
		return aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader("Content-Type", APPLICATION_JSON_VALUE)
				.withBody(copyToString(InfrastructureElementFixtures.class.getClassLoader()
								.getResourceAsStream("test-responses/get-services-for-ie-2-response.json"),
						defaultCharset()));
	}

	public static ResponseDefinitionBuilder prepareServicesForIE3Response() {
		return aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader("Content-Type", APPLICATION_JSON_VALUE)
				.withBody("[]");
	}

	private static ServiceComponentRest prepareService1() {
		return new ServiceComponentRest(
				"urn:ngsi-ld:Service:01:Component:01",
				"urn:ngsi-ld:InfrastructureElement:01",
				"urn:ngsi-ld:Service:01",
				"urn:ngsi-ld:ServiceComponentStatus:Starting",
				"urn:ngsi-ld:Service:01:Component:01:InfrastructureElementRequirements",
				Collections.emptyList(),
				"fiware/orion-ld:1.5.1",
				Collections.emptyList(),
				Collections.emptyList()
		);
	}

	private static ServiceComponentRest prepareService2() {
		return new ServiceComponentRest(
				"urn:ngsi-ld:Service:02:Component:02",
				"urn:ngsi-ld:InfrastructureElement:01",
				"urn:ngsi-ld:Service:02",
				"urn:ngsi-ld:ServiceComponentStatus:Finished",
				"urn:ngsi-ld:Service:02:Component:02:InfrastructureElementRequirements",
				Collections.emptyList(),
				"fiware/orion-ld:1.5.1",
				Collections.emptyList(),
				Collections.emptyList()
		);
	}

	private static ServiceComponentRest prepareService3() {
		return new ServiceComponentRest(
				"urn:ngsi-ld:Service:03:Component:03",
				"urn:ngsi-ld:InfrastructureElement:02",
				"urn:ngsi-ld:Service:03",
				"urn:ngsi-ld:ServiceComponentStatus:Starting",
				"urn:ngsi-ld:Service:03:Component:03:InfrastructureElementRequirements",
				Collections.emptyList(),
				"fiware/orion-ld:1.5.1",
				Collections.emptyList(),
				Collections.emptyList()
		);
	}
}
