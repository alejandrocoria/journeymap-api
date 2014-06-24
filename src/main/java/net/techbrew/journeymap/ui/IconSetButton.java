package net.techbrew.journeymap.ui;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.gui.FontRenderer;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.properties.MapProperties;

import java.util.ArrayList;

/**
* Created by mwoodman on 6/24/2014.
*/
public class IconSetButton extends Button
{
    final String messageKey;
    final MapProperties mapProperties;

    public IconSetButton(int id, MapProperties mapProperties, String messageKey)
    {
        super(id, 0, 0, Constants.getString(messageKey,""));
        this.mapProperties = mapProperties;

        this.messageKey = messageKey;
        updateLabel();

        // Determine width
        fitWidth(FMLClientHandler.instance().getClient().fontRenderer);
    }

    protected void updateLabel()
    {
        ArrayList<String> validNames = FileHandler.getMobIconSetNames();
        if(!validNames.contains(mapProperties.getEntityIconSetName().get()))
        {
            mapProperties.getEntityIconSetName().set(validNames.get(0));
            mapProperties.save();
        }

        displayString = getSafeLabel(mapProperties.getEntityIconSetName().get());
    }

    protected String getSafeLabel(String label)
    {
        int maxLength = 13;
        if(label.length()>maxLength)
        {
            label = label.substring(0, maxLength - 3).concat("...");
        }

        return Constants.getString(messageKey,label);
    }

    @Override
    public int getFitWidth(FontRenderer fr)
    {
        int maxWidth = 0;
        for (String iconSetName : FileHandler.getMobIconSetNames())
        {
            String name = getSafeLabel(iconSetName);
            maxWidth = Math.max(maxWidth, FMLClientHandler.instance().getClient().fontRenderer.getStringWidth(name));
        }
        return maxWidth + 12;
    }

    @Override
    public void toggle()
    {
        ArrayList<String> validNames = FileHandler.getMobIconSetNames();
        int index = validNames.indexOf(mapProperties.getEntityIconSetName().get()) + 1;

        if(index==validNames.size() || index<0)
        {
            index = 0;
        }

        mapProperties.getEntityIconSetName().set(validNames.get(index));
        mapProperties.save();

        updateLabel();
    }
}
