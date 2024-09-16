package com.dmdev.validator;

import com.dmdev.dto.CreateSubscriptionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CreateSubscriptionValidatorTest {

    private CreateSubscriptionValidator validator;

    @BeforeEach
    void setUp() {
        validator = CreateSubscriptionValidator.getInstance();
    }

    @Test
    void validate_shouldPassValidation_whenAllFieldsAreValid() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("YouTube Premium")
                .provider("GOOGLE")
                .expirationDate(Instant.now().plusSeconds(3600))
                .build();

        ValidationResult result = validator.validate(dto);

        assertThat(result.hasErrors()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("invalidDtoProvider")
    void validate_shouldFailValidation_whenFieldsAreInvalid(CreateSubscriptionDto dto, int expectedErrorCode) {
        ValidationResult result = validator.validate(dto);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo(expectedErrorCode);
    }

    static Stream<Arguments> invalidDtoProvider() {
        return Stream.of(
                Arguments.of(CreateSubscriptionDto.builder()
                        .name("YouTube Premium")
                        .provider("GOOGLE")
                        .expirationDate(Instant.now().plusSeconds(3600))
                        .build(), 100),
                Arguments.of(CreateSubscriptionDto.builder()
                        .userId(1)
                        .provider("GOOGLE")
                        .expirationDate(Instant.now().plusSeconds(3600))
                        .build(), 101),
                Arguments.of(CreateSubscriptionDto.builder()
                        .userId(1)
                        .name("YouTube Premium")
                        .expirationDate(Instant.now().plusSeconds(3600))
                        .build(), 102),
                Arguments.of(CreateSubscriptionDto.builder()
                        .userId(1)
                        .name("YouTube Premium")
                        .provider("INVALID")
                        .expirationDate(Instant.now().plusSeconds(3600))
                        .build(), 102),
                Arguments.of(CreateSubscriptionDto.builder()
                        .userId(1)
                        .name("YouTube Premium")
                        .provider("GOOGLE")
                        .build(), 103),
                Arguments.of(CreateSubscriptionDto.builder()
                        .userId(1)
                        .name("YouTube Premium")
                        .provider("GOOGLE")
                        .expirationDate(Instant.now().minusSeconds(3600))
                        .build(), 103)
        );
    }

    @Test
    void validate_shouldReturnMultipleErrors_whenMultipleFieldsAreInvalid() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .expirationDate(Instant.now().minusSeconds(3600))
                .build();

        ValidationResult result = validator.validate(dto);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).hasSize(4);
        assertThat(result.getErrors()).extracting(Error::getCode)
                .containsExactlyInAnyOrder(100, 101, 102, 103);
    }
}
