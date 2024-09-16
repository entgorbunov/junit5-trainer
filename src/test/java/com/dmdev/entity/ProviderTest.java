package com.dmdev.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProviderTest {

    @Test
    void testFindByNameValid() {
        assertEquals(Provider.GOOGLE, Provider.findByName("GOOGLE"));
        assertEquals(Provider.APPLE, Provider.findByName("APPLE"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"google", "GOOGLE", "Google"})
    void testFindByNameCaseInsensitive(String input) {
        assertEquals(Provider.GOOGLE, Provider.findByName(input));
    }

    @Test
    void testFindByNameInvalid() {
        assertThrows(NoSuchElementException.class, () -> Provider.findByName("INVALID"));
    }

    @Test
    void testFindByNameNull() {
        assertThrows(NoSuchElementException.class, () -> Provider.findByName(null));
    }


    @Test
    void testFindByNameOptValid() {
        Optional<Provider> googleProvider = Provider.findByNameOpt("GOOGLE");
        assertTrue(googleProvider.isPresent());
        assertEquals(Provider.GOOGLE, googleProvider.get());

        Optional<Provider> appleProvider = Provider.findByNameOpt("APPLE");
        assertTrue(appleProvider.isPresent());
        assertEquals(Provider.APPLE, appleProvider.get());
    }

    @ParameterizedTest
    @ValueSource(strings = {"apple", "APPLE", "Apple"})
    void testFindByNameOptCaseInsensitive(String input) {
        Optional<Provider> provider = Provider.findByNameOpt(input);
        assertTrue(provider.isPresent());
        assertEquals(Provider.APPLE, provider.get());
    }

    @Test
    void testFindByNameOptInvalid() {
        Optional<Provider> invalidProvider = Provider.findByNameOpt("INVALID");
        assertTrue(invalidProvider.isEmpty());
    }

    @Test
    void testFindByNameOptNull() {
        Optional<Provider> nullProvider = Provider.findByNameOpt(null);
        assertTrue(nullProvider.isEmpty());
    }

    @Test
    void testEnumValues() {
        Provider[] providers = Provider.values();
        assertEquals(2, providers.length);
        assertTrue(Arrays.asList(providers).contains(Provider.GOOGLE));
        assertTrue(Arrays.asList(providers).contains(Provider.APPLE));
    }
}
