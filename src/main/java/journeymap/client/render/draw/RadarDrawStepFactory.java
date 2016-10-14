/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.render.draw;

import com.google.common.base.Strings;
import journeymap.client.data.DataCache;
import journeymap.client.model.EntityDTO;
import journeymap.client.properties.InGameMapProperties;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.render.texture.TextureImpl;
import journeymap.client.ui.minimap.EntityDisplay;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders an entity image in the MapOverlay.
 *
 * @author mwoodman
 */
public class RadarDrawStepFactory
{
    public List<DrawStep> prepareSteps(List<EntityDTO> entityDTOs, GridRenderer grid, InGameMapProperties mapProperties)
    {
        final boolean showAnimals = mapProperties.showAnimals.get();
        final boolean showPets = mapProperties.showPets.get();
        final EntityDisplay mobDisplay = mapProperties.mobDisplay.get();
        final EntityDisplay playerDisplay = mapProperties.playerDisplay.get();
        final boolean showMobHeading = mapProperties.showMobHeading.get();
        final boolean showPlayerHeading = mapProperties.showPlayerHeading.get();
        final List<DrawStep> drawStepList = new ArrayList<DrawStep>();

        try
        {
            for (EntityDTO dto : entityDTOs)
            {
                try
                {
                    TextureImpl entityIcon = null;
                    TextureImpl locatorImg = null;
                    boolean isPlayer, isPet;

                    EntityLivingBase entityLiving = dto.entityLivingRef.get();
                    if (entityLiving == null)
                    {
                        continue;
                    }

                    if (grid.getPixel(dto.posX, dto.posZ) == null)
                    {
                        continue;
                    }

                    isPet = !Strings.isNullOrEmpty(dto.owner);
                    if (!showPets && isPet)
                    {
                        continue;
                    }

                    if (!showAnimals && dto.passiveAnimal)
                    {
                        if (!(isPet && showPets))
                        {
                            continue;
                        }
                    }

                    // Draw entity icon and label
                    DrawEntityStep drawStep = DataCache.INSTANCE.getDrawEntityStep(entityLiving);

                    isPlayer = entityLiving instanceof EntityPlayer;
                    if (isPlayer)
                    {
                        locatorImg = TextureCache.getLowerEntityTexture(playerDisplay, showPlayerHeading);
                        entityIcon = TextureCache.getUpperEntityTexture(playerDisplay, entityLiving.getName());
                        drawStep.update(playerDisplay, locatorImg, entityIcon, dto.color, showPlayerHeading);
                        drawStepList.add(drawStep);
                    }
                    else
                    {
                        locatorImg = TextureCache.getLowerEntityTexture(mobDisplay, showMobHeading);
                        entityIcon = TextureCache.getUpperEntityTexture(mobDisplay, dto.entityIconLocation);

                        EntityDisplay actualDisplay = mobDisplay;
                        if (!mobDisplay.isDots() && entityIcon == null)
                        {
                            // Missing icon?  Use dot.
                            actualDisplay = mobDisplay.isLarge() ? EntityDisplay.LargeDots : EntityDisplay.SmallDots;
                            entityIcon = TextureCache.getUpperEntityTexture(actualDisplay, dto.entityIconLocation);
                        }
                        drawStep.update(actualDisplay, locatorImg, entityIcon, dto.color, showMobHeading);
                        drawStepList.add(drawStep);
                    }

                }
                catch (Exception e)
                {
                    Journeymap.getLogger().error("Exception during prepareSteps: " + LogFormatter.toString(e));
                }
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error("Throwable during prepareSteps: " + LogFormatter.toString(t));
        }

        return drawStepList;
    }

}
