package journeymap.client.properties;

/**
 * WebMapProperties tests.
 */
public class WebMapPropertiesTest extends ClientPropertiesBaseTest<WebMapProperties>
{
    @Override
    protected WebMapProperties createDefaultInstance()
    {
        return new WebMapProperties();
    }

    @Override
    protected WebMapProperties createRandomizedInstance()
    {
        WebMapProperties p = new WebMapProperties();
        randomizeMapProperties(p);

        randomize(p.enabled);
        randomizeCustomInteger(p.port);

        return p;
    }
}
