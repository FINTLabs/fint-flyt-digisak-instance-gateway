package no.fintlabs.discovery.gateway.model.fint;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class InstanceObjectCollectionMetadata {

    private final String displayName;
    private final InstanceMetadataContent objectMetadata;
    private final String key;
}
