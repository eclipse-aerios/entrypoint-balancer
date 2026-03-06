package org.aeros.service;

/**
 * Service responsible for distributing the workload between HLOs.
 * (base algorithm used:
 * <a href="https://link.springer.com/chapter/10.1007/978-3-642-17625-8_13">
 * An Improvement on the Weighted Least-Connection Scheduling Algorithm for Load Balancing in Web Cluster Systems
 * </a>)
 */
public interface LoadBalancerService {

	/**
	 * Method responsible for selecting the address of target HLO to which the request is to be distributed.
	 *
	 * @param serviceId identifier of the service received from the Management Portal
	 * @param toscaYaml tosca received from the Management Portal
	 * @return String with HLO's URL
	 */
	String passToscaToSelectedHLO(final String serviceId, final String toscaYaml);

	/**
	 * Method responsible for selecting the address of target HLO in which service will be orchestrated.
	 *
	 * @param serviceId identifier of the service received from the Management Portal
	 * @return String with HLO's URL
	 */
	String reOrchestrateServiceInToSelectedHLO(final String serviceId);

	/**
	 * Method allows the user to change the number of maximum HLO requests' assignments.
	 */
	void changeAssignmentLimit(final int newLimit);

	/**
	 * Method retrieves current value of maximal assignments.
	 *
	 * @return value of maximal assignments
	 */
	int getMaxAssignments();
}
