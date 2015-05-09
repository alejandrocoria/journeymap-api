package net.techbrew.journeymap.model;

import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.properties.config.Config;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.component.JmUI;
import net.techbrew.journeymap.ui.dialog.OptionsManager;

import java.util.ArrayList;

/**
 * Simple model for GSON binding to assets/journeymap/lang/message/splash*.json
 */
public class SplashInfo
{
    public ArrayList<Line> lines = new ArrayList<Line>();

    public SplashInfo()
    {
    }

    public static class Line
    {
        public String label;
        public String action;

        public Line()
        {
        }

        public boolean hasAction()
        {
            return action != null && action.trim().length() > 0;
        }

        public void invokeAction(JmUI returnUi)
        {
            if (!hasAction())
            {
                return;
            }
            String[] parts = this.action.split("#");
            String className = parts[0];

            try
            {
                Class<? extends JmUI> uiClass = (Class<? extends JmUI>) Class.forName("net.techbrew.journeymap.ui.dialog." + className);

                if (uiClass.equals(OptionsManager.class) && parts.length > 0)
                {
                    Config.Category category = Config.Category.valueOf(parts[1]);
                    UIManager.getInstance().openOptionsManager(returnUi, category);
                }
                else
                {
                    UIManager.getInstance().open(uiClass, returnUi);
                }

            }
            catch (Throwable t)
            {
                JourneyMap.getLogger().error("Couldn't invoke action: " + action + ": " + LogFormatter.toString(t));
            }
        }
    }
}
