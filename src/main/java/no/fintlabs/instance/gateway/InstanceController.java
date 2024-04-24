package no.fintlabs.instance.gateway;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.gateway.instance.InstanceProcessor;
import no.fintlabs.gateway.instance.kafka.ArchiveCaseIdRequestService;
import no.fintlabs.instance.gateway.model.Status;
import no.fintlabs.instance.gateway.model.digisak.SubsidyInstance;
import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationAuthorizationUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static no.fintlabs.resourceserver.UrlPaths.EXTERNAL_API;

@RestController
@RequestMapping(EXTERNAL_API + "/digisak/instances")
@Slf4j
public class InstanceController {

    private final InstanceProcessor<SubsidyInstance> subsidyInstanceProcessor;
    private final ArchiveCaseIdRequestService archiveCaseIdRequestService;

    public InstanceController(InstanceProcessor<SubsidyInstance> subsidyInstanceInstanceProcessor,
                              ArchiveCaseIdRequestService archiveCaseIdRequestService) {
        this.subsidyInstanceProcessor = subsidyInstanceInstanceProcessor;
        this.archiveCaseIdRequestService = archiveCaseIdRequestService;
    }

    @PostMapping("instance")
    public Mono<ResponseEntity<?>> postIncomingSubsidy(
            @RequestBody SubsidyInstance incomingInstance,
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {

        return authenticationMono.flatMap(
                authentication -> subsidyInstanceProcessor.processInstance(
                        authentication,
                        incomingInstance
                )
        );
    }

    @GetMapping("status/{instanceId}")
    public Mono<ResponseEntity<?>> getInstanceStatus(
            @AuthenticationPrincipal Mono<Authentication> authenticationMono,
            @PathVariable String instanceId) {

        return authenticationMono.map(authentication -> {
            Long sourceApplicationId = SourceApplicationAuthorizationUtil.getSourceApplicationId(authentication);
            log.debug("Trying to get the latest status for instance {} (sourceApplication {})", instanceId, sourceApplicationId);

            return archiveCaseIdRequestService.getArchiveCaseId(sourceApplicationId, instanceId)
                    .map(caseId -> ResponseEntity.ok(Status.builder()
                            .instanceId(instanceId)
                            .destinationId(caseId)
                            .status("Instans godtatt av destinasjon").build())
                    ).orElse(ResponseEntity.badRequest().body(Status.builder()
                            .instanceId(instanceId)
                                    .status("Ukjent status").build())
                    );
        });
    }

}
