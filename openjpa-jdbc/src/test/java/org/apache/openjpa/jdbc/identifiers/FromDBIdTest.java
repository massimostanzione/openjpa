package org.apache.openjpa.jdbc.identifiers;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.identifier.DBIdentifier.DBIdentifierType;
import org.apache.openjpa.jdbc.identifier.DBIdentifierUtil;
import org.apache.openjpa.jdbc.identifier.DBIdentifierUtilImpl;
import org.apache.openjpa.jdbc.identifier.DefaultIdentifierConfiguration;
import org.apache.openjpa.lib.identifier.IdentifierConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.apache.openjpa.jdbc.identifiers.utils.ISW2TestUtils.*;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class FromDBIdTest {
    // Test parameters.
    private String name;
    private DBIdentifierType id;
    private String expectedBehavior;

    // Class under test.
    private DBIdentifierUtil util;

    /**
     * Inner class containing the test parameters.
     */
    private static class FromDBIdTestParams {
        private String name;
        private DBIdentifierType id;
        private String expectedBehavior;

        public FromDBIdTestParams(String name, DBIdentifierType id, String expectedBehavior) {
            this.name = name;
            this.id = id;
            this.expectedBehavior = expectedBehavior;
        }
    }

    public FromDBIdTest(FromDBIdTestParams params) {
        configure(params);
    }

    /**
     * Link class parameters with test parameters.
     *
     * @param params test parameters
     */
    private void configure(FromDBIdTestParams params) {
        this.name = params.name;
        this.id = params.id;
        this.expectedBehavior = params.expectedBehavior;
    }

    /**
     * Before each test, instantiate the class under test
     * and assign the configuration to the instance.
     */
    @Before
    public void setup() {
        this.util = new DBIdentifierUtilImpl();
        IdentifierConfiguration conf = new DefaultIdentifierConfiguration();
        this.util.setIdentifierConfiguration(conf);
    }

    /**
     * Automatically determine expected test result, when no failure is expected.
     *
     * @param name the id to be returned
     * @param id   identificator type
     * @return the expected test result
     */
    private static String computeExpected(String name, DBIdentifierType id) {
        if (id == DBIdentifierType.COLUMN_DEFINITION || id == DBIdentifierType.CONSTANT || name == EMPTY_STRING)
            return name;
        // Append DefaultIdentifierConfiguration's delimiters
        return "\"" + name + "\"";
    }

    /**
     * Parameters association. The parameter involved has been declared earlier as
     * attributes in this class.
     *
     * @return array containing actual values of the test parameters
     */
    @Parameterized.Parameters
    public static Collection<FromDBIdTestParams[]> getTestParameters() {
        FromDBIdTestParams[] allParams =
                new FromDBIdTestParams[]{
                        new FromDBIdTestParams(VALID_STRING, DBIdentifierType.DEFAULT, computeExpected(VALID_STRING, DBIdentifierType.DEFAULT)),
                        new FromDBIdTestParams(EMPTY, DBIdentifierType.DEFAULT, computeExpected(EMPTY, DBIdentifierType.DEFAULT)),
                        new FromDBIdTestParams(null, DBIdentifierType.DEFAULT, null),
                        new FromDBIdTestParams(VALID_STRING, null, FAIL),
                        new FromDBIdTestParams(VALID_STRING, DBIdentifierType.NULL, FAIL),
                };
        // Since DBIdentifierType is an enumeration, iterate automatically through it
        for (DBIdentifierType idtype : DBIdentifierType.values()) {
            if (idtype != DBIdentifierType.NULL && idtype != DBIdentifierType.DEFAULT) {
                FromDBIdTestParams moreParams = new FromDBIdTestParams(VALID_STRING, idtype, computeExpected(VALID_STRING, idtype));
                allParams = ArrayUtils.add(allParams, moreParams);
            }
        }
        FromDBIdTestParams[][] arr = new FromDBIdTestParams[][]{};
        for (FromDBIdTestParams p : allParams) {
            arr = ArrayUtils.add(arr, new FromDBIdTestParams[]{p});
        }
        List<FromDBIdTestParams[]> args = Arrays.asList(arr);
        return args;
    }

    @Test
    public void fromDBIdTest() {
        DBIdentifier ret = null;
        try {
            ret = util.fromDBName(name, id);
            assertNotNull("Returned a null object.", ret);
            assertTrue(ret instanceof DBIdentifier);
            assertEquals("Different name received.", expectedBehavior, ret.getName());
            assertEquals("Different Type received.", name == null ? DBIdentifierType.NULL : id, ret.getType());
            assertFalse("Expecting an exception, but was not thrown.", expectedBehavior == FAIL || expectedBehavior == FAIL);
        } catch (Exception e) {
            assertTrue(buildExceptionString(e), expectedBehavior == FAIL);
            return;
        }
        assertTrue(EXPECTED_TO_FAIL, expectedBehavior != FAIL);
    }
}
