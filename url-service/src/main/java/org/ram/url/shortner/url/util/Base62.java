package org.ram.url.shortner.url.util;

public final class Base62 {

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = 62;
    public static final int SHORT_CODE_LENGTH = 8;

    // 62^8 = 218,340,105,584,896 — maximum capacity for 8-char Base62
    private static final long MAX_CAPACITY = 218_340_105_584_896L;

    // Large coprime multiplier (coprime to 2 and 31) for multiplicative scrambling
    private static final long MULTIPLIER = 143_489_071_127_917L;
    private static final long OFFSET = 87_112_233_445_566L;
    private static final long XOR_MASK = 0x5A3C96E5A3C9L; // 48-bit mask

    // Fixed 48-bit permutation table to shuffle bit positions
    private static final int[] BIT_PERM = {
        17, 34,  3, 41, 12, 28,  7, 45,
        22,  1, 38, 15, 30,  9, 47, 24,
         5, 42, 19, 36, 11, 26,  0, 44,
        20,  2, 39, 14, 31,  8, 46, 23,
         4, 43, 18, 35, 10, 27, 13, 29,
         6, 37, 16, 33, 21, 40, 25, 32
    };

    private Base62() {}

    /**
     * Obfuscates a sequential sequence ID using bit permutation, XOR masking,
     * and modular multiplicative hashing, then encodes it into an 8-character Base62 string.
     *
     * @param id the unique sequence ID generated from MongoDB counter
     * @return an 8-character obfuscated Base62 string
     */
    public static String encodeId(long id) {
        long obfuscated = obfuscate(id);
        return encodeWithPadding(obfuscated, SHORT_CODE_LENGTH);
    }

    /**
     * Applies a bit-permutation and integer obfuscation function to eliminate
     * predictable sequential key patterns.
     *
     * @param id raw sequence number
     * @return obfuscated 48-bit long value within [0, MAX_CAPACITY)
     */
    public static long obfuscate(long id) {
        // Step 1: Bit-permutation on 48 bits
        long permuted = permuteBits(id & 0xFFFFFFFFFFFFL);

        // Step 2: XOR with mask
        long masked = permuted ^ XOR_MASK;

        // Step 3: Multiplicative mixing with coprime modulo MAX_CAPACITY
        // Using positive modulo arithmetic
        long scrambled = Math.floorMod((masked * MULTIPLIER) + OFFSET, MAX_CAPACITY);

        return scrambled;
    }

    /**
     * Bit permutation function that redistributes bit positions across 48 bits.
     */
    private static long permuteBits(long val) {
        long result = 0L;
        for (int i = 0; i < 48; i++) {
            long bit = (val >> i) & 1L;
            result |= (bit << BIT_PERM[i]);
        }
        return result;
    }

    /**
     * Encodes a long value to Base62 and left-pads with '0' to the specified width.
     */
    public static String encodeWithPadding(long n, int width) {
        if (n < 0) {
            throw new IllegalArgumentException("Number must be non-negative: " + n);
        }
        if (n == 0) {
            return "0".repeat(width);
        }
        StringBuilder sb = new StringBuilder();
        while (n > 0) {
            sb.append(ALPHABET.charAt((int) (n % BASE)));
            n /= BASE;
        }
        String encoded = sb.reverse().toString();
        if (encoded.length() < width) {
            return "0".repeat(width - encoded.length()) + encoded;
        }
        return encoded;
    }

    /**
     * Standard Base62 encode without padding.
     */
    public static String encode(long n) {
        if (n == 0) return "0";
        StringBuilder sb = new StringBuilder();
        while (n > 0) {
            sb.append(ALPHABET.charAt((int) (n % BASE)));
            n /= BASE;
        }
        return sb.reverse().toString();
    }

    /**
     * Standard Base62 decode.
     */
    public static long decode(String str) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("String cannot be null or empty");
        }
        long result = 0;
        for (int i = 0; i < str.length(); i++) {
            int index = ALPHABET.indexOf(str.charAt(i));
            if (index == -1) {
                throw new IllegalArgumentException("Invalid Base62 character: " + str.charAt(i));
            }
            result = result * BASE + index;
        }
        return result;
    }
}
