package no.fintlabs.instance.gateway.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class Status {

    private final String instanceId;
    private final String destinationId;
    private final String status;
}
