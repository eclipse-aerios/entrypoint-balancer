package org.aeros.service;

/**
 * Service responsible for communication with HLO FE.
 */
public interface HLOService {

	/**
	 * Method distributes TOSCA specification to HLO located at the given URL.
	 *
	 * @param serviceId identifier of the service received from the Management Portal
	 * @param baseURL base URL of selected domain
	 *
	 * @return URL of HLO's domain to which TOSCA was sent
	 */
	String passToscaToHLO(final String baseURL, final String serviceId, final String tosca);

	/**
	 * Method distributes service ID to HLO located at the given URL.
	 *
	 * @param serviceId identifier of the service received from the Management Portal
	 * @param baseURL base URL of selected domain
	 *
	 * @return URL of HLO's domain to which service ID was sent
	 */
	String passServiceIdToHLO(final String baseURL, final String serviceId);

}
