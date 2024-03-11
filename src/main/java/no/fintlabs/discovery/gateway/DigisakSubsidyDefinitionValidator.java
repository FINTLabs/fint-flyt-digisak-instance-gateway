package no.fintlabs.discovery.gateway;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.discovery.gateway.model.digisak.SubsidyDefinition;
import org.springframework.stereotype.Service;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Slf4j
public class DigisakSubsidyDefinitionValidator {

    private final Validator validator;

    public DigisakSubsidyDefinitionValidator(ValidatorFactory validatorFactory) {
        this.validator = validatorFactory.getValidator();
    }

    public Optional<List<String>> validate(SubsidyDefinition subsidyDefinition) {
        List<String> errors = validator.validate(subsidyDefinition)
                .stream()
                .map(constraintViolation ->
                        constraintViolation.getPropertyPath() + " " +  constraintViolation.getMessage())
                .sorted()
                .collect(Collectors.toList());

        return errors.isEmpty()
                ? Optional.empty()
                : Optional.of(errors);
    }
 }
