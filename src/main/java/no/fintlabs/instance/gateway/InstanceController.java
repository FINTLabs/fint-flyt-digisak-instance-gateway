package no.fintlabs.instance.gateway;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.gateway.instance.InstanceProcessor;
import no.fintlabs.instance.gateway.model.digisak.SubsidyInstance;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static no.fintlabs.resourceserver.UrlPaths.EXTERNAL_API;

@RestController
@RequestMapping(EXTERNAL_API + "/digisak/instances")
@Slf4j
public class InstanceController {

    private final InstanceProcessor<SubsidyInstance> subsidyInstanceProcessor;

    public InstanceController(InstanceProcessor<SubsidyInstance> subsidyInstanceInstanceProcessor) {
        this.subsidyInstanceProcessor = subsidyInstanceInstanceProcessor;
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

}
