/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.render.draw;

import com.google.common.base.Strings;
import journeymap.client.data.DataCache;
import journeymap.client.model.EntityDTO;
import journeymap.client.properties.InGameMapProperties;
import journeymap.client.render.map.GridRenderer;
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
        final boolean showVillagers = mapProperties.showVillagers.get();
        final EntityDisplay mobDisplay = mapProperties.mobDisplay.get();
        final EntityDisplay playerDisplay = mapProperties.playerDisplay.get();
        final boolean showMobHeading = mapProperties.showMobHeading.get();
        final boolean showPlayerHeading = mapProperties.showPlayerHeading.get();
        final boolean showEntityNames = mapProperties.showEntityNames.get();
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

                    if (!showVillagers && (dto.profession != null || dto.npc)) {
                        continue;
                    }

                    // Draw entity icon and label
                    DrawEntityStep drawStep = DataCache.INSTANCE.getDrawEntityStep(entityLiving);

                    isPlayer = entityLiving instanceof EntityPlayer;
                    if (isPlayer)
                    {
                        locatorImg = EntityDisplay.getLocatorTexture(playerDisplay, showPlayerHeading);
                        entityIcon = EntityDisplay.getEntityTexture(playerDisplay, entityLiving.getUniqueID(), entityLiving.getName());
                        drawStep.update(playerDisplay, locatorImg, entityIcon, dto.color, showPlayerHeading, false);
                        drawStepList.add(drawStep);
                    }
                    else
                    {
                        locatorImg = EntityDisplay.getLocatorTexture(mobDisplay, showMobHeading);
                        entityIcon = EntityDisplay.getEntityTexture(mobDisplay, dto.entityIconLocation);

                        EntityDisplay actualDisplay = mobDisplay;
                        if (!mobDisplay.isDots() && entityIcon == null)
                        {
                            // Missing icon?  Use dot.
                            actualDisplay = mobDisplay.isLarge() ? EntityDisplay.LargeDots : EntityDisplay.SmallDots;
                            entityIcon = EntityDisplay.getEntityTexture(actualDisplay, dto.entityIconLocation);
                        }
                        drawStep.update(actualDisplay, locatorImg, entityIcon, dto.color, showMobHeading, showEntityNames);
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
