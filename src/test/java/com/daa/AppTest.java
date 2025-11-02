package com.daa;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic test for the App class.
 */
class AppTest {
    
    @Test
    void testAppExists() {
        // Simple test to verify the App class can be instantiated
        assertDoesNotThrow(() -> {
            App.main(new String[]{});
        });
    }
}
