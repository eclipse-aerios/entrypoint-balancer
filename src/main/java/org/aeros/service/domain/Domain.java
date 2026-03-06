package org.aeros.service.domain;

import java.util.Objects;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class Domain {

	private String publicUrl;
	private long activeConnections;
	private double weight;
	private double domainScore;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Domain domain))
			return false;
		return Objects.equals(publicUrl, domain.publicUrl);
	}

	@Override
	public int hashCode() {
		return Objects.hash(publicUrl);
	}
}
