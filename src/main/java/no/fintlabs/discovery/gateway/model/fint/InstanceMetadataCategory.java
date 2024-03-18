package no.fintlabs.discovery.gateway.model.fint;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class InstanceMetadataCategory {

    private final String displayName;
    private final InstanceMetadataContent content;
}
