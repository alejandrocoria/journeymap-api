package journeymap.server.properties;

/**
 * Used to load basic permissions with out any load errors if it is a global or dimension property.
 * <p>
 * We only care about mapping and radar properties.
 */
public class Permissions extends DimensionProperties
{

    public Permissions()
    {
        super(0);
        this.enabled.set(true);
    }

}
