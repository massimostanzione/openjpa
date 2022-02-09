package org.apache.openjpa.lib.identifier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.apache.openjpa.lib.identifier.utils.ISW2TestUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class RemoveDelimitersTest {
    // Test parameters.
    private static IdentifierConfiguration currentConf;
    private static IdentifierConfiguration newConf;
    private static String rule;
    private static String fullName;
    private static String expectedBehavior;

    // Class under test.
    private IdentifierUtilImpl idUtils;

    private static final String MOCKEDIDC_LEADING = "#START#";
    private static final String MOCKEDIDC_CONCAT = "#CONCAT#";
    private static final String MOCKEDIDC_SEPARATOR = "#SEPARATOR#";
    private static final String MOCKEDIDC_TRAILING = "#END#";

    /**
     * Inner class containing the test parameters.
     */
    private static class RemoveDelimitersTestParams {
        private IdentifierConfiguration currentConf;
        private IdentifierConfiguration newConf;
        private String rule;
        private String fullName;
        private String expectedBehavior;

        public RemoveDelimitersTestParams(IdentifierConfiguration currentConf, IdentifierConfiguration newConf,
                                          String rule, String fullName, String expectedBehavior) {
            this.currentConf = currentConf;
            this.newConf = newConf;
            this.rule = rule;
            this.fullName = fullName;
            this.expectedBehavior = expectedBehavior;
        }
    }

    public RemoveDelimitersTest(RemoveDelimitersTestParams params) {
        configure(params);
    }

    /**
     * Link class parameters with test parameters.
     *
     * @param params test parameters
     */
    private void configure(RemoveDelimitersTestParams params) {
        this.currentConf = params.currentConf;
        this.newConf = params.newConf;
        this.rule = params.rule;
        this.fullName = params.fullName;
        this.expectedBehavior = params.expectedBehavior;
    }

    /**
     * Before each test, instantiate the class under test.
     */
    @Before
    public void setup() {
        this.idUtils = new IdentifierUtilImpl();
    }

    /**
     * In order to have a realization of the IdentifierConfiguration interface that can expose
     * custom-defined identifier rules, a mock of it is needed.
     * Further details about this choice can be faound in the project's final report.
     *
     * @return a ready-to-use IdentifierConfiguration mock
     * @see IdentifierConfiguration
     */
    public static IdentifierConfiguration generateIdCMock() {
        IdentifierConfiguration mockedIdCImpl = Mockito.mock(IdentifierConfiguration.class);

        // Identifier rules to be assigned to the mock
        IdentifierRule ir = new IdentifierRule();
        ir.setMaxLength(MAXLEN);
        ir.setName("test");
        ir.setCanDelimit(false);
        ir.setMustDelimit(false);

        IdentifierRule ir2 = new IdentifierRule();
        ir2.setMaxLength(MAXLEN);
        ir2.setName("test2");
        ir2.setCanDelimit(true);
        ir2.setMustDelimit(true);

        Map<Object, IdentifierRule> map = new HashMap<>();
        map.put("test", ir);

        MockitoAnnotations.initMocks(mockedIdCImpl);
        // Specify reaction from the mocked interface's realization
        when(mockedIdCImpl.getLeadingDelimiter()).thenReturn(MOCKEDIDC_LEADING);
        when(mockedIdCImpl.getIdentifierConcatenator()).thenReturn(MOCKEDIDC_CONCAT);
        when(mockedIdCImpl.getIdentifierDelimiter()).thenReturn(MOCKEDIDC_SEPARATOR);
        when(mockedIdCImpl.getTrailingDelimiter()).thenReturn(MOCKEDIDC_TRAILING);
        when(mockedIdCImpl.getConversionKey()).thenReturn(MOCKEDIDC_LEADING + MOCKEDIDC_SEPARATOR + MOCKEDIDC_TRAILING);
        when(mockedIdCImpl.getIdentifierRules()).thenReturn(map);
        return mockedIdCImpl;
    }

    /**
     * Automatically determine expected test result, when no failure is expected.
     *
     * @param fullName name to which delimiters have to be removed, according to the config
     * @param config   IdentifierConfiguration, with its delimiters
     * @return the provided string, with delimiters (only leading and trailing) removed
     */
    private static String oracle(String fullName, IdentifierConfiguration config) {
        return fullName.replace(config.getLeadingDelimiter(), "").
                replace(config.getTrailingDelimiter(), "");
    }

    /**
     * Parameters association. The parameter involved has been declared earlier as
     * attributes in this class.
     *
     * @return array containing actual values of the test parameters
     */
    @Parameterized.Parameters
    public static Collection<RemoveDelimitersTestParams[]> getTestParameters() {
        IdentifierConfiguration config = new IdConfigurationTestImpl();
        IdentifierConfiguration mockedIdCImpl = generateIdCMock();
        String delimIdC = "\"first.second\"";
        String delimMock = MOCKEDIDC_LEADING + "first" + MOCKEDIDC_SEPARATOR + "second" + MOCKEDIDC_TRAILING;
        List<RemoveDelimitersTestParams[]> args = Arrays.asList(new RemoveDelimitersTestParams[][]{
                {new RemoveDelimitersTestParams(mockedIdCImpl, config, "default", delimIdC, oracle(delimIdC, mockedIdCImpl))},
                {new RemoveDelimitersTestParams(mockedIdCImpl, config, "default", delimMock, oracle(delimMock, mockedIdCImpl))},
                {new RemoveDelimitersTestParams(config, mockedIdCImpl, "test", delimMock, oracle(delimMock, config))},
                {new RemoveDelimitersTestParams(config, mockedIdCImpl, "test2", delimIdC, oracle(delimIdC, config))},
                {new RemoveDelimitersTestParams(config, mockedIdCImpl, "non-existent", delimIdC, oracle(delimIdC, config))},
                {new RemoveDelimitersTestParams(mockedIdCImpl, config, "default", "", "")},
                {new RemoveDelimitersTestParams(mockedIdCImpl, config, "default", null, null)},
                {new RemoveDelimitersTestParams(null, config, "default", delimMock, FAIL)},
                {new RemoveDelimitersTestParams(config, null, "default", delimMock, delimMock)},
        });
        return args;
    }

    @Test
    public void makeIdValidCheckTest() {
        try {
            this.idUtils.setIdentifierConfiguration(currentConf);
            String ret = this.idUtils.removeDelimiters(newConf, rule, fullName);
            assertEquals(expectedBehavior, ret);
            if (fullName != null)
                assertEquals(expectedBehavior.length(), ret.length());
        } catch (Exception e) {
            assertTrue(buildExceptionString(e), expectedBehavior == FAIL);
            return;
        }
        assertTrue(EXPECTED_TO_FAIL, expectedBehavior != FAIL);
    }


    /**
     * Clean up the environment, at the ond of each test.
     */
    @After
    public void tearDown() {
        // Mock is used as parameter, so it is not closed here.
    }

}
