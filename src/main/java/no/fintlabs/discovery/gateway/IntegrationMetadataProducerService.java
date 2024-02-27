package no.fintlabs.discovery.gateway;

import no.fintlabs.discovery.gateway.model.fint.IntegrationMetadata;
import no.fintlabs.kafka.event.EventProducer;
import no.fintlabs.kafka.event.EventProducerFactory;
import no.fintlabs.kafka.event.EventProducerRecord;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import org.springframework.stereotype.Service;

@Service
public class IntegrationMetadataProducerService {

    private final EventProducer<IntegrationMetadata> integrationMetadataEventProducer;
    private final EventTopicNameParameters eventTopicNameParameters;

    public IntegrationMetadataProducerService(EventProducerFactory eventProducerFactory) {
        this.integrationMetadataEventProducer = eventProducerFactory.createProducer(IntegrationMetadata.class);
        this.eventTopicNameParameters = EventTopicNameParameters.builder()
                .eventName("integration-metadata-received")
                .build();
    }

    public void publishNewIntergationMetadata(IntegrationMetadata integrationMetadata) {
        integrationMetadataEventProducer.send(
                EventProducerRecord.<IntegrationMetadata>builder()
                        .topicNameParameters(eventTopicNameParameters)
                        .value(integrationMetadata)
                        .build()
        );
    }
}
