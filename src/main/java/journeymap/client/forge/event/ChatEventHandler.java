/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.forge.event;

import com.google.common.base.Strings;
import journeymap.client.data.DataCache;
import journeymap.client.feature.FeatureManager;
import journeymap.client.ui.UIManager;
import journeymap.client.waypoint.WaypointParser;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Set;

/**
 * @author techbrew 1/29/14.
 */
@SideOnly(Side.CLIENT)
public class ChatEventHandler implements EventHandlerManager.EventHandler
{

    Set<String> featureControlCodes = FeatureManager.instance().getControlCodes();

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void invoke(ClientChatReceivedEvent event)
    {
        if (event.getMessage() != null)
        {
            try
            {
                String text = event.getMessage().getFormattedText();
                if (!Strings.isNullOrEmpty(text))
                {
                    WaypointParser.parseChatForWaypoints(event, text);
                    checkForControlCode(text.replaceAll(TextFormatting.RESET.toString(), ""));
                }
            }
            catch (Exception e)
            {
                Journeymap.getLogger().warn("Unexpected exception on ClientChatReceivedEvent: " + LogFormatter.toString(e));
            }
        }
    }

    private void checkForControlCode(String text)
    {
        if (text.contains("\u00a7"))
        {
            boolean resetRequired = false;
            for (String code : featureControlCodes)
            {
                if (text.contains(code))
                {
                    FeatureManager.instance().handleControlCode(code);
                    resetRequired = true;
                }
            }
            if (resetRequired)
            {
                DataCache.INSTANCE.purge();
                UIManager.INSTANCE.reset();
            }
        }
    }
}
