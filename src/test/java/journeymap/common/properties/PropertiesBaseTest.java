package journeymap.common.properties;

import journeymap.common.properties.config.BooleanField;
import journeymap.common.properties.config.EnumField;
import journeymap.common.properties.config.IntegerField;
import journeymap.common.properties.config.StringField;
import junit.framework.Assert;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

/**
 * Generic test code to ensure serialization/deserialization work correctly
 */
public abstract class PropertiesBaseTest<P extends PropertiesBase>
{
    // Randomizer per testcase
    protected Random rand = new Random();

    /**
     * Create a default instance of P
     */
    protected abstract P createDefaultInstance();

    /**
     * Create a randomized instance of P
     */
    protected abstract P createRandomizedInstance();

    /**
     * Default instance should be valid with the fix flag
     */
    @Test
    public void testDefaultInstanceValidity()
    {
        P p = createDefaultInstance();
        Assert.assertTrue(p.isValid(true));
    }

    /**
     * Randomized instance should be valid with the fix flag
     */
    @Test
    public void testRandomizedInstanceValidity()
    {
        P p = createRandomizedInstance();
        Assert.assertTrue(p.isValid(true));
    }

    /**
     * updateFrom() should make instances equivalent
     */
    @Test
    public void testUpdateFrom()
    {
        P p1 = createDefaultInstance();
        P p2 = createRandomizedInstance();
        P p3 = createRandomizedInstance();

        // Test default instance updated from randomized instance
        p1.updateFrom(p2);
        assertEqual(p2, p1);

        // Test default instance updated from 2nd randomized instance
        p1.updateFrom(p3);
        assertEqual(p3, p1);
    }

    /**
     * Instance2.load(Instance1 compact json file) should make instances equivalent
     */
    @Test
    public void testLoadCompactJson() throws Exception
    {
        P p1 = createRandomizedInstance();
        File temp = File.createTempFile(p1.getFileName(), ".json");
        p1.save(temp, false);

        P p2 = createRandomizedInstance();
        p2.load(temp, false);

        assertEqual(p1, p2);
    }

    /**
     * Instance2.load(Instance1 verbose json file) should make instances equivalent
     */
    @Test
    public void testLoadVerboseJson() throws Exception
    {
        P p1 = createRandomizedInstance();
        File temp = File.createTempFile(p1.getFileName(), ".json");
        p1.save(temp, true);

        P p2 = createRandomizedInstance();
        p2.load(temp, true);

        assertEqual(p1, p2);
    }

    /**
     * Use JSON output to confirm equivalency.
     *
     * @param p1 first
     * @param p2 second
     */
    protected void assertEqual(P p1, P p2)
    {
        // Check with compact json
        Assert.assertEquals(p1.toJsonString(false), p2.toJsonString(false));

        // Check with verbose json
        Assert.assertEquals(p1.toJsonString(true), p2.toJsonString(true));
    }

    protected void randomize(StringField field)
    {
        List<String> validValues = field.getValidValues();
        if (validValues != null && !validValues.isEmpty())
        {
            field.set(validValues.get(rand.nextInt(validValues.size())));
        }
        else
        {
            Assert.fail("Can't randomize a field without validValues: " + field);
        }
    }

    @SuppressWarnings("unchecked")
    protected void randomize(EnumField field)
    {
        EnumSet enumSet = field.getValidValues();
        ArrayList<? extends Enum> list = new ArrayList<Enum>(enumSet);
        field.set(list.get(rand.nextInt(list.size())));
    }

    protected void randomize(IntegerField field)
    {
        int min = field.getMinValue();
        int max = field.getMaxValue();
        int range = max - min;

        field.set(rand.nextInt(range) + min);
    }

    protected void randomize(BooleanField field)
    {
        field.set(rand.nextBoolean());
    }

}
