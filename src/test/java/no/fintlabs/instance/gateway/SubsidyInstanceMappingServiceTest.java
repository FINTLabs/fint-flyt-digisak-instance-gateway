package no.fintlabs.instance.gateway;

import no.fintlabs.gateway.instance.model.File;
import no.fintlabs.gateway.instance.model.instance.InstanceObject;
import no.fintlabs.gateway.instance.web.FileClient;
import no.fintlabs.instance.gateway.model.digisak.SubsidyInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SubsidyInstanceMappingServiceTest {

    @Mock
    private FileClient fileClient;

    private SubsidyInstanceMappingService service;
    private SubsidyInstance subsidyInstance;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new SubsidyInstanceMappingService(fileClient);

        subsidyInstance = SubsidyInstance.builder()
                .integrationId("FRIP")
                .instanceId("12345")
                .fields(Map.of(
                        "kulturminneId", "99",
                        "saksnummer", "55",
                        "fil1", Map.of(
                                "format", "text/plain",
                                "filnavn", "fil.txt",
                                "data", "BASE64_STRING"
                )))
                .groups(
                        Map.of("hoveddokument",
                                Map.of(
                                        "mediatype", "text/plain",
                                        "filnavn", "hoveddokument.txt",
                                        "fil2", Map.of(
                                                "format", "text/plain",
                                                "filnavn", "fil.txt",
                                                "data", "BASE64_STRING"
                                        )
                                )
                        ))
                .collections(
                        Map.of("vedlegg", List.of (
                                Map.of(
                                        "mediatype", "text/plain",
                                        "filnavn", "vedlegg1.txt",
                                        "fil3", Map.of(
                                                "format", "text/plain",
                                                "filnavn", "fil.txt",
                                                "data", "BASE64_STRING"
                                        )
                                )
                        )))
                .build();

    }

    @Test
    void shouldReturnValidInstanceObject() {

        when(fileClient.postFile(any(File.class))).thenReturn(Mono.just(UUID.randomUUID()));

        InstanceObject result = service.map(0L, subsidyInstance).block();

        assertEquals("99", result.getValuePerKey().get("kulturminneId"));
        assertEquals("55", result.getValuePerKey().get("saksnummer"));
        assertEquals("hoveddokument.txt", result.getValuePerKey().get("hoveddokumentFilnavn"));
        assertEquals("vedlegg1.txt", result.getObjectCollectionPerKey().get("vedlegg").stream().findFirst().get().getValuePerKey().get("filnavn"));
    }

    @Test
    void shouldConvertFileContentToUuidOnField() {
        String hoveddokumentUuid = UUID.randomUUID().toString();

        when(fileClient.postFile(any(File.class))).thenReturn(Mono.just(UUID.fromString(hoveddokumentUuid)));

        InstanceObject instanceObject = service.map(0L, subsidyInstance).block();

        assertEquals(hoveddokumentUuid, instanceObject.getValuePerKey().get("fil1"));
    }

    @Test
    void shouldConvertFileContentToUuidOnGroups() {
        String hoveddokumentUuid = UUID.randomUUID().toString();

        when(fileClient.postFile(any(File.class))).thenReturn(Mono.just(UUID.fromString(hoveddokumentUuid)));

        InstanceObject instanceObject = service.map(0L, subsidyInstance).block();

        assertEquals(hoveddokumentUuid, instanceObject.getValuePerKey().get("hoveddokumentFil2"));
    }

    @Test
    void shouldConvertFileContentToUuidOnCollections() {
        String vedleggUuid = UUID.randomUUID().toString();

        when(fileClient.postFile(any(File.class))).thenReturn(Mono.just(UUID.fromString(vedleggUuid)));

        InstanceObject instanceObject = service.map(0L, subsidyInstance).block();

        assertEquals(vedleggUuid, instanceObject.getObjectCollectionPerKey().get("vedlegg").stream().findFirst().get().getValuePerKey().get("fil3"));
    }

}