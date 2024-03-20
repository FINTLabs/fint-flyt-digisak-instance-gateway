package no.fintlabs.instance.gateway;

import no.fintlabs.gateway.instance.InstanceProcessor;
import no.fintlabs.gateway.instance.InstanceProcessorFactoryService;
import no.fintlabs.instance.gateway.model.digisak.SubsidyInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class InstanceProcessorConfiguration {

    @Bean
    public InstanceProcessor<SubsidyInstance> subsidyInstanceProcessor(
            InstanceProcessorFactoryService instanceProcessorFactoryService,
            SubsidyInstanceMappingService subsidyInstanceMappingService) {

        return instanceProcessorFactoryService.createInstanceProcessor(
                incomingInstance -> Optional.of(incomingInstance.getIntegrationId()),
                incomingInstance -> Optional.of(incomingInstance.getInstanceId()),
                subsidyInstanceMappingService
        );
    }
}
