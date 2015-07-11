/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model;

import journeymap.client.JourneyMap;
import journeymap.client.log.LogFormatter;
import journeymap.client.properties.config.Config;
import journeymap.client.ui.UIManager;
import journeymap.client.ui.component.JmUI;
import journeymap.client.ui.dialog.OptionsManager;

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
                Class<? extends JmUI> uiClass = (Class<? extends JmUI>) Class.forName("journeymap.client.ui.dialog." + className);

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
