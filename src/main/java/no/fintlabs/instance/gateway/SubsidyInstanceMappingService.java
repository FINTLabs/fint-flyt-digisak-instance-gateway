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

    public SubsidyInstanceMappingService(FileClient fileClient) {
        this.fileClient = fileClient;
    }

    @Override
    public Mono<InstanceObject> map(Long sourceApplicationId, SubsidyInstance subsidyInstance) {

        return Mono.zip(
                    fieldValueMapper(subsidyInstance.getFields(), sourceApplicationId, subsidyInstance.getInstanceId()),
                    groupValueMapper(subsidyInstance, sourceApplicationId),
                    collectionValueMapper(subsidyInstance, sourceApplicationId))
                .map(zip -> toInstanceObject(zip.getT1(), zip.getT2(), zip.getT3()));
    }

    private InstanceObject toInstanceObject(Map<String, String> fields,
                                                   Map<String, String> groups,
                                                   Map<String, Collection<InstanceObject>> collections) {
        return InstanceObject.builder()
                .valuePerKey(new HashMap<>() {{
                    putAll(fields);
                    putAll(groups);}})
                .objectCollectionPerKey(collections)
                .build();
    }

    private InstanceObject toInstanceObject(Map<String, String> fieldsInCollection) {
        return InstanceObject.builder()
                .valuePerKey(fieldsInCollection)
                .build();
    }

    private Mono<Map<String, String>> fieldValueMapper(Map<String, Object> fields, Long sourceApplicationId, String instanceId) {
        return Flux.fromIterable(fields.entrySet())
                .flatMap(field -> parseField(field, sourceApplicationId, instanceId))
                .flatMapIterable(Map::entrySet)
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    private Mono<Map<String, String>> groupValueMapper(SubsidyInstance subsidyInstance, Long sourceApplicationId) {
        return Flux.fromIterable(subsidyInstance.getGroups().entrySet())
                .flatMap(group -> fieldValueMapperInGroup(subsidyInstance, sourceApplicationId, group))
                .flatMapIterable(Map::entrySet)
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    private Flux<Map<String, String>> fieldValueMapperInGroup(SubsidyInstance subsidyInstance, Long sourceApplicationId, Map.Entry<String, Map<String, Object>> group) {
        return Flux.fromIterable(group.getValue().entrySet())
                .map(field -> Map.entry(
                        concatGroupNameWithFieldName(group.getKey(), field.getKey()),
                        field.getValue()))
                .flatMap(field -> parseField(field, sourceApplicationId, subsidyInstance.getInstanceId()));
    }

    private String concatGroupNameWithFieldName(String group, String field) {
        return group.concat(StringUtils.capitalize(field));
    }

    private Mono<Map<String, Collection<InstanceObject>>> collectionValueMapper(SubsidyInstance instance, Long sourceApplicationId) {
        return Flux.fromIterable(instance.getCollections().entrySet())
                .flatMap(entry -> Flux.fromIterable(entry.getValue())
                        .flatMap(collectionMap -> fieldValueMapper(collectionMap, sourceApplicationId, instance.getInstanceId()))
                        .map(this::toInstanceObject)
                        .collectList()
                        .map(list -> Map.entry(entry.getKey(), list)))
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    private Mono<Map<String, String>> parseField(Map.Entry<String, Object> field, Long sourceApplicationId, String instanceId) {
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
            String message = String.format("Field (%s) with value (%s) is not a valid type.", field.getKey(), field.getValue());
            return Mono.error(new IllegalArgumentException(message));
        }
    }

    private SubsidyDokumentfil toSubsidyDokumentfil(Object object) {
        Map<String, String> subsidyDocumentfilMap = (Map<String, String>) object;
        return SubsidyDokumentfil.builder()
                .filnavn(subsidyDocumentfilMap.get("filnavn"))
                .format(MediaType.valueOf(subsidyDocumentfilMap.get("format")))
                .data(subsidyDocumentfilMap.get("data"))
                .build();
    }

    private Mono<String> postFile(SubsidyDokumentfil field, Long sourceApplicationId, String instanceId) {
        File fileContent = File.builder()
                .sourceApplicationId(sourceApplicationId)
                .sourceApplicationInstanceId(instanceId)
                .name(field.getFilnavn())
                .type(field.getFormat())
                .encoding("UTF-8")
                .base64Contents(field.getData())
                .build();
        return fileClient.postFile(fileContent)
                .map(UUID::toString);
    }
}
