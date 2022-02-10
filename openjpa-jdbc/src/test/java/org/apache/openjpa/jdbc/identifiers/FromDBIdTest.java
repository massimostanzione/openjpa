package org.apache.openjpa.jdbc.identifiers;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.identifier.DBIdentifier.DBIdentifierType;
import org.apache.openjpa.jdbc.identifier.DBIdentifierUtil;
import org.apache.openjpa.jdbc.identifier.DBIdentifierUtilImpl;
import org.apache.openjpa.jdbc.identifier.DefaultIdentifierConfiguration;
import org.apache.openjpa.lib.identifier.IdentifierConfiguration;
import org.apache.openjpa.lib.identifier.IdentifierUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.apache.openjpa.jdbc.identifiers.utils.ISW2TestUtils.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class FromDBIdTest {
    // Test parameters.
    private static String name;
    private static DBIdentifierType id;
    private static IdentifierConfiguration conf;
    private static String expectedBehavior;

    // Class under test.
    private DBIdentifierUtil util;

    /**
     * Inner class containing the test parameters.
     */
    private static class FromDBIdTestParams {
        private String name;
        private DBIdentifierType id;
        private IdentifierConfiguration conf;
        private String expectedBehavior;

        public FromDBIdTestParams(String name, DBIdentifierType id, IdentifierConfiguration conf, String expectedBehavior) {
            this.name = name;
            this.id = id;
            this.conf = conf;
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
        this.conf = params.conf;
        this.expectedBehavior = params.expectedBehavior;
    }

    /**
     * Before each test, instantiate the class under test
     * and assign the configuration to the instance.
     */
    @Before
    public void setup() {
        this.util = new DBIdentifierUtilImpl();
    }

    /**
     * Automatically determine expected test result, when no failure is expected.
     *
     * @param name the id to be returned
     * @param id   identificator type
     * @return the expected test result
     */
    private static String oracle(String name, DBIdentifierType id) {
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

        IdentifierConfiguration config = new DefaultIdentifierConfiguration();
        FromDBIdTestParams[] allParams =
                new FromDBIdTestParams[]{
                        new FromDBIdTestParams(VALID_STRING, DBIdentifierType.DEFAULT, config, oracle(VALID_STRING, DBIdentifierType.DEFAULT)),
                        new FromDBIdTestParams(EMPTY, DBIdentifierType.DEFAULT, config, oracle(EMPTY, DBIdentifierType.DEFAULT)),
                        new FromDBIdTestParams(null, DBIdentifierType.DEFAULT, config, null),
                        new FromDBIdTestParams(VALID_STRING, null, config, FAIL),
                        new FromDBIdTestParams(VALID_STRING, DBIdentifierType.NULL, config, FAIL),
                };

        // Since DBIdentifierType is an enumeration, iterate automatically through it
        for (DBIdentifierType idtype : DBIdentifierType.values()) {
            if (idtype != DBIdentifierType.NULL && idtype != DBIdentifierType.DEFAULT) {
                FromDBIdTestParams moreParams = new FromDBIdTestParams(VALID_STRING, idtype, config, oracle(VALID_STRING, idtype));
                allParams = ArrayUtils.add(allParams, moreParams);
            }
        }

        //Enhance coverage (adequacy):
        Pair<String, String>[] combinations =
                new ImmutablePair[]{
                        ImmutablePair.of(IdentifierUtil.CASE_LOWER, IdentifierUtil.CASE_LOWER),
                        ImmutablePair.of(IdentifierUtil.CASE_PRESERVE, IdentifierUtil.CASE_LOWER),
                        ImmutablePair.of(IdentifierUtil.CASE_PRESERVE, IdentifierUtil.CASE_UPPER),
                        ImmutablePair.of(IdentifierUtil.CASE_LOWER, IdentifierUtil.CASE_UPPER),
                        ImmutablePair.of(IdentifierUtil.CASE_UPPER, IdentifierUtil.CASE_LOWER),
                        ImmutablePair.of(IdentifierUtil.CASE_UPPER, IdentifierUtil.CASE_PRESERVE),
                        ImmutablePair.of(IdentifierUtil.CASE_LOWER, IdentifierUtil.CASE_PRESERVE),
                };
        List mocks = new ArrayList<IdentifierConfiguration>();
        for (Pair p : combinations) {
            IdentifierConfiguration customCaseMock = Mockito.spy(DefaultIdentifierConfiguration.class);
            when(customCaseMock.getDelimitedCase()).thenReturn(p.getLeft().toString());
            when(customCaseMock.getSchemaCase()).thenReturn(p.getRight().toString());
            mocks.add(customCaseMock);
        }
        for (Object mock : mocks) {
            FromDBIdTestParams p = new FromDBIdTestParams(VALID_STRING, DBIdentifierType.DEFAULT, (IdentifierConfiguration) mock, oracle(VALID_STRING, DBIdentifierType.DEFAULT));
            allParams = ArrayUtils.add(allParams, p);
        }


        IdentifierConfiguration delIdentMock = Mockito.spy(DefaultIdentifierConfiguration.class);
        when(delIdentMock.getSupportsDelimitedIdentifiers()).thenReturn(false);
        FromDBIdTestParams delIdentParam = new FromDBIdTestParams(VALID_STRING, DBIdentifierType.DEFAULT, (IdentifierConfiguration) delIdentMock, oracle(VALID_STRING, DBIdentifierType.DEFAULT));
        allParams = ArrayUtils.add(allParams, delIdentParam);

        IdentifierConfiguration delimitAllMock = Mockito.spy(DefaultIdentifierConfiguration.class);
        when(delimitAllMock.getDelimitedCase()).thenReturn("cAmEl cAsE");
        when(delimitAllMock.getSchemaCase()).thenReturn("different");
        when(delimitAllMock.delimitAll()).thenReturn(true);
        FromDBIdTestParams delimitAllMockCase = new FromDBIdTestParams("cAmEl cAsE", DBIdentifierType.DEFAULT, delimitAllMock, oracle("cAmEl cAsE", DBIdentifierType.DEFAULT));
        allParams = ArrayUtils.add(allParams, delimitAllMockCase);

        FromDBIdTestParams[][] arr = new FromDBIdTestParams[][]{};
        for (FromDBIdTestParams p : allParams) {
            arr = ArrayUtils.add(arr, new FromDBIdTestParams[]{p});
        }
        List<FromDBIdTestParams[]> args = Arrays.asList(arr);
        return args;
    }

    @Test
    public void fromDBIdTest() {
        this.util.setIdentifierConfiguration(conf);
        DBIdentifier ret = null;
        try {
            ret = this.util.fromDBName(name, id);
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
