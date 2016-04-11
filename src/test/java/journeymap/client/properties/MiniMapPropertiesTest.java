package journeymap.client.properties;

/**
 * MiniMapProperties tests.
 */
public class MiniMapPropertiesTest extends ClientPropertiesBaseTest<MiniMapProperties>
{
    // Reuse the same dimension per testcase
    int id = rand.nextInt(1) + 1;

    @Override
    protected MiniMapProperties createDefaultInstance()
    {
        return new MiniMapProperties(id);
    }

    @Override
    protected MiniMapProperties createRandomizedInstance()
    {
        MiniMapProperties p = new MiniMapProperties(id);
        randomizeInGameMapProperties(p);

        randomize(p.enabled);
        randomize(p.shape);
        randomize(p.position);
        randomize(p.showFps);
        randomize(p.showBiome);
        randomize(p.showLocation);
        randomize(p.sizePercent);
        randomize(p.frameAlpha);
        randomize(p.terrainAlpha);
        randomize(p.orientation);
        randomize(p.compassFontScale);
        randomize(p.showCompass);
        randomize(p.showReticle);
        randomize(p.reticleOrientation);

        return p;
    }
}
