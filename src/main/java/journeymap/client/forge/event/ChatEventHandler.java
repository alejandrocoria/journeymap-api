/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.forge.event;

import com.google.common.base.Strings;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import journeymap.client.data.DataCache;
import journeymap.client.feature.FeatureManager;
import journeymap.client.log.LogFormatter;
import journeymap.client.ui.UIManager;
import journeymap.common.Journeymap;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.EnumSet;
import java.util.Set;

// 1.7.10
// 1.8
//import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//import net.minecraftforge.fml.relauncher.Side;
//import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by mwoodman on 1/29/14.
 */
@SideOnly(Side.CLIENT)
public class ChatEventHandler implements EventHandlerManager.EventHandler
{

    Set<String> featureControlCodes = FeatureManager.instance().getControlCodes();

    @Override
    public EnumSet<EventHandlerManager.BusType> getBus()
    {
        return EnumSet.of(EventHandlerManager.BusType.MinecraftForgeBus);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void invoke(ClientChatReceivedEvent event)
    {
        if (event.message != null)
        {
            try
            {
                String text = event.message.getFormattedText();
                if (!Strings.isNullOrEmpty(text))
                {
                    checkForControlCode(text.replaceAll(EnumChatFormatting.RESET.toString(), ""));
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
                DataCache.instance().purge();
                UIManager.getInstance().reset();
            }
        }
    }
}
