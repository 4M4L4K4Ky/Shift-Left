package com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.amalakaky.aegiscode.infrastructure.adapter.in.rest.dto.RegisterRequestDto.ScopesEnum;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RegisterRequestDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void testConstructorsAndGettersSetters() {
        // No-args constructor
        RegisterRequestDto dto = new RegisterRequestDto();
        assertThat(dto.getScopes()).containsExactly(ScopesEnum.READER);

        // Setters
        dto.setUsername("testuser");
        dto.setPassword("password123");
        List<ScopesEnum> customScopes = new ArrayList<>(List.of(ScopesEnum.READER, ScopesEnum.WRITER));
        dto.setScopes(customScopes);

        assertThat(dto.getUsername()).isEqualTo("testuser");
        assertThat(dto.getPassword()).isEqualTo("password123");
        assertThat(dto.getScopes()).isEqualTo(customScopes);

        // All-args constructor
        RegisterRequestDto allArgsDto = new RegisterRequestDto("adminUser", "securePass123", customScopes);
        assertThat(allArgsDto.getUsername()).isEqualTo("adminUser");
        assertThat(allArgsDto.getPassword()).isEqualTo("securePass123");
        assertThat(allArgsDto.getScopes()).isEqualTo(customScopes);
    }

    @Test
    void testFluentMethods() {
        RegisterRequestDto dto = new RegisterRequestDto()
                .username("fluentUser")
                .password("fluentPass123")
                .scopes(List.of(ScopesEnum.WRITER));

        assertThat(dto.getUsername()).isEqualTo("fluentUser");
        assertThat(dto.getPassword()).isEqualTo("fluentPass123");
        assertThat(dto.getScopes()).containsExactly(ScopesEnum.WRITER);
    }

    @Test
    void testAddScopesItem_withExistingListAndWithNullList() {
        RegisterRequestDto dto = new RegisterRequestDto();

        // Lista no nula (por defecto trae READER)
        dto.addScopesItem(ScopesEnum.WRITER);
        assertThat(dto.getScopes()).containsExactly(ScopesEnum.READER, ScopesEnum.WRITER);

        // Rama en la que scopes es null (fuerza la inicialización interna en addScopesItem)
        dto.setScopes(null);
        dto.addScopesItem(ScopesEnum.WRITER);
        assertThat(dto.getScopes()).containsExactly(ScopesEnum.READER, ScopesEnum.WRITER);
    }

    @Test
    void testEnumScopes() {
        // Values & valueOf
        assertThat(ScopesEnum.valueOf("READER")).isEqualTo(ScopesEnum.READER);
        assertThat(ScopesEnum.values()).contains(ScopesEnum.READER, ScopesEnum.WRITER);

        // getValue & toString
        assertThat(ScopesEnum.READER.getValue()).isEqualTo("READER");
        assertThat(ScopesEnum.READER.toString()).isEqualTo("READER");

        // fromValue válido e inválido
        assertThat(ScopesEnum.fromValue("READER")).isEqualTo(ScopesEnum.READER);
        assertThat(ScopesEnum.fromValue("WRITER")).isEqualTo(ScopesEnum.WRITER);

        assertThatThrownBy(() -> ScopesEnum.fromValue("INVALID_SCOPE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unexpected value 'INVALID_SCOPE'");
    }

    @Test
    void testEqualsAndHashCode() {
        RegisterRequestDto dto1 = new RegisterRequestDto("user", "password123", List.of(ScopesEnum.READER));
        RegisterRequestDto dto2 = new RegisterRequestDto("user", "password123", List.of(ScopesEnum.READER));
        RegisterRequestDto diffUser = new RegisterRequestDto("other", "password123", List.of(ScopesEnum.READER));
        RegisterRequestDto diffPass = new RegisterRequestDto("user", "otherpass123", List.of(ScopesEnum.READER));
        RegisterRequestDto diffScopes = new RegisterRequestDto("user", "password123", List.of(ScopesEnum.WRITER));

        // Misma instancia
        assertThat(dto1.equals(dto1)).isTrue();

        // Nulo y clase diferente
        assertThat(dto1.equals(null)).isFalse();
        assertThat(dto1.equals("String Object")).isFalse();

        // Objetos iguales
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());

        // Objetos diferentes por cada campo
        assertThat(dto1).isNotEqualTo(diffUser);
        assertThat(dto1).isNotEqualTo(diffPass);
        assertThat(dto1).isNotEqualTo(diffScopes);
    }

    @Test
    void testToString_withNonNullAndNullValues() {
        RegisterRequestDto dto = new RegisterRequestDto("user", "password123", List.of(ScopesEnum.READER));
        String str = dto.toString();

        assertThat(str)
                .contains("class RegisterRequestDto")
                .contains("username: user")
                .contains("password: password123")
                .contains("scopes: [READER]");

        // Forzar la rama toIndentedString(null)
        RegisterRequestDto emptyDto = new RegisterRequestDto();
        emptyDto.setUsername(null);
        emptyDto.setPassword(null);
        emptyDto.setScopes(null);

        assertThat(emptyDto.toString())
                .contains("username: null")
                .contains("password: null")
                .contains("scopes: null");
    }

    @Test
    void testBeanValidation_Passes() {
        RegisterRequestDto dto = new RegisterRequestDto("validUser", "validPassword123", List.of(ScopesEnum.READER));
        Set<ConstraintViolation<RegisterRequestDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void testBeanValidation_FailsOnUsernameConstraints() {
        // Username nulo
        RegisterRequestDto nullUser = new RegisterRequestDto(null,
                "validPassword123", List.of(ScopesEnum.READER));
        assertThat(validator.validate(nullUser)).isNotEmpty();

        // Username muy corto (< 3)
        RegisterRequestDto shortUser = new RegisterRequestDto("ab",
                "validPassword123", List.of(ScopesEnum.READER));
        assertThat(validator.validate(shortUser)).isNotEmpty();

        // Username muy largo (> 100)
        RegisterRequestDto longUser = new RegisterRequestDto("a".repeat(101),
                "validPassword123", List.of(ScopesEnum.READER));
        assertThat(validator.validate(longUser)).isNotEmpty();
    }

    @Test
    void testBeanValidation_FailsOnPasswordConstraints() {
        // Password nula
        RegisterRequestDto nullPass = new RegisterRequestDto("validUser",
                null, List.of(ScopesEnum.READER));
        assertThat(validator.validate(nullPass)).isNotEmpty();

        // Password muy corta (< 6)
        RegisterRequestDto shortPass = new RegisterRequestDto("validUser",
                "12345", List.of(ScopesEnum.READER));
        assertThat(validator.validate(shortPass)).isNotEmpty();

        // Password muy larga (> 255)
        RegisterRequestDto longPass = new RegisterRequestDto("validUser",
                "a".repeat(256), List.of(ScopesEnum.READER));
        assertThat(validator.validate(longPass)).isNotEmpty();
    }
}