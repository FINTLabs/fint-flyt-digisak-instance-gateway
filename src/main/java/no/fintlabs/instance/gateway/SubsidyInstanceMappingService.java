package no.fintlabs.instance.gateway;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.gateway.instance.InstanceMapper;
import no.fintlabs.gateway.instance.model.instance.InstanceObject;
import no.fintlabs.instance.gateway.model.digisak.SubsidyInstance;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class SubsidyInstanceMappingService implements InstanceMapper<SubsidyInstance> {

    @Override
    public Mono<InstanceObject> map(Long sourceApplicationId, SubsidyInstance subsidyInstance) {

        return Mono.just(
                InstanceObject.builder()
                        .valuePerKey(Stream
                                .concat(subsidyInstance.getFields().entrySet().stream(),
                                        groupValueMapper(subsidyInstance))
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

    private Stream<Map.Entry<String, String>> groupValueMapper(SubsidyInstance instance) {

        return instance.getGroups().entrySet().stream()
                .flatMap(group -> group.getValue().entrySet().stream()
                        .map(field -> Map.entry(group.getKey().concat(StringUtils.capitalize(field.getKey())),
                                field.getValue()) ));
    }

    private InstanceObject toInstanceObject(Map<String, String> stringStringMap) {

        return InstanceObject
                .builder()
                .valuePerKey(stringStringMap)
                .build();
    }
}
