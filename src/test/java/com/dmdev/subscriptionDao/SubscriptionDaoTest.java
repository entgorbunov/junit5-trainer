package com.dmdev.subscriptionDao;

import com.dmdev.dao.SubscriptionDao;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.integration.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SubscriptionDaoTest extends IntegrationTestBase {

    private final SubscriptionDao subscriptionDao = SubscriptionDao.getInstance();

    @Test
    void findAll() {
        Subscription subscription1 = createSubscription(1, "Subscription 1");
        Subscription subscription2 = createSubscription(2, "Subscription 2");

        subscriptionDao.insert(subscription1);
        subscriptionDao.insert(subscription2);

        List<Subscription> actualResult = subscriptionDao.findAll();

        assertNotNull(actualResult);
        assertEquals(2, actualResult.size());
    }

    @Test
    void findById() {
        Subscription expectedSubscription = createSubscription(1, "Test Subscription");
        subscriptionDao.insert(expectedSubscription);

        Optional<Subscription> actualResult = subscriptionDao.findById(expectedSubscription.getId());

        assertTrue(actualResult.isPresent());
        assertEquals(expectedSubscription, actualResult.get());
    }

    @Test
    void update() {
        Subscription subscription = createSubscription(1, "Original Subscription");
        subscriptionDao.insert(subscription);

        subscription.setName("Updated Subscription");
        subscription.setStatus(Status.CANCELED);

        Subscription updatedSubscription = subscriptionDao.update(subscription);

        assertEquals(subscription, updatedSubscription);
        assertEquals("Updated Subscription", updatedSubscription.getName());
        assertEquals(Status.CANCELED, updatedSubscription.getStatus());
    }


    @Test
    void delete() {
        Subscription subscription = createSubscription(1);
        subscriptionDao.insert(subscription);

        boolean result = subscriptionDao.delete(subscription.getId());

        assertTrue(result);
        assertTrue(subscriptionDao.findById(subscription.getId()).isEmpty());
    }



    @Test
    void insert() {
        Subscription subscription = createSubscription(1);

        Subscription insertedSubscription = subscriptionDao.insert(subscription);

        assertNotNull(insertedSubscription.getId());
        assertEquals(subscription, insertedSubscription);
    }

    @Test
    void findByUserId() {
        Subscription subscription1 = createSubscription(1, "Subscription 1");
        Subscription subscription2 = createSubscription(1, "Subscription 2");
        Subscription subscription3 = createSubscription(2, "Subscription 3");

        subscriptionDao.insert(subscription1);
        subscriptionDao.insert(subscription2);
        subscriptionDao.insert(subscription3);

        List<Subscription> actualResult = subscriptionDao.findByUserId(1);

        assertNotNull(actualResult);
        assertEquals(2, actualResult.size());
        assertTrue(actualResult.stream().anyMatch(s -> s.getName().equals("Subscription 1")));
        assertTrue(actualResult.stream().anyMatch(s -> s.getName().equals("Subscription 2")));
    }

    private Subscription createSubscription(Integer userId, String name) {
        return Subscription.builder()
                .userId(userId)
                .name(name)
                .provider(Provider.APPLE)
                .expirationDate(Instant.now().plusSeconds(3600))
                .status(Status.ACTIVE)
                .build();
    }


    private Subscription createSubscription(Integer userId) {
        return Subscription.builder()
                .userId(userId)
                .name("Test Subscription")
                .provider(Provider.APPLE)
                .expirationDate(Instant.now().plusSeconds(3600))
                .status(Status.ACTIVE)
                .build();
    }
}

