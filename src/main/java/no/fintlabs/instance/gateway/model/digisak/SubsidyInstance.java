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

    private String instanceId;

    @NotNull
    private Map<String, Object> fields;

    private Map<String, Map<String, Object>> groups;

    private Map<String, List<Map<String, Object>>> collections;
}
