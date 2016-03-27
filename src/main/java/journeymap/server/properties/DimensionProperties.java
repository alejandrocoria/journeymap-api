package journeymap.server.properties;

import com.google.common.io.Files;
import journeymap.common.properties.config.BooleanField;

import java.io.File;

/**
 * Permissions which can be applied to a specific dimension.
 */
public class DimensionProperties extends PermissionProperties
{
    // Whether or not these properties should override GlobalProperties
    public final BooleanField enabled = new BooleanField(ServerCategory.General, "Enable Configuration", false).categoryMaster(true);
    protected final Integer dimension;

    /**
     * Constructor.
     *
     * @param dimension  the dimension id this applies to
     */
    public DimensionProperties(Integer dimension)
    {
        super(String.format("Dimension %s Configuration", dimension),
                "Overrides the Global Server Configuration for this dimension");
        this.dimension = dimension;
    }

    public static void main(String[] args) throws Exception
    {
        boolean fix = true;

        DimensionProperties p = new DimensionProperties(-1);
        System.out.println(p.isValid(fix));

        // Verbose result
        System.out.println("DimensionProperties Verbose: " + p.toJsonString(true));

        // Compact result
        System.out.println("DimensionProperties Compact: " + p.toJsonString(false));

        System.out.println("GlobalProperties Verbose: " + new GlobalProperties().toJsonString(true));

        System.out.println("GlobalProperties Compact: " + new GlobalProperties().toJsonString(false));

        GlobalProperties c1 = new GlobalProperties();
        c1.isValid(true);

        File temp = File.createTempFile(c1.getFileName(), ".json");
        c1.save(temp, false);

        System.out.println("GlobalProperties Compact File: \n\n" + Files.toString(temp, UTF8));
    }

    @Override
    public String getName()
    {
        return "dim" + dimension;
    }
}
