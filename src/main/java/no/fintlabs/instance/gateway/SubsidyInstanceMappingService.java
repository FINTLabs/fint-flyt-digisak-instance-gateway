package no.fintlabs.instance.gateway;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.gateway.instance.InstanceMapper;
import no.fintlabs.gateway.instance.model.File;
import no.fintlabs.gateway.instance.model.instance.InstanceObject;
import no.fintlabs.gateway.instance.web.FileClient;
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
                fieldValueMapper(subsidyInstance),
                groupValueMapper(subsidyInstance, sourceApplicationId),
                collectionValueMapper(subsidyInstance, sourceApplicationId)
            ).map(collectionValue ->
                InstanceObject.builder()
                                .valuePerKey(getCombineValuePerKey(collectionValue.getT1(), collectionValue.getT2()))
                                .objectCollectionPerKey(collectionValue.getT3())
                                .build());
    }

    private Map<String, String> getCombineValuePerKey(Map<String, String> fieldValueMap, Map<String, String> groupValueMap) {
        Map<String, String> concatinatedMap = new HashMap<>();
        concatinatedMap.putAll(fieldValueMap);
        concatinatedMap.putAll(groupValueMap);
        return concatinatedMap;
    }

    private Mono<Map<String, String>> fieldValueMapper(SubsidyInstance subsidyInstance) {
        return Flux.fromIterable(subsidyInstance.getFields().entrySet())
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    private Mono<Map<String, Collection<InstanceObject>>> collectionValueMapper(SubsidyInstance instance, Long sourceApplicationId) {
        return Flux.fromIterable(instance.getCollections().entrySet())
                .flatMap(entry -> Flux.fromIterable(entry.getValue())
                        .flatMap(collectionMap -> Flux.fromIterable(collectionMap.entrySet())
                                .flatMap(field -> Mono.from(getEntry(field, sourceApplicationId, instance.getInstanceId()))
                                        .map(uuid -> Map.entry(field.getKey(), uuid))
                                )
                                .collectMap(Map.Entry::getKey, Map.Entry::getValue))
                        .map(this::toInstanceObject)
                        .collectList()
                        .map(list -> Map.entry(entry.getKey(), list)))
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    private Mono<Map<String, String>> groupValueMapper(SubsidyInstance instance, Long sourceApplicationId) {
        return Flux.fromIterable(instance.getGroups().entrySet())
                .flatMap(group -> Flux.fromIterable(group.getValue().entrySet())
                        .flatMap(field -> Mono.from(getEntry(field, sourceApplicationId, instance.getInstanceId()))
                                .map(uuid -> Map.entry(group.getKey().concat(StringUtils.capitalize(field.getKey())), uuid))
                        ))
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    private Mono<String> getEntry(Map.Entry<String, String> field, Long sourceApplicationId, String instanceId) {
        if  ("fil".equals(field.getKey())){
            Mono<UUID> uuidMono = fileClient.postFile(File.builder()
                    .sourceApplicationId(sourceApplicationId)
                    .sourceApplicationInstanceId(instanceId)
                    .name("hoveddokument.txt")
                    .type(MediaType.TEXT_PLAIN)
                    .encoding("UTF-8")
                    .base64Contents(field.getValue())
                    .build());

            return uuidMono.map(UUID::toString);
        }
        return Mono.just(field.getValue());
    }

    private InstanceObject toInstanceObject(Map<String, String> stringStringMap) {
        return InstanceObject.builder()
                .valuePerKey(stringStringMap)
                .build();
    }
}
