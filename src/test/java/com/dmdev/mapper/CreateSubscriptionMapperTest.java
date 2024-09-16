package com.dmdev.mapper;

import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CreateSubscriptionMapperTest {

    private final CreateSubscriptionMapper mapper = CreateSubscriptionMapper.getInstance();

    @Test
    void testMapValidDto() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("Google Subscription")
                .provider("GOOGLE")  // Изменено с "NETFLIX" на "GOOGLE"
                .expirationDate(Instant.now())
                .build();

        Subscription subscription = mapper.map(dto);

        assertNotNull(subscription);
        assertEquals(dto.getUserId(), subscription.getUserId());
        assertEquals(dto.getName(), subscription.getName());
        assertEquals(Provider.GOOGLE, subscription.getProvider());  // Изменено на Provider.GOOGLE
        assertEquals(dto.getExpirationDate(), subscription.getExpirationDate());
        assertEquals(Status.ACTIVE, subscription.getStatus());
    }


    @ParameterizedTest
    @MethodSource("providerMappingArguments")
    void testProviderMapping(String providerName, Provider expectedProvider) {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("Subscription")
                .provider(providerName)
                .expirationDate(Instant.now())
                .build();

        Subscription subscription = mapper.map(dto);

        assertEquals(expectedProvider, subscription.getProvider());
    }

    static Stream<Arguments> providerMappingArguments() {
        return Stream.of(
                Arguments.of("GOOGLE", Provider.GOOGLE),
                Arguments.of("APPLE", Provider.APPLE),
                Arguments.of("google", Provider.GOOGLE),
                Arguments.of("apple", Provider.APPLE),
                Arguments.of("invalid_provider", null)
        );
    }

    @Test
    void testMapNullDto() {
        assertThrows(NullPointerException.class, () -> mapper.map(null));
    }

    @Test
    void testMapDtoWithNullFields() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder().build();

        Subscription subscription = mapper.map(dto);

        assertNotNull(subscription);
        assertNull(subscription.getUserId());
        assertNull(subscription.getName());
        assertNull(subscription.getProvider());
        assertNull(subscription.getExpirationDate());
        assertEquals(Status.ACTIVE, subscription.getStatus());
    }

    @Test
    void testSingletonInstance() {
        CreateSubscriptionMapper instance1 = CreateSubscriptionMapper.getInstance();
        CreateSubscriptionMapper instance2 = CreateSubscriptionMapper.getInstance();

        assertSame(instance1, instance2, "getInstance should always return the same instance");
    }
}
