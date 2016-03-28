package journeymap.server.properties;

import journeymap.common.properties.PropertiesBaseTest;

/**
 * DimensionProperties tests.
 */
public class DimensionPropertiesTest extends PropertiesBaseTest<DimensionProperties>
{
    // Reuse the same dimension per testcase
    int dimension = rand.nextInt(100);

    @Override
    protected DimensionProperties createDefaultInstance()
    {
        DimensionProperties p = new DimensionProperties(dimension);
        return p;
    }

    @Override
    protected DimensionProperties createRandomizedInstance()
    {
        DimensionProperties p = new DimensionProperties(dimension);
        p.enabled.set(rand.nextBoolean());
        p.opCaveMapping.set(rand.nextBoolean());
        p.playerCaveMapping.set(rand.nextBoolean());
        p.whiteListCaveMapping.set(String.format("user%s, user%s", rand.nextInt(1000), rand.nextInt(1000)));
        p.opRadar.set(rand.nextBoolean());
        p.playerRadar.set(rand.nextBoolean());
        p.whiteListRadar.set(String.format("user%s, user%s", rand.nextInt(1000), rand.nextInt(1000)));
        return p;
    }
}
