/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model;

import journeymap.client.properties.ClientCategory;
import journeymap.client.ui.UIManager;
import journeymap.client.ui.component.JmUI;
import journeymap.client.ui.dialog.OptionsManager;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.properties.Category;

import java.lang.reflect.Method;
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
            String action = null;
            if (parts.length > 0)
            {
                action = parts[1];
            }

            try
            {
                Class<? extends JmUI> uiClass = (Class<? extends JmUI>) Class.forName("journeymap.client.ui." + className);

                if (uiClass.equals(OptionsManager.class) && action != null)
                {
                    Category category = ClientCategory.valueOf(action);
                    UIManager.INSTANCE.openOptionsManager(returnUi, category);
                    return;
                }
                else if (action != null)
                {
                    String arg = parts.length == 3 ? parts[2] : null;
                    Method actionMethod;
                    try
                    {
                        JmUI ui = UIManager.INSTANCE.open(uiClass, returnUi);

                        if (arg == null)
                        {
                            actionMethod = uiClass.getMethod(action);
                            actionMethod.invoke(ui);
                        }
                        else
                        {
                            actionMethod = uiClass.getMethod(action, String.class);
                            actionMethod.invoke(ui, arg);
                        }
                        return;
                    }
                    catch (Exception e)
                    {
                        Journeymap.getLogger().warn("Couldn't perform action " + action + " on " + uiClass + ": " + e.getMessage());
                    }
                }

                UIManager.INSTANCE.open(uiClass, returnUi);
            }
            catch (Throwable t)
            {
                Journeymap.getLogger().error("Couldn't invoke action: " + action + ": " + LogFormatter.toString(t));
            }
        }
    }
}
