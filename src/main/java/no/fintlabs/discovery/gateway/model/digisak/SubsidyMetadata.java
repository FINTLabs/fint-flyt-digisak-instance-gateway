package no.fintlabs.discovery.gateway.model.digisak;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubsidyMetadata {

    @NotBlank
    private String subsidyId;

    @NotBlank
    private String subsidyDisplayName;

    @NotNull
    private Long version;
}
