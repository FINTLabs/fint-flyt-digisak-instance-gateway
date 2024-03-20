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
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class SubsidyInstanceMappingService implements InstanceMapper<SubsidyInstance> {

    private final FileClient fileClient;

    public SubsidyInstanceMappingService(FileClient fileClient) {
        this.fileClient = fileClient;
    }

    @Override
    public Mono<InstanceObject> map(Long sourceApplicationId, SubsidyInstance subsidyInstance) {

        return Mono.just(
                InstanceObject.builder()
                        .valuePerKey(Stream
                                .concat(subsidyInstance.getFields().entrySet().stream(),
                                        groupValueMapper(subsidyInstance, sourceApplicationId, subsidyInstance))
                                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                        .objectCollectionPerKey(collectionValueMapper(subsidyInstance))
                        .build());
    }

    private Map<String, Collection<InstanceObject>> collectionValueMapper(SubsidyInstance instance) {

        return instance.getCollections().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        collectionFields -> collectionFields.getValue().stream()
                                .map(this::toInstanceObject).collect(Collectors.toList())));
    }

    private Stream<Map.Entry<String, String>> groupValueMapper(SubsidyInstance instance, Long sourceApplicationId, SubsidyInstance subsidyInstance) {

        return instance.getGroups().entrySet().stream()
                .flatMap(group -> group.getValue().entrySet().stream()
                        .map(field -> getEntry(group, field, sourceApplicationId, subsidyInstance)));
    }

    private Map.Entry<String, String> getEntry(Map.Entry<String, Map<String, String>> group, Map.Entry<String, String> field, Long sourceApplicationId, SubsidyInstance subsidyInstance) {
        if  ("fil".equals(field.getKey())){
            Mono<UUID> uuidMono = fileClient.postFile(File.builder()
                    .name("hoveddokument.txt")
                    .type(MediaType.TEXT_PLAIN)
                    .sourceApplicationId(sourceApplicationId)
                    .sourceApplicationInstanceId(subsidyInstance.getInstanceId())
                    .encoding("UTF-8")
                    .base64Contents(field.getValue())
                    .build());

            return Map.entry(group.getKey().concat(StringUtils.capitalize(field.getKey())),
                    uuidMono.block().toString());
        }
        return Map.entry(group.getKey().concat(StringUtils.capitalize(field.getKey())),
                field.getValue());
    }

    private InstanceObject toInstanceObject(Map<String, String> stringStringMap) {

        return InstanceObject
                .builder()
                .valuePerKey(stringStringMap)
                .build();
    }
}
