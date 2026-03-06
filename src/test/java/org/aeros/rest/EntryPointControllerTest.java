package org.aeros.rest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate;
import static org.aeros.fixtures.DomainFixtures.prepareDomainRest1Response;
import static org.aeros.fixtures.DomainFixtures.prepareDomainRest2Response;
import static org.aeros.fixtures.InfrastructureElementFixtures.prepareEmptyIERestResponse;
import static org.aeros.fixtures.InfrastructureElementFixtures.prepareIERestResponse;
import static org.aeros.fixtures.ServiceFixtures.SERVICE_ID;
import static org.aeros.fixtures.ServiceFixtures.TOSCA;
import static org.aeros.fixtures.ServiceFixtures.prepareServicesForIE1Response;
import static org.aeros.fixtures.ServiceFixtures.prepareServicesForIE2Response;
import static org.aeros.fixtures.ServiceFixtures.prepareServicesForIE3Response;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;

import org.aeros.config.WireMockConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.github.tomakehurst.wiremock.client.WireMock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(WireMockConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EntryPointControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private WireMockConfig wireMockConfig;

	@Test
	@DisplayName("Test POST /distribute/{serviceId} when domain is not found.")
	void testPOSTDistributeServiceWhenNoIEs() throws Exception {
		wireMockConfig.mockOrion().stubFor(WireMock.get(urlPathEqualTo("/entities"))
				.withQueryParam("format", equalTo("simplified"))
				.withQueryParam("type", equalTo("InfrastructureElement"))
				.willReturn(prepareEmptyIERestResponse()));

		final ResultActions result = mockMvc.perform(post("/entrypoint-balancer/distribute/{serviceId}", SERVICE_ID)
				.contentType("application/yaml")
				.content(TOSCA));

		result.andExpect(status().isNotFound())
				.andExpect(content().string("Couldn't find the HLO to allocate the request."));
	}

	@Test
	@DisplayName("Test POST /distribute/{serviceId} when media type is incorrect.")
	void testPOSTDistributeServiceIncorrectMediaType() throws Exception {
		final ResultActions result = mockMvc.perform(post("/entrypoint-balancer/distribute/{serviceId}", SERVICE_ID)
				.contentType(APPLICATION_JSON)
				.content(TOSCA));

		result.andExpect(status().isUnsupportedMediaType())
				.andExpect(content().string("Unsupported Media Type. "
											+ "Expected type: application/yaml. Received type: application/json;charset=UTF-8."));
	}

	@Test
	@DisplayName("Test POST /distribute/{serviceId} when domain is selected.")
	void testPOSTDistributeServiceWhenDomainIsSelected() throws Exception {
		prepareWireMockResponses();
		wireMockConfig.mockHLO().stubFor(WireMock.post(urlPathMatching("/hlo_fe/services/1"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value()).withBody("service allocation initiated")));

		final ResultActions result = mockMvc.perform(post("/entrypoint-balancer/distribute/{serviceId}", SERVICE_ID)
				.contentType("application/yaml")
				.content(TOSCA));

		result.andExpect(status().isOk())
				.andExpect(content().string(
						"Request was distributed successfully to HLO located in the domain http://localhost:8181."));
	}

	@Test
	@DisplayName("Test POST /distribute/{serviceId} when HLO throws an error.")
	void testPOSTDistributeServiceWithHLOError() throws Exception {
		prepareWireMockResponses();
		wireMockConfig.mockHLO().stubFor(WireMock.post(urlPathMatching("/hlo_fe/services/1"))
				.willReturn(
						aResponse().withStatus(HttpStatus.BAD_REQUEST.value()).withBody("invalid service parameters")));

		final ResultActions result = mockMvc.perform(post("/entrypoint-balancer/distribute/{serviceId}", SERVICE_ID)
				.contentType("application/yaml")
				.content(TOSCA));

		result.andExpect(status().isBadRequest())
				.andExpect(content().string(
						"Could not initialize service in HLO. Root cause: "
						+ "[400 Bad Request] during [POST] to [http://localhost:8181/hlo_fe/services/1] [HLOClient#passToscaToHLO(URI,String,String)]: "
						+ "[invalid service parameters]."));
	}

	@Test
	@DisplayName("Test PUT /distribute/{serviceId} when domain is not found.")
	void testPUTDistributeServiceWhenNoIEs() throws Exception {
		wireMockConfig.mockOrion().stubFor(WireMock.get(urlPathEqualTo("/entities"))
				.withQueryParam("format", equalTo("simplified"))
				.withQueryParam("type", equalTo("InfrastructureElement"))
				.willReturn(prepareEmptyIERestResponse()));

		final ResultActions result = mockMvc.perform(put("/entrypoint-balancer/distribute/{serviceId}", SERVICE_ID));

		result.andExpect(status().isNotFound())
				.andExpect(content().string("Couldn't find the HLO to allocate the request."));
	}

	@Test
	@DisplayName("Test PUT /distribute/{serviceId} when domain is selected.")
	void testPUTDistributeServiceWhenDomainIsSelected() throws Exception {
		prepareWireMockResponses();
		wireMockConfig.mockHLO().stubFor(WireMock.put(urlPathMatching("/hlo_fe/services/1"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value()).withBody("service allocation initiated")));

		final ResultActions result = mockMvc.perform(put("/entrypoint-balancer/distribute/{serviceId}", SERVICE_ID));

		result.andExpect(status().isOk())
				.andExpect(content().string(
						"Service Id was distributed successfully to HLO located in the domain http://localhost:8181."));
	}

	@Test
	@DisplayName("Test PUT /distribute/{serviceId} when HLO throws an error.")
	void testPUTDistributeServiceWithHLOError() throws Exception {
		prepareWireMockResponses();
		wireMockConfig.mockHLO().stubFor(WireMock.put(urlPathMatching("/hlo_fe/services/1"))
				.willReturn(
						aResponse().withStatus(HttpStatus.BAD_REQUEST.value()).withBody("invalid service parameters")));

		final ResultActions result = mockMvc.perform(put("/entrypoint-balancer/distribute/{serviceId}", SERVICE_ID));

		result.andExpect(status().isBadRequest())
				.andExpect(content().string(
						"Could not initialize service in HLO. Root cause: "
						+ "[400 Bad Request] during [PUT] to [http://localhost:8181/hlo_fe/services/1] [HLOClient#passServiceIdToHLO(URI,String)]: "
						+ "[invalid service parameters]."));
	}

	@Test
	@DisplayName("Test POST /configure when correct.")
	void testPOSTConfigureWhenCorrect() throws Exception {
		final String payload = """
				{ "maxAssignments": 20, "weightingFunctionType": "RAM_AND_CPU" }""";

		final ResultActions result = mockMvc.perform(post("/entrypoint-balancer/configure")
				.contentType(APPLICATION_JSON)
				.content(payload));

		result.andExpect(status().isOk())
				.andExpect(content().string(
						"New configuration has been applied (ConfigurationRest(maxAssignments=20, weightingFunctionType=RAM_AND_CPU))."));
	}

	private void prepareWireMockResponses() throws IOException {
		wireMockConfig.mockOrion().stubFor(WireMock.get(urlPathEqualTo("/entities"))
				.withQueryParam("format", equalTo("simplified"))
				.withQueryParam("type", equalTo("InfrastructureElement"))
				.willReturn(prepareIERestResponse()));
		wireMockConfig.mockOrion().stubFor(WireMock.get(urlPathEqualTo("/entities"))
				.withQueryParam("format", equalTo("simplified"))
				.withQueryParam("type", equalTo("ServiceComponent"))
				.withQueryParam("q", equalTo("infrastructureElement==\"urn:ngsi-ld:InfrastructureElement:01\""))
				.willReturn(prepareServicesForIE1Response()));
		wireMockConfig.mockOrion().stubFor(WireMock.get(urlPathEqualTo("/entities"))
				.withQueryParam("format", equalTo("simplified"))
				.withQueryParam("type", equalTo("ServiceComponent"))
				.withQueryParam("q", equalTo("infrastructureElement==\"urn:ngsi-ld:InfrastructureElement:02\""))
				.willReturn(prepareServicesForIE2Response()));
		wireMockConfig.mockOrion().stubFor(WireMock.get(urlPathEqualTo("/entities"))
				.withQueryParam("format", equalTo("simplified"))
				.withQueryParam("type", equalTo("ServiceComponent"))
				.withQueryParam("q", equalTo("infrastructureElement==\"urn:ngsi-ld:InfrastructureElement:03\""))
				.willReturn(prepareServicesForIE3Response()));
		wireMockConfig.mockOrion().stubFor(WireMock.get(urlPathTemplate("/entities/{id}"))
				.withQueryParam("format", equalTo("simplified"))
				.withQueryParam("type", equalTo("Domain"))
				.withPathParam("id", equalTo("urn%3Angsi-ld%3ADomain%3A01"))
				.willReturn(prepareDomainRest1Response()));
		wireMockConfig.mockOrion().stubFor(WireMock.get(urlPathTemplate("/entities/{id}"))
				.withQueryParam("format", equalTo("simplified"))
				.withQueryParam("type", equalTo("Domain"))
				.withPathParam("id", equalTo("urn%3Angsi-ld%3ADomain%3A02"))
				.willReturn(prepareDomainRest2Response()));
	}

	@Test
	@DisplayName("Test POST /configure when wrong weighting function type passed.")
	void testPOSTConfigureWhenIncorrectWeightingFunctionGiven() throws Exception {
		final String payload = """
				{ "maxAssignments": 20, "weightingFunctionType": "WRONG" }""";

		final ResultActions result = mockMvc.perform(post("/entrypoint-balancer/configure")
				.contentType(APPLICATION_JSON)
				.content(payload));

		result.andExpect(status().isNotAcceptable())
				.andExpect(content().string("Type WRONG of weighting function is not supported."));
	}

	@Test
	@DisplayName("Test GET /configure.")
	void testGETConfigure() throws Exception {
		final ResultActions result = mockMvc.perform(get("/entrypoint-balancer/configure"));

		result.andExpect(status().isOk())
				.andExpect(content().json("{\"maxAssignments\": 2, \"weightingFunctionType\": \"CPU\"}"));
	}
}
