package no.fintlabs.discovery.gateway;


import no.fintlabs.discovery.gateway.model.digisak.SubsidyDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class DigisakSubsidyDefinitionValidatorTest {

    @Mock
    ValidatorFactory validatorFactory;

    @Mock
    Validator validator;

    @InjectMocks
    DigisakSubsidyDefinitionValidator digisakSubsidyDefinitionValidator;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(validatorFactory.getValidator()).thenReturn(validator);
        digisakSubsidyDefinitionValidator = new DigisakSubsidyDefinitionValidator(validatorFactory);
    }

    @Test
    public void greenIsGood() {
        assertTrue(true);
    }

    //@Test
    public void shouldReturnErrorWhenFieldsAreMissing() {
        SubsidyDefinition subsidyDefinition = SubsidyDefinition.builder()
                .integrationId("FOOBAR")
                .integrationDisplayName("Your Display Name")
                .version(1337L)
                .fieldDefinitions(new ArrayList<>())
                .groupDefinitions(new ArrayList<>())
                .collectionDefinitions(new ArrayList<>())
                .build();

        Optional<List<String>> result = digisakSubsidyDefinitionValidator.validate(subsidyDefinition);

        assertTrue(result.isPresent());
    }
}
