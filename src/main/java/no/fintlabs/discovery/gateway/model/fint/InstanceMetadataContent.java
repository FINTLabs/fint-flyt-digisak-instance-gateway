package no.fintlabs.discovery.gateway.model.fint;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@Builder
@EqualsAndHashCode
public class InstanceMetadataContent {

    @Builder.Default
    private final Collection<InstanceValueMetadata> instanceValueMetadata = new ArrayList<>();

    @Builder.Default
    private final Collection<InstanceObjectCollectionMetadata> instanceObjectCollectionMetadata = new ArrayList<>();

    @Builder.Default
    private final Collection<InstanceMetadataCategory> instanceMetadataCategories = new ArrayList<>();
}
