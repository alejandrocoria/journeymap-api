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
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.model.EntityDTO;
import journeymap.client.properties.InGameMapProperties;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.render.texture.TextureImpl;
import journeymap.client.ui.minimap.MobDisplay;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders an entity image in the MapOverlay.
 *
 * @author mwoodman
 */
public class RadarDrawStepFactory
{
    public List<DrawStep> prepareSteps(List<EntityDTO> entityDTOs, GridRenderer grid, float drawScale, InGameMapProperties mapProperties)
    {
        int color = 0xcccccc;
        final boolean minimal = (mapProperties.mobDisplay.get() == MobDisplay.Dots);
        final boolean showAnimals = mapProperties.showAnimals.get();
        final boolean showPets = mapProperties.showPets.get();
        final boolean showMobHeading = mapProperties.showMobHeading.get();
        final boolean showPlayerHeading = mapProperties.showPlayerHeading.get();
        final List<DrawStep> drawStepList = new ArrayList<DrawStep>();

        try
        {
            TextureImpl entityIcon = null;
            TextureImpl locatorImg = null;
            boolean isPlayer, isPet;

            String playername = ForgeHelper.INSTANCE.getEntityName(FMLClientHandler.instance().getClient().thePlayer);
            TextureCache tc = TextureCache.instance();

            for (EntityDTO dto : entityDTOs)
            {
                EntityLivingBase entityLiving = dto.entityLivingRef.get();
                if (entityLiving == null)
                {
                    continue;
                }

                try
                {
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

                    if (grid.getPixel(dto.posX, dto.posZ) != null)
                    {
                        isPlayer = entityLiving instanceof EntityPlayer;

                        // Determine and draw locator
                        if (dto.hostile)
                        {
                            color = 0xff0000;
                            locatorImg = tc.getHostileLocator();
                        }
                        else
                        {
                            if (!Strings.isNullOrEmpty(dto.owner) && playername.equals(dto.owner))
                            {
                                color = 0x0077ff;
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
                                    color = 0xcccccc;
                                    locatorImg = tc.getNeutralLocator();
                                }
                            }
                        }

                        // Draw entity icon and label
                        DrawEntityStep drawStep = DataCache.INSTANCE.getDrawEntityStep(entityLiving);
                        if (isPlayer)
                        {
                            entityIcon = tc.getPlayerSkin(ForgeHelper.INSTANCE.getEntityName(entityLiving));
                            drawStep.update(false, locatorImg, entityIcon, showPlayerHeading);
                        }
                        else
                        {
                            entityIcon = tc.getEntityIconTexture(dto.entityIconLocation);
                            drawStep.update(false, locatorImg, entityIcon, showMobHeading);
                            if (minimal || entityIcon == null)
                            {
                                drawStep.updateMinimal(color);
                            }
                        }
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
