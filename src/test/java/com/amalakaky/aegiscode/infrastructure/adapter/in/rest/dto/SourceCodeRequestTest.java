package com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SourceCodeRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void sourceCode_shouldHoldAndReturnCorrectValue() {
        String code = "public class Main { public static void main(String[] args) {} }";
        SourceCodeRequest request = new SourceCodeRequest(code);

        assertThat(request.sourceCode()).isEqualTo(code);
    }

    @Test
    void validation_shouldPassForValidSourceCode() {
        SourceCodeRequest request = new SourceCodeRequest("String x = \"hello\";");
        Set<ConstraintViolation<SourceCodeRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void validation_shouldFailWhenSourceCodeIsNull() {
        SourceCodeRequest request = new SourceCodeRequest(null);
        Set<ConstraintViolation<SourceCodeRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("El código fuente no puede estar vacío");
    }

    @Test
    void validation_shouldFailWhenSourceCodeIsBlank() {
        SourceCodeRequest request = new SourceCodeRequest("   ");
        Set<ConstraintViolation<SourceCodeRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("El código fuente no puede estar vacío");
    }

    @Test
    void validation_shouldFailWhenSourceCodeExceedsMaxSize() {
        String oversizedCode = "a".repeat(50001);
        SourceCodeRequest request = new SourceCodeRequest(oversizedCode);
        Set<ConstraintViolation<SourceCodeRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("El tamaño máximo permitido para el análisis estático es de 50KB");
    }

    @Test
    void equalsHashCodeAndToString_shouldBehaveCorrectly() {
        SourceCodeRequest req1 = new SourceCodeRequest("class Test {}");
        SourceCodeRequest req2 = new SourceCodeRequest("class Test {}");
        SourceCodeRequest req3 = new SourceCodeRequest("class Other {}");

        assertThat(req1).isEqualTo(req2);
        assertThat(req1).isNotEqualTo(req3);
        assertThat(req1.hashCode()).isEqualTo(req2.hashCode());
        assertThat(req1.toString()).contains("class Test {}");
    }
}