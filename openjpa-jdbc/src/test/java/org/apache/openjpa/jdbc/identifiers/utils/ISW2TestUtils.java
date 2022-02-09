package org.apache.openjpa.jdbc.identifiers.utils;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for global string values, passing expectations statements,
 * and other specific tools, used (statically) in the "2" Apache BookKeeper project.
 */
public final class ISW2TestUtils {
    // Sample string values.
    public static final String VALID_STRING = "This is, actually, a valid string.";
    public static final String EMPTY_STRING = "";
    public static final String PASSWORD = "password";

    // Sample valid byte buffer, and relative parameters.
    public static byte[] validData = VALID_STRING.getBytes();
    public static final int validOffset = 0;
    public static int validLength = validData.length;

    // Passing expectations. Only high-level "pass/fail" logic, based on a code-agnostic approach.
    public static final String PASS = "Success.";
    public static final String FAIL = "Fail.";
    public static final String EXPECTED_TO_FAIL = "Test meant to fail, but did not.";

    /**
     * Custom exception string.
     *
     * @see ISW2TestUtils#buildExceptionString
     */
    private static final String NOT_EXPECTED_EXCEPTION = "Exception raised, but not expected here: ";

    // OpenJPA-specific default parameters used in the tests.
    public static final String EMPTY = "";
    public static final String NULL_LITERALLY = "null";
    public static final int MAXLEN = 3;

    /**
     * Custom set of reserved words.
     *
     * @see ISW2TestUtils#getResWords()
     */
    private static Set<String> resWords = null;

    /**
     * Default constructor, to prevent instatiation
     */
    private ISW2TestUtils() {
    }

    /**
     * Build a specific error string, to be made distinct w.r.t. the standard strings not included in the tests.
     *
     * @param e the exception raised
     * @return the built string
     */
    public static String buildExceptionString(Exception e) {
        return NOT_EXPECTED_EXCEPTION + e.toString();
    }

    /**
     * Initialize and return a set of fictitious reserved words.
     *
     * @return a set of reserved words
     */
    public static Set<String> getResWords() {
        resWords = new HashSet<>();
        resWords.add("reserved1");
        resWords.add("_reserved2");
        resWords.add("reserved 3");
        resWords.add("RESERVED1");
        return resWords;
    }

    public enum IdentifierRuleType {
        ALLOW_COMPACTION,
        ALLOW_TRUNCATION,
        CAN_DELIMIT,
        DELIMIT_RESERVED_WORDS,
        MAX_LENGTH,
        MUST_BEGIN_WITH_LETTER,
        MUST_DELIMIT,
        NULLABLE,
        NOT_NULLABLE,
        ONLY_LDUS
    }
}