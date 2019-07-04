package journeymap.server.config;

import journeymap.common.Journeymap;
import net.minecraftforge.common.config.Config;


@Config(modid = Journeymap.MOD_ID, type = Config.Type.INSTANCE, name = Journeymap.MOD_ID + "_server", category = "server")
public class ForgeConfig
{
    @Config.Name("Ops Admin Access")
    @Config.Comment({
            "Default, all Ops have access to Server Admin UI in the Options screen.",
            "If set to false, only users in the Admin List will have access.",
            "If set to true, all ops and users in the Admin List will have access."
    })
    public static Boolean opAccess = true;

    @Config.Name("Journeymap Server Admins")
    @Config.Comment({
            "Players in this list have access to the Journeymap's Server Admin Panel",
            "Add users by name or UUID, Prefer UUID as it is more secure!",
            "Each value on a new line with the example format provided. (please delete the default values)"
    })
    public static String[] playerNames = {"mysticdrew", "12341234132"};
}
