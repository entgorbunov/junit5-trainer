package com.dmdev.service;
import com.dmdev.dao.SubscriptionDao;
import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.exception.SubscriptionException;
import com.dmdev.exception.ValidationException;
import com.dmdev.integration.IntegrationTestBase;
import com.dmdev.mapper.CreateSubscriptionMapper;
import com.dmdev.validator.CreateSubscriptionValidator;
import com.dmdev.validator.Error;
import com.dmdev.validator.ValidationResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
class SubscriptionServiceTest {

    private static final int VALID_USER_ID = 1;
    private static final int INVALID_USER_ID = -1;
    private static final String SUBSCRIPTION_NAME = "Google Drive";
    private static final String PROVIDER_NAME = "GOOGLE";

    @Mock
    private SubscriptionDao subscriptionDao;

    @Mock
    private CreateSubscriptionMapper createSubscriptionMapper;

    @Mock
    private CreateSubscriptionValidator createSubscriptionValidator;

    @Mock
    private Clock clock;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void testUpsert_NewSubscription_ShouldCreateNewSubscription() {
        CreateSubscriptionDto dto = createValidDto();
        ValidationResult validationResult = new ValidationResult();
        when(createSubscriptionValidator.validate(dto)).thenReturn(validationResult);

        when(subscriptionDao.findByUserId(dto.getUserId())).thenReturn(Collections.emptyList());

        Subscription newSubscription = new Subscription();
        when(createSubscriptionMapper.map(dto)).thenReturn(newSubscription);
        when(subscriptionDao.upsert(any(Subscription.class))).thenReturn(newSubscription);

        Subscription result = subscriptionService.upsert(dto);

        assertNotNull(result);
        verify(subscriptionDao).findByUserId(dto.getUserId());
        verify(subscriptionDao).upsert(newSubscription);
        verify(createSubscriptionMapper).map(dto);
    }


    @Test
    void testUpsert_ExistingSubscription_ShouldUpdateExistingSubscription() {
        CreateSubscriptionDto dto = createValidDto();
        ValidationResult validationResult = new ValidationResult();
        when(createSubscriptionValidator.validate(dto)).thenReturn(validationResult);

        Subscription existingSubscription = new Subscription()
                .setName(SUBSCRIPTION_NAME)
                .setProvider(Provider.GOOGLE)
                .setUserId(VALID_USER_ID)
                .setStatus(Status.ACTIVE);
        when(subscriptionDao.findByUserId(VALID_USER_ID)).thenReturn(List.of(existingSubscription));
        when(subscriptionDao.upsert(any(Subscription.class))).thenReturn(existingSubscription);

        Subscription result = subscriptionService.upsert(dto);

        assertNotNull(result);
        assertEquals(Status.ACTIVE, result.getStatus());
        assertEquals(SUBSCRIPTION_NAME, result.getName());
        assertEquals(Provider.GOOGLE, result.getProvider());
        assertEquals(VALID_USER_ID, result.getUserId());
        verify(subscriptionDao).upsert(existingSubscription);
    }

    @Test
    void testUpsert_ValidationFails_ShouldThrowValidationException() {
        CreateSubscriptionDto dto = createValidDto();
        ValidationResult validationResult = new ValidationResult();
        Error error = Error.of(1001, "Invalid data");
        validationResult.add(error);
        when(createSubscriptionValidator.validate(dto)).thenReturn(validationResult);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> subscriptionService.upsert(dto));

        assertNotNull(exception.getErrors(), "Список ошибок не должен быть null");
        assertFalse(exception.getErrors().isEmpty(), "Список ошибок не должен быть пустым");
        assertEquals(1, exception.getErrors().size(), "Должна быть одна ошибка");

