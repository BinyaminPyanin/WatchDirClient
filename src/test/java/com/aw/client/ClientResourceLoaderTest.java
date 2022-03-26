package com.aw.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ClientResourceLoaderTest
 * Test class for
 * A {@link ClientResourceLoader} class.
 *
 * @author Binyamin (Dima) Pyanin
 * @version POC
 * @since March 23, 2022
 */
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClientResourceLoaderTest {

    private static final String TEST_REGEX = "^[0-9]$";
    private static final String TEST_FILE_NAME = "test1.properties";

    private ClientResourceLoader classUnderTest;

    @BeforeEach
    public void setUp() {
        this.classUnderTest = new ClientResourceLoader();

        this.classUnderTest.properties.put("1", "1_abc");
        this.classUnderTest.properties.put("2", "2_abc");
        this.classUnderTest.properties.put("c", "3_abc");

        this.classUnderTest.files.add(new File(TEST_FILE_NAME));
    }

    @Test
    void whenPropertiesMapFilterMethodCalledThenRegexAppliedOk() {

        Map<String, String> filteredMap = this.classUnderTest.filter(TEST_REGEX);

        assertEquals(3, filteredMap.size());

        assertTrue(filteredMap.containsValue(TEST_FILE_NAME));

    }

    @Test
    void whenPropertiesMapFilterMethodCalledOnEmptyPropertiesThenEmptyMapReturned() {

        this.classUnderTest.properties = new HashMap<>();

        Map<String, String> filteredMap = this.classUnderTest.filter(TEST_REGEX);

        assertEquals(Collections.emptyMap(), filteredMap);

    }

    @Test
    void whenPropertiesMapFilterMethodCalledWithEmptyRegexThenEmptyMapReturned() {

        Map<String, String> filteredMap = this.classUnderTest.filter(null);

        assertEquals(Collections.emptyMap(), filteredMap);

    }

    @Test
    void whenRemovePropertyFileMethodCalledOnEmptyFileListThenNoExceptionThrown() {

        this.classUnderTest.files = Collections.EMPTY_LIST;

        assertDoesNotThrow(() -> this.classUnderTest.removePropertyFile());

    }

    @Test
    void whenRemovePropertyFileMethodCalledThenFileDeletedOk() {

        assertDoesNotThrow(() -> this.classUnderTest.removePropertyFile());

    }

}
