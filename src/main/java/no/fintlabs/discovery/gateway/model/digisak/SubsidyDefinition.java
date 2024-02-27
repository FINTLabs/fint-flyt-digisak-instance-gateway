package no.fintlabs.discovery.gateway.model.digisak;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubsidyDefinition {

    @NotNull
    private SubsidyMetadata metadata;

}