        Error actualError = exception.getErrors().get(0);
        assertEquals(1001, actualError.getCode(), "Код ошибки должен совпадать");
        assertEquals("Invalid data", actualError.getMessage(), "Сообщение об ошибке должно совпадать");
    }


    @Test
    void testUpsert_DaoThrowsException_ShouldPropagateException() {
        CreateSubscriptionDto dto = createValidDto();
        ValidationResult validationResult = new ValidationResult();
        when(createSubscriptionValidator.validate(dto)).thenReturn(validationResult);
        when(createSubscriptionMapper.map(dto)).thenReturn(new Subscription());
        when(subscriptionDao.upsert(any(Subscription.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> subscriptionService.upsert(dto));
    }

    @Test
    void testCancel_ActiveSubscription_ShouldCancelSubscription() {
        Subscription subscription = new Subscription().setId(VALID_USER_ID).setStatus(Status.ACTIVE);
        when(subscriptionDao.findById(VALID_USER_ID)).thenReturn(Optional.of(subscription));

        subscriptionService.cancel(VALID_USER_ID);

        assertEquals(Status.CANCELED, subscription.getStatus());
        verify(subscriptionDao).update(subscription);
    }

    @ParameterizedTest
    @EnumSource(value = Status.class, names = {"EXPIRED", "CANCELED"})
    void testCancel_NonActiveSubscription_ShouldThrowException(Status status) {
        Subscription subscription = new Subscription().setId(VALID_USER_ID).setStatus(status);
        when(subscriptionDao.findById(VALID_USER_ID)).thenReturn(Optional.of(subscription));

        SubscriptionException exception = assertThrows(SubscriptionException.class,
                () -> subscriptionService.cancel(VALID_USER_ID));

        String expectedMessage = String.format("Only active subscription %d can be canceled", VALID_USER_ID);
        assertEquals(expectedMessage, exception.getMessage());

        verify(subscriptionDao).findById(VALID_USER_ID);
        verify(subscriptionDao, never()).update(any(Subscription.class));
    }

    @Test
    void testCancel_SubscriptionNotFound_ShouldThrowException() {
        when(subscriptionDao.findById(VALID_USER_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> subscriptionService.cancel(VALID_USER_ID));

        verify(subscriptionDao).findById(VALID_USER_ID);
        verify(subscriptionDao, never()).update(any(Subscription.class));
    }





    @Test
    void testExpire_ActiveSubscription_ShouldExpireSubscription() {
        Subscription subscription = new Subscription().setId(VALID_USER_ID).setStatus(Status.ACTIVE);
        when(subscriptionDao.findById(VALID_USER_ID)).thenReturn(Optional.of(subscription));
        when(clock.instant()).thenReturn(Instant.now());

        subscriptionService.expire(VALID_USER_ID);

        assertEquals(Status.EXPIRED, subscription.getStatus());
        verify(subscriptionDao).update(subscription);
    }

    @Test
    void testExpire_AlreadyExpiredSubscription_ShouldThrowException() {
        int subscriptionId = VALID_USER_ID;
        Subscription subscription = new Subscription()
                .setId(subscriptionId)
                .setStatus(Status.EXPIRED);
        when(subscriptionDao.findById(subscriptionId)).thenReturn(Optional.of(subscription));

        SubscriptionException exception = assertThrows(SubscriptionException.class,
                () -> subscriptionService.expire(subscriptionId));

        String expectedMessage = String.format("Subscription %d has already expired", subscriptionId);
        assertEquals(expectedMessage, exception.getMessage());

        verify(subscriptionDao).findById(subscriptionId);
        verify(subscriptionDao, never()).update(any(Subscription.class));
        assertEquals(Status.EXPIRED, subscription.getStatus(), "Статус подписки не должен измениться");
    }





    @Test
    void testExpire_SubscriptionNotFound_ShouldThrowException() {
        when(subscriptionDao.findById(VALID_USER_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> subscriptionService.expire(VALID_USER_ID));

        assertNull(exception.getMessage());

        verify(subscriptionDao).findById(VALID_USER_ID);
        verify(subscriptionDao, never()).update(any(Subscription.class));
    }

    @Test
    void testUpsert_NullDto_ShouldThrowException() {
        assertThrows(NullPointerException.class, () -> subscriptionService.upsert(null));
    }

    @Test
    void testCancel_InvalidId_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> subscriptionService.cancel(INVALID_USER_ID));
    }



    private CreateSubscriptionDto createValidDto() {
        return CreateSubscriptionDto.builder()
                .userId(VALID_USER_ID)
                .name(SUBSCRIPTION_NAME)
                .provider(PROVIDER_NAME)
                .expirationDate(Instant.now())
                .build();
    }
}