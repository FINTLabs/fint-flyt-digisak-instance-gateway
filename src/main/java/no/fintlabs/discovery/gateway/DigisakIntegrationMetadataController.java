package no.fintlabs.discovery.gateway;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.discovery.gateway.model.digisak.SubsidyDefinition;
import no.fintlabs.discovery.gateway.model.fint.IntegrationMetadata;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static no.fintlabs.resourceserver.UrlPaths.EXTERNAL_API;

@Slf4j
@RestController
@RequestMapping(EXTERNAL_API + "/digisak/metadata")
public class DigisakIntegrationMetadataController {

    private final IntegrationMetadataProducerService integrationMetadataProducerService;
    private final SourceApplicationAuthorizationUtil sourceApplicationAuthorizationUtil;

    public DigisakIntegrationMetadataController(IntegrationMetadataProducerService integrationMetadataProducerService,
                                                SourceApplicationAuthorizationUtil sourceApplicationAuthorizationUtil) {
        this.integrationMetadataProducerService = integrationMetadataProducerService;
        this.sourceApplicationAuthorizationUtil = sourceApplicationAuthorizationUtil;
    }

    @PostMapping()
    public Mono<ResponseEntity<?>> postIntegrationMetadata(
            @RequestBody SubsidyDefinition subsidyDefinition,
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
            ) {

        return authenticationMono.map(authentication -> processIntegrationMetadata(subsidyDefinition, authentication));
    }

    protected ResponseEntity<?> processIntegrationMetadata(SubsidyDefinition subsidyDefinition, Authentication authentication) {
        log.debug("☠☠☠ Arkivlandslaget was here ☠☠☠");

        IntegrationMetadata integrationMetadata = IntegrationMetadata.builder()
                .sourceApplicationId(sourceApplicationAuthorizationUtil.getSourceApplicationId(authentication))
                .sourceApplicationIntegrationId(subsidyDefinition.getMetadata().getSubsidyId())
                .integrationDisplayName(subsidyDefinition.getMetadata().getSubsidyDisplayName())
                .version(subsidyDefinition.getMetadata().getVersion())
                .build();

        integrationMetadataProducerService.publishNewIntergationMetadata(integrationMetadata);
        return ResponseEntity.accepted().build();
    }
}
