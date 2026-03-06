package org.aeros.rest.domain;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DomainRest {

	private String id;
	private String description;
	private String publicUrl;
	private ArrayList<String> owner;
	private Boolean isEntrypoint;
	private String domainStatus;
}
