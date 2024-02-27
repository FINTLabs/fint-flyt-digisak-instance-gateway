package no.fintlabs.discovery.gateway.model.fint;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class IntegrationMetadata {

    private final Long sourceApplicationId;
    private final String sourceApplicationIntegrationId;
    private final String sourceApplicationIntegrationUri;
    private final String integrationDisplayName;
    private final Long version;
    private final InstanceMetadataContent instanceMetadata;
}
