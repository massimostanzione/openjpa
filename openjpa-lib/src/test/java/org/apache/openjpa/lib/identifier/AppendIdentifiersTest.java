package org.apache.openjpa.lib.identifier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.apache.openjpa.lib.identifier.utils.ISW2TestUtils.*;
import static org.apache.openjpa.lib.identifier.utils.ISW2TestUtils.IdentifierRuleType.*;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class AppendIdentifiersTest {
    // Test parameters.
    private static IdentifierRule rule;
    private static String name1;
    private static String name2;
    private String expectedBehavior;

    // Class under test.
    private IdentifierUtilImpl idUtils;

    /**
     * Inner class containing the test parameters.
     */
    private static class AppendIdentifiersTestParams {
        private IdentifierRule rule;
        private String name1;
        private String name2;
        private String expectedBehavior;

        public AppendIdentifiersTestParams(IdentifierRule rule, String name1, String name2, String expectedBehavior) {
            this.rule = rule;
            this.name1 = name1;
            this.name2 = name2;
            this.expectedBehavior = expectedBehavior;
        }
    }

    public AppendIdentifiersTest(AppendIdentifiersTestParams params) {
        configure(params);
    }

    /**
     * Link class parameters with test parameters.
     *
     * @param params test parameters
     */
    private void configure(AppendIdentifiersTestParams params) {
        this.rule = params.rule;
        this.name1 = params.name1;
        this.name2 = params.name2;
        this.expectedBehavior = params.expectedBehavior;
    }

    /**
     * Before each test, instantiate the class under test
     * and assign the configuration to the instance.
     * Exceptions not specifically handled because not under test.
     */
    @Before
    public void setup() {
        this.idUtils = new IdentifierUtilImpl();
        IdentifierConfiguration conf = new IdConfigurationTestImpl();
        this.idUtils.setIdentifierConfiguration(conf);
    }

    /**
     * Parameters association. The parameter involved has been declared earlier as
     * attributes in this class.
     *
     * @return array containing actual values of the test parameters
     */
    @Parameterized.Parameters
    public static Collection<AppendIdentifiersTestParams[]> getTestParameters() {
        IdentifierRule rule_allowComp = new IdentifierRule();
        rule_allowComp.setName(ALLOW_COMPACTION.toString());
        rule_allowComp.setAllowCompaction(true);
        rule_allowComp.getAllowCompaction();

        IdentifierRule rule_allowTrunc = new IdentifierRule();
        rule_allowTrunc.setName(ALLOW_TRUNCATION.toString());
        rule_allowTrunc.setAllowTruncation(true);

        IdentifierRule rule_canDelimit = new IdentifierRule();
        rule_canDelimit.setName(CAN_DELIMIT.toString());
        rule_canDelimit.setCanDelimit(true);

        IdentifierRule rule_delimitResWords = new IdentifierRule();
        rule_delimitResWords.setName(DELIMIT_RESERVED_WORDS.toString());
        rule_delimitResWords.setDelimitReservedWords(true);
        rule_delimitResWords.setReservedWords(getResWords());

        IdentifierRule rule_maxLen = new IdentifierRule();
        rule_maxLen.setName(MAX_LENGTH.toString());
        rule_maxLen.setMaxLength(MAXLEN);

        IdentifierRule rule_mustBeginWithLetter = new IdentifierRule();
        rule_mustBeginWithLetter.setName(MUST_BEGIN_WITH_LETTER.toString());
        rule_mustBeginWithLetter.setMustBeginWithLetter(true);

        IdentifierRule rule_mustDelimit = new IdentifierRule();
        rule_mustDelimit.setName(MUST_DELIMIT.toString());
        rule_mustDelimit.setMustDelimit(true);

        IdentifierRule rule_nullable = new IdentifierRule();
        rule_nullable.setName(NULLABLE.toString());
        rule_nullable.setNullable(true);

        IdentifierRule rule_not_nullable = new IdentifierRule();
        rule_not_nullable.setName(NOT_NULLABLE.toString());
        rule_not_nullable.setNullable(false);

        IdentifierRule rule_onlyLDUS = new IdentifierRule();
        rule_onlyLDUS.setName(ONLY_LDUS.toString());
        rule_onlyLDUS.setOnlyLettersDigitsUnderscores(true);

        List<AppendIdentifiersTestParams[]> args = Arrays.asList(new AppendIdentifiersTestParams[][]{
                {new AppendIdentifiersTestParams(rule_canDelimit, "first", "second", "firstsecond")},
                {new AppendIdentifiersTestParams(rule_canDelimit, "`first`", "`second`", "firstsecond")},
                {new AppendIdentifiersTestParams(rule_canDelimit, "`first", "second`", "`firstsecond`")},
                {new AppendIdentifiersTestParams(rule_canDelimit, "first", "secon`d", "`firstsecon`d`")},
                {new AppendIdentifiersTestParams(rule_canDelimit, "", "secon`d", "`secon`d`")},
                {new AppendIdentifiersTestParams(rule_canDelimit, "reserv", "ed1", "reserved1")},
                {new AppendIdentifiersTestParams(rule_mustDelimit, "first", "second", "`firstsecond`")},
                {new AppendIdentifiersTestParams(rule_mustDelimit, "`first`", "`second`", "`firstsecond`")},
                {new AppendIdentifiersTestParams(rule_mustDelimit, "`first", "second`", "`firstsecond`")},
                {new AppendIdentifiersTestParams(rule_mustDelimit, "first", "secon`d", "`firstsecon`d`")},
                {new AppendIdentifiersTestParams(rule_mustDelimit, "", "secon`d", "`secon`d`")},
                {new AppendIdentifiersTestParams(rule_mustDelimit, "reserv", "ed1", "`reserved1`")},
                {new AppendIdentifiersTestParams(rule_nullable, "first", null, "first")},
                {new AppendIdentifiersTestParams(rule_nullable, null, null, "")},
                {new AppendIdentifiersTestParams(rule_not_nullable, "first", null, "first")},
                {new AppendIdentifiersTestParams(rule_not_nullable, null, null, "")},
                {new AppendIdentifiersTestParams(null, "first", "second", FAIL)},

                // Removed, because not relevant
                //{new AppendIdentifiersTestParams(rule_allowTrunc, "first", "second", "fir")},
                //{new AppendIdentifiersTestParams(rule_delimitResWords, "first", "second", "firstsecond")},
                //{new AppendIdentifiersTestParams(rule_delimitResWords, "firreserved 3st_reserved2", "se reserved1 cond", "fir`reserved 3`st`_reserved2`")},
                //{new AppendIdentifiersTestParams(rule_maxLen, "fir", "", "fir"")},
                //{new AppendIdentifiersTestParams(rule_maxLen, "", "sec", "sec")},
                //{new AppendIdentifiersTestParams(rule_maxLen, "longer than", "the limit", "lon")},
                //{new AppendIdentifiersTestParams(rule_mustBeginWithLetter, "already starts", " with letter", "already starts with letter")},
                //{new AppendIdentifiersTestParams(rule_mustBeginWithLetter, "44does not start ", "with letter", "does not start with letter")},
                //{new AppendIdentifiersTestParams(rule_onlyLDUS, "44firs-t1", "second2", PASS)},
        });
        return args;
    }

    @Test
    public void appendIdentifiersCheckTest() {
        String ret = null;
        try {
            ret = this.idUtils.appendNames(rule, name1, name2);
        } catch (Exception e) {
            assertTrue(buildExceptionString(e), expectedBehavior == FAIL);
            return;
        }
        assertNotNull("Null value returned.", ret);
        assertEquals(expectedBehavior, ret);
        assertTrue(EXPECTED_TO_FAIL, expectedBehavior != FAIL);
    }
}
