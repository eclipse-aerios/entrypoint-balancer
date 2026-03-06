package org.aeros.rest.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceComponentRest {

	private String id;
	private String infrastructureElement;
	private String service;
	private String serviceComponentStatus;
	private String infrastructureElementRequirements;
	private List<SLARest> sla;
	private String containerImage;
	private List<CliArgsRest> cliArgs;
	private List<String> networkPorts;
}
