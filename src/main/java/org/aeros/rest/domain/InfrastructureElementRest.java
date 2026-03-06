package org.aeros.rest.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InfrastructureElementRest {

	private String id;
	private String domain;
	private Integer cpuCores;
	private Integer currentCpuUsage;
	private Integer ramCapacity;
	private Integer  availableRam;
	private Integer currentRamUsage;
	private Integer avgPowerConsumption;
	private Integer currentPowerConsumption;
	private Boolean realTimeCapable;
	private String  cpuArchitecture;
	private String operatingSystem;
	private String infrastructureElementTier;
	private String infrastructureElementStatus;
	private LocationRest location;
}
