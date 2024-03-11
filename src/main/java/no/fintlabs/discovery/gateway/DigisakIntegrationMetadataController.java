package no.fintlabs.discovery.gateway;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.discovery.gateway.model.digisak.SubsidyDefinition;
import no.fintlabs.discovery.gateway.model.fint.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static no.fintlabs.resourceserver.UrlPaths.EXTERNAL_API;

@Slf4j
@RestController
@RequestMapping(EXTERNAL_API + "/digisak/metadata")
public class DigisakIntegrationMetadataController {

    private final IntegrationMetadataProducerService integrationMetadataProducerService;
    private final SourceApplicationAuthorizationUtil sourceApplicationAuthorizationUtil;
    private final DigisakSubsidyDefinitionValidator digisakSubsidyDefinitionValidator;

    public DigisakIntegrationMetadataController(IntegrationMetadataProducerService integrationMetadataProducerService,
                                                SourceApplicationAuthorizationUtil sourceApplicationAuthorizationUtil, DigisakSubsidyDefinitionValidator digisakSubsidyDefinitionValidator) {
        this.integrationMetadataProducerService = integrationMetadataProducerService;
        this.sourceApplicationAuthorizationUtil = sourceApplicationAuthorizationUtil;
        this.digisakSubsidyDefinitionValidator = digisakSubsidyDefinitionValidator;
    }

    @PostMapping()
    public Mono<ResponseEntity<?>> postIntegrationMetadata(
            @RequestBody SubsidyDefinition subsidyDefinition,
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
            ) {

        return authenticationMono.map(authentication -> processIntegrationMetadata(subsidyDefinition, authentication));
    }

    protected ResponseEntity<?> processIntegrationMetadata(SubsidyDefinition subsidyDefinition, Authentication authentication) {

        digisakSubsidyDefinitionValidator.validate(subsidyDefinition).ifPresent(
                (List<String> validationErrors) -> {
                    throw new ResponseStatusException(
                            HttpStatus.UNPROCESSABLE_ENTITY, "Validation error(s): "
                            + validationErrors.stream().map(error -> "'" + error + "'").toList()
                    );
                }
        );

        IntegrationMetadata integrationMetadata = IntegrationMetadata.builder()
                .sourceApplicationId(sourceApplicationAuthorizationUtil.getSourceApplicationId(authentication))
                .sourceApplicationIntegrationId(subsidyDefinition.getIntegrationId())
                .integrationDisplayName(subsidyDefinition.getIntegrationDisplayName())
                .version(subsidyDefinition.getVersion())
                .instanceMetadata(InstanceMetadataContent.builder()
                        .instanceValueMetadata(
                                subsidyDefinition.getFieldDefinitions().stream()
                                        .map(subsidyField -> InstanceValueMetadata.builder()
                                            .key(subsidyField.getId())
                                            .displayName(subsidyField.getDisplayName())
                                            .type(InstanceValueMetadata.Type.STRING).build())
                                        .collect(Collectors.toList()))
                        .instanceMetadataCategories(
                                subsidyDefinition.getGroupDefinitions().stream()
                                        .map(subsidyGroupDefinition -> InstanceMetadataCategory.builder()
                                                .displayName(subsidyGroupDefinition.getDisplayName())
                                                .instanceMetadataContent(InstanceMetadataContent.builder()
                                                        .instanceValueMetadata(
                                                                subsidyGroupDefinition.getFieldDefinitions().stream()
                                                                        .map(subsidyField -> InstanceValueMetadata.builder()
                                                                                .key(subsidyGroupDefinition.getId().concat(StringUtils.capitalize(subsidyField.getId())))
                                                                                .displayName(subsidyField.getDisplayName())
                                                                                .type(InstanceValueMetadata.Type.STRING).build())
                                                                        .collect(Collectors.toList()))
                                                        .build())
                                                .build())
                                        .collect(Collectors.toList()))
                        .instanceObjectCollectionMetadata(
                                subsidyDefinition.getCollectionDefinitions().stream()
                                        .map(subsidyCollectionDefinition -> InstanceObjectCollectionMetadata.builder()
                                                .key(subsidyCollectionDefinition.getId())
                                                .displayName(subsidyCollectionDefinition.getDisplayName())
                                                .instanceMetadataContent(InstanceMetadataContent.builder()
                                                        .instanceValueMetadata(
                                                                subsidyCollectionDefinition.getFieldDefinitions().stream()
                                                                        .map(subsidyField -> InstanceValueMetadata.builder()
                                                                                .key(subsidyCollectionDefinition.getId().concat(StringUtils.capitalize(subsidyField.getId())))
                                                                                .displayName(subsidyField.getDisplayName())
                                                                                .type(InstanceValueMetadata.Type.STRING).build())
                                                                        .collect(Collectors.toList()))
                                                        .build())
                                                .build())
                                        .collect(Collectors.toList()))
                        .build())
                .build();

        integrationMetadataProducerService.publishNewIntegrationMetadata(integrationMetadata);
        return ResponseEntity.accepted().build();
    }
}
