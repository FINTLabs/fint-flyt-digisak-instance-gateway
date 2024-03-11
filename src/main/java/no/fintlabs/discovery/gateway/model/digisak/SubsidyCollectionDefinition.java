package no.fintlabs.discovery.gateway.model.digisak;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubsidyCollectionDefinition {

    @NotBlank
    private String id;

    @NotBlank
    private String displayName;

    @NotEmpty
    private List<@NotNull SubsidyFieldDefinition> fieldDefinitions;
}
