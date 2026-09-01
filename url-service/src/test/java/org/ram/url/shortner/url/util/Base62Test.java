package org.ram.url.shortner.url.util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class Base62Test {

    @Test
    void encodeId_alwaysGeneratesEightCharacters() {
        for (long id = 1; id <= 1000; id++) {
            String code = Base62.encodeId(id);
            assertNotNull(code);
            assertEquals(8, code.length(), "Each code must be exactly 8 characters for id=" + id);
            assertTrue(code.matches("^[0-9A-Za-z]{8}$"), "Code must contain only Base62 characters: " + code);
        }
    }

    @Test
    void encodeId_producesUniqueCodesForSequentialIds() {
        Set<String> generatedCodes = new HashSet<>();
        int count = 5000;

        for (long id = 1; id <= count; id++) {
            String code = Base62.encodeId(id);
            assertTrue(generatedCodes.add(code), "Collision detected for id=" + id + ", code=" + code);
        }
        assertEquals(count, generatedCodes.size());
    }

    @Test
    void obfuscation_preventsPredictableSequentialPatterns() {
        String code1 = Base62.encodeId(1L);
        String code2 = Base62.encodeId(2L);
        String code3 = Base62.encodeId(3L);

        assertNotEquals(code1, code2);
        assertNotEquals(code2, code3);

        // Verify codes are not just simple increments (e.g., not ending with consecutive chars or having identical prefixes)
        assertFalse(code1.substring(0, 6).equals(code2.substring(0, 6)),
                "Codes for 1 and 2 should have distinct prefixes due to bit permutation and scrambling");
    }

    @Test
    void encodeAndDecode_roundTrip() {
        long[] testValues = {0L, 1L, 61L, 62L, 1000L, 999999L, 1234567890123L};
        for (long val : testValues) {
            String encoded = Base62.encode(val);
            long decoded = Base62.decode(encoded);
            assertEquals(val, decoded, "Decoded value must match original for val=" + val);
        }
    }

    @Test
    void encodeWithPadding_padsProperly() {
        String padded = Base62.encodeWithPadding(5L, 8);
        assertEquals(8, padded.length());
        assertTrue(padded.endsWith("5"));
    }
}
