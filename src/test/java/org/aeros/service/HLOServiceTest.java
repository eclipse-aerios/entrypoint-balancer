package org.aeros.service;

import static java.net.URI.create;
import static org.aeros.fixtures.ServiceFixtures.SERVICE_ID;
import static org.aeros.fixtures.ServiceFixtures.TOSCA;
import static org.aeros.fixtures.ServiceFixtures.URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.aeros.feign.HLOClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class HLOServiceTest {

	@MockBean
	HLOClient hloClient;

	@Autowired
	HLOService hloService;

	@Test
	@DisplayName("Test passing TOSCA to HLO.")
	void testPassToscaToHLO() {
		when(hloClient.passToscaToHLO(create(URL), SERVICE_ID, TOSCA)).thenReturn("OK.");

		final String resultUrl = hloService.passToscaToHLO(URL, SERVICE_ID, TOSCA);

		assertEquals(URL, resultUrl, "Input URL should be returned by HLOService.");
		verify(hloClient).passToscaToHLO(create(URL), SERVICE_ID, TOSCA);
	}

	@Test
	@DisplayName("Test passing service Id to HLO.")
	void testPassServiceIdToHLO() {
		when(hloClient.passServiceIdToHLO(create(URL), SERVICE_ID)).thenReturn("OK.");

		final String resultUrl = hloService.passServiceIdToHLO(URL, SERVICE_ID);

		assertEquals(URL, resultUrl, "Input URL should be returned by HLOService.");
		verify(hloClient).passServiceIdToHLO(create(URL), SERVICE_ID);
	}

}
