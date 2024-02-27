package no.fintlabs.discovery.gateway.model.digisak;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubsidyDefinition {

    @NotNull
    private SubsidyMetadata metadata;

    @NotEmpty
    private List<@NotNull SubsidyField> fields;
}
