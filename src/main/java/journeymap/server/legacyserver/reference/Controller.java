package journeymap.server.legacyserver.reference;

/**
 * Created by Mysticdrew on 3/18/2016.
 */

public enum Controller
{
    FORGE,
    BUKKIT;

    private static Controller controller;

    public static Controller getController()
    {
        return controller;
    }

    public static void setController(Controller controller)
    {
        Controller.controller = controller;
    }
}


