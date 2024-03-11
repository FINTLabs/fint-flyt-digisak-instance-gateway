package no.fintlabs.instance.gateway.model.digisak;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Getter
@Jacksonized
@EqualsAndHashCode
@Builder
public class SubsidyInstance {

    @NotNull
    private String integrationId;

    @NotNull
    private Map<String, String> fields;

    private Map<String, Map<String, String>> groups;

    private Map<String, List<Map<String, String>>> collections;
}
