/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.render.overlay;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.EntityDTO;
import net.techbrew.journeymap.properties.MapProperties;
import net.techbrew.journeymap.render.draw.DrawCenteredLabelStep;
import net.techbrew.journeymap.render.draw.DrawEntityStep;
import net.techbrew.journeymap.render.draw.DrawPlayerStep;
import net.techbrew.journeymap.render.draw.DrawStep;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

;

/**
 * Renders an entity image in the MapOverlay.
 *
 * @author mwoodman
 */
public class OverlayRadarRenderer
{

    final ArrayList<DrawStep> drawStepList = new ArrayList<DrawStep>();

    public List<DrawStep> prepareSteps(List<EntityDTO> entityDTOs, GridRenderer grid, float drawScale, MapProperties mapProperties)
    {

        final boolean showAnimals = mapProperties.showAnimals.get();
        final boolean showPets = mapProperties.showPets.get();
        final int fontHeight = 14;
        final Color labelBg = Color.darkGray.darker();

        drawStepList.retainAll(Collections.EMPTY_LIST);

        try
        {

            double heading;
            TextureImpl entityIcon, locatorImg;
            String filename, owner;
            Boolean isHostile, isPet, isPlayer;
            boolean filterAnimals = (showAnimals != showPets);
            //FontMetrics fm = g2D.getFontMetrics();
            String playername = FMLClientHandler.instance().getClient().thePlayer.getDisplayName();
            TextureCache tc = TextureCache.instance();
            String iconSetName = mapProperties.getEntityIconSetName().get();
            int iconOffset = iconSetName.equals(FileHandler.MOB_ICON_SET_3D) ? (int) (8 * drawScale) : 0;

            for (EntityDTO dto : entityDTOs)
            {
                isHostile = Boolean.TRUE.equals(dto.hostile);
                owner = dto.owner;
                isPet = playername.equals(owner) && dto.entityLiving instanceof EntityTameable;

                // Skip animals/pets if needed
                if (filterAnimals && !isHostile)
                {
                    if (!showPets && isPet)
                    {
                        continue;
                    }

                    if(!showAnimals && !isPet)
                    {
                        continue;
                    }
                }

                double posX = (Double) dto.posX;
                double posZ = (Double) dto.posZ;

                if (grid.getPixel(posX, posZ) != null)
                {
                    filename = (String) dto.filename;
                    isPlayer = filename.startsWith("/skin/");

                    // Determine and draw locator
                    if (isHostile)
                    {
                        locatorImg = tc.getHostileLocator();
                    }
                    else
                    {
                        if (isPet)
                        {
                            locatorImg = tc.getPetLocator();
                        }
                        else
                        {
                            if (isPlayer)
                            {
                                locatorImg = tc.getOtherLocator();
                            }
                            else
                            {
                                locatorImg = tc.getNeutralLocator();
                            }
                        }
                    }

                    // Draw locator icon
                    Entity entity = (Entity) dto.entityLiving;


                    // Draw entity icon and label
                    if (isPlayer)
                    {
                        entityIcon = tc.getPlayerSkin(dto.username);
                        drawStepList.add(new DrawPlayerStep((EntityPlayer) entity, entityIcon));
                    }
                    else
                    {
                        entityIcon = tc.getEntityIconTexture(iconSetName, filename);
                        if (entityIcon != null)
                        {
                            int bottomMargin = isPlayer ? 0 : iconOffset;

                            DrawEntityStep drawStep = DataCache.instance().getDrawEntityStep(entity);
                            drawStep.update(false, locatorImg, entityIcon, bottomMargin);

                            drawStepList.add(drawStep);
                        }

                        if (dto.customName != null)
                        {
                            drawStepList.add(new DrawCenteredLabelStep(posX, posZ, dto.customName, entityIcon.height / 2, labelBg, Color.white));
                        }
                    }
                }
            }
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().severe("Error during prepareSteps: " + LogFormatter.toString(t));
        }

        return drawStepList;
    }

}
