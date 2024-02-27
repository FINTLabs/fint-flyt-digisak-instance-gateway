package no.fintlabs.discovery.gateway.model.digisak;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubsidyField {

    @NotBlank
    private String name;

    @NotBlank
    private String displayName;
}
