package journeymap.server.oldservercode.reference;

/**
 * Created by Mysticdrew on 3/18/2015.
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


