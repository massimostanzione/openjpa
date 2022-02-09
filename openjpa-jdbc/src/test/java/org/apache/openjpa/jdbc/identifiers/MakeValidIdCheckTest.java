package org.apache.openjpa.jdbc.identifiers;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.identifier.DBIdentifier.DBIdentifierType;
import org.apache.openjpa.jdbc.identifier.DBIdentifierUtil;
import org.apache.openjpa.jdbc.identifier.DBIdentifierUtilImpl;
import org.apache.openjpa.jdbc.identifier.DefaultIdentifierConfiguration;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.NameSet;
import org.apache.openjpa.jdbc.schema.SchemaGroup;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.lib.identifier.IdentifierConfiguration;
import org.apache.openjpa.lib.identifier.IdentifierRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.apache.openjpa.jdbc.identifiers.utils.ISW2TestUtils.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class MakeValidIdCheckTest {
    // Test parameters.
    private static DBIdentifier sname;
    private static NameSet set;
    private static int maxLen;
    private static boolean checkForUniqueness;
    private static String expectedBehavior;

    // Class under test.
    private DBIdentifierUtil util;

    private IdentifierConfiguration mockedIdCImpl;
    private Set<String> resWords;

    /**
     * Inner class containing the test parameters.
     */
    private static class MakeValidIdCheckParams {
        private DBIdentifier sname;
        private NameSet set;
        private int maxLen;
        private boolean checkForUniqueness;
        private String expected;

        public MakeValidIdCheckParams(DBIdentifier sname, NameSet set, int maxLen, boolean checkForUniqueness, String expectedBehavior) {
            this.sname = sname;
            this.set = set;
            this.maxLen = maxLen;
            this.checkForUniqueness = checkForUniqueness;
            this.expected = expectedBehavior;
        }
    }

    public MakeValidIdCheckTest(MakeValidIdCheckParams params) {
        configure(params);
    }

    /**
     * Link class parameters with test parameters.
     *
     * @param params test parameters
     */
    public void configure(MakeValidIdCheckParams params) {
        this.sname = params.sname;
        this.set = params.set;
        this.maxLen = params.maxLen;
        this.checkForUniqueness = params.checkForUniqueness;
        this.expectedBehavior = params.expected;
    }

    /**
     * Before each test, instantiate the class under test
     * and assign the configuration to the instance.
     * Exceptions not specifically handled because not under test.
     */
    @Before
    public void setup() {
        this.util = new DBIdentifierUtilImpl();
        mockedIdCImpl = generateDIdCMock();
        this.util.setIdentifierConfiguration(mockedIdCImpl);
    }

    /**
     * In order to have a different implementation of the DefaultIdentifierConfiguration class,
     * with only some of its methods to be changed in a custom way, a "spy" mock of it is needed.
     * Further details about this choice can be faound in the project's final report.
     *
     * @return a ready-to-use DefaultIdentifierConfiguration mock
     * @see DefaultIdentifierConfiguration
     */
    public static IdentifierConfiguration generateDIdCMock() {
        IdentifierConfiguration mockedIdCImpl = Mockito.spy(DefaultIdentifierConfiguration.class);

        // Identifier rule to be assigned to the mock
        IdentifierRule ir = new IdentifierRule();
        MockitoAnnotations.initMocks(ir);
        ir.setReservedWords(getResWords());
        ir.setDelimitReservedWords(true);

        Map<Object, IdentifierRule> map = new HashMap<>();
        map.put("resWordsRule", ir);

        MockitoAnnotations.initMocks(mockedIdCImpl);
        // Specify reaction from the mocked class implementation
        when(mockedIdCImpl.getIdentifierRules()).thenReturn(map);
        when(mockedIdCImpl.getIdentifierRule(any())).thenReturn(ir);
        return mockedIdCImpl;
    }

    /**
     * Automatically determine expected test result, when no failure is expected.
     *
     * @param name   the id to be made valid
     * @param maxLen maximum length
     * @return the expected test result
     */
    private static String oracle(String name, int maxLen) {
        int len = maxLen == 0 ? name.length() : maxLen;
        return name.substring(0, len).toUpperCase();
    }

    /**
     * Parameters association. The parameter involved has been declared earlier as
     * attributes in this class.
     *
     * @return array containing actual values of the test parameters
     */
    @Parameterized.Parameters
    public static Collection<MakeValidIdCheckParams[]> getTestParameters() {
        DBIdentifier validId = DBIdentifier.newIdentifier("valid", DBIdentifierType.DEFAULT, true);
        DBIdentifier emptyId = DBIdentifier.newIdentifier("", DBIdentifierType.DEFAULT, true);
        DBIdentifier nullId = DBIdentifier.newIdentifier(null, DBIdentifierType.DEFAULT, true);
        DBIdentifier reservedId = DBIdentifier.newIdentifier("RESERVED1", DBIdentifierType.DEFAULT, true);

        SchemaGroup emptyNS = new SchemaGroup();
        SchemaGroup validNS = new SchemaGroup();
        Table tab = new Table();
        tab.setIdentifier(DBIdentifier.newIdentifier("VALID", DBIdentifierType.TABLE, true));
        Column col = new Column();
        col.setIdentifier(DBIdentifier.newIdentifier("Column", DBIdentifierType.COLUMN, true));
        tab.addColumn(col.getIdentifier());
        validNS.addSchema(tab.getIdentifier());

        Random rand = new Random();
        int len = validId.getName().length();
        List<MakeValidIdCheckParams[]> args = Arrays.asList(new MakeValidIdCheckParams[][]{
                {new MakeValidIdCheckParams(validId, validNS, -1, false, oracle(validId.getName(), 0))},
                {new MakeValidIdCheckParams(validId, validNS, 0, false, oracle(validId.getName(), 0))},
                {new MakeValidIdCheckParams(validId, validNS, 1, false, oracle(validId.getName(), 1))},
                {new MakeValidIdCheckParams(validId, validNS, len - 1, false, oracle(validId.getName(), len - 1))},
                {new MakeValidIdCheckParams(validId, validNS, len + 1, false, oracle(validId.getName(), 0))},
                {new MakeValidIdCheckParams(validId, validNS, len, true, "VALI1")},
                {new MakeValidIdCheckParams(validId, emptyNS, len, true, oracle(validId.getName(), len))},
                {new MakeValidIdCheckParams(reservedId, validNS, 9, false, "RESERVED0")},
                {new MakeValidIdCheckParams(emptyId, validNS, len, false, oracle(emptyId.getName(), 0))},
                {new MakeValidIdCheckParams(nullId, validNS, len, false, FAIL)},
                {new MakeValidIdCheckParams(validId, validNS, len, false, oracle(validId.getName(), 0))},
        });
        return args;
    }

    @Test
    public void makeIdValidCheckTest() {
        try {
            DBIdentifier idMadeValid = util.makeIdentifierValid(sname, set, maxLen, checkForUniqueness);
            assertNotNull("Returned a null item", idMadeValid);
            assertEquals(expectedBehavior, idMadeValid.getName());
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
        Mockito.reset(mockedIdCImpl);
    }
}
