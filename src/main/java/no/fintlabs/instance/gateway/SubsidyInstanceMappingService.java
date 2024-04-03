package no.fintlabs.instance.gateway;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.gateway.instance.InstanceMapper;
import no.fintlabs.gateway.instance.model.File;
import no.fintlabs.gateway.instance.model.instance.InstanceObject;
import no.fintlabs.gateway.instance.web.FileClient;
import no.fintlabs.instance.gateway.model.digisak.SubsidyDokumentfil;
import no.fintlabs.instance.gateway.model.digisak.SubsidyInstance;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class SubsidyInstanceMappingService implements InstanceMapper<SubsidyInstance> {

    private final FileClient fileClient;
    private String instanceId;
    private Long sourceApplicationId;

    public SubsidyInstanceMappingService(FileClient fileClient) {
        this.fileClient = fileClient;
    }

    @Override
    public Mono<InstanceObject> map(Long sourceApplicationId, SubsidyInstance subsidyInstance) {
        this.instanceId = subsidyInstance.getInstanceId();
        this.sourceApplicationId = sourceApplicationId;

        return Mono.zip(fieldValueMapper(subsidyInstance),
                        groupValueMapper(subsidyInstance),
                        collectionValueMapper(subsidyInstance))
                .map(collectionValue ->
                        InstanceObject.builder()
                                .valuePerKey(new HashMap<>() {{
                                    putAll(collectionValue.getT1());
                                    putAll(collectionValue.getT2()); }})
                                .objectCollectionPerKey(collectionValue.getT3())
                                .build());
    }

    private Mono<Map<String, String>> fieldValueMapper(SubsidyInstance subsidyInstance) {
        return Flux.fromIterable(subsidyInstance.getFields().entrySet())
                .flatMap(this::parseObject)
                .flatMapIterable(Map::entrySet)
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    private Mono<Map<String, String>> groupValueMapper(SubsidyInstance subsidyInstance) {
        return Flux.fromIterable(subsidyInstance.getGroups().entrySet())
                .flatMap(group -> Flux.fromIterable(group.getValue().entrySet())
                        .map(field -> Map.entry(concatGroupNameWithFieldName(group.getKey(), field.getKey()), field.getValue()))
                        .flatMap(this::parseObject))
                .flatMapIterable(Map::entrySet)
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    private static String concatGroupNameWithFieldName(String group, String field) {
        return group.concat(StringUtils.capitalize(field));
    }

    private Mono<Map<String, Collection<InstanceObject>>> collectionValueMapper(SubsidyInstance instance) {
        return Flux.fromIterable(instance.getCollections().entrySet())
                .flatMap(entry -> Flux.fromIterable(entry.getValue())
                        .flatMap(collectionMap -> Flux.fromIterable(collectionMap.entrySet())
                                .flatMap(this::parseObject)
                                .flatMapIterable(Map::entrySet)
                                .collectMap(Map.Entry::getKey, Map.Entry::getValue))
                        .map(this::toInstanceObject)
                        .collectList()
                        .map(list -> Map.entry(entry.getKey(), list)))
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    private Mono<Map<String, String>> parseObject(Map.Entry<String, Object> field) {
        Object object = field.getValue();
        if (object instanceof Map<?,?>) {
            SubsidyDokumentfil dokumentfil = toSubsidyDokumentfil(object);
            return postFile(dokumentfil, sourceApplicationId, instanceId)
                    .map(uuid -> Map.of(
                            field.getKey().concat("Data"), uuid,
                            field.getKey().concat("Format"), dokumentfil.getFormat().toString(),
                            field.getKey().concat("Filnavn"), dokumentfil.getFilnavn()
                    ));
        } else if (object instanceof String) {
            return Mono.just(Map.of(field.getKey(), (String) object));
        } else {
            String message = String.format("Field ({}) value ({}) is not a valid type.", field.getKey(), field.getValue());
            return Mono.error(new IllegalArgumentException(message));
        }
    }

    private static SubsidyDokumentfil toSubsidyDokumentfil(Object object) {
        Map<String, String> subsidyDocumentfilMap = (Map<String, String>) object;
        return SubsidyDokumentfil.builder()
                .filnavn(subsidyDocumentfilMap.get("filnavn"))
                .format(MediaType.valueOf(subsidyDocumentfilMap.get("format")))
                .data(subsidyDocumentfilMap.get("data"))
                .build();
    }

    private Mono<String> postFile(SubsidyDokumentfil field, Long sourceApplicationId, String instanceId) {
        Mono<UUID> uuidMono = fileClient.postFile(File.builder()
                    .sourceApplicationId(sourceApplicationId)
                    .sourceApplicationInstanceId(instanceId)
                    .name(field.getFilnavn())
                    .type(field.getFormat())
                    .encoding("UTF-8")
                    .base64Contents(field.getData())
                    .build());

        return uuidMono.map(UUID::toString);
    }

    private InstanceObject toInstanceObject(Map<String, String> stringStringMap) {
        return InstanceObject.builder()
                .valuePerKey(stringStringMap)
                .build();
    }
}
