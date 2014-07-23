package net.techbrew.journeymap.render.overlay;

import com.google.common.base.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.model.EntityDTO;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.EntityHelper;
import net.techbrew.journeymap.properties.MapProperties;
import net.techbrew.journeymap.render.draw.DrawCenteredLabelStep;
import net.techbrew.journeymap.render.draw.DrawEntityStep;
import net.techbrew.journeymap.render.draw.DrawPlayerStep;
import net.techbrew.journeymap.render.draw.DrawStep;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders an entity image in the MapOverlay.
 *
 * @author mwoodman
 */
public class OverlayRadarRenderer
{
    final Color labelBg = Color.darkGray.darker();

    public List<DrawStep> prepareSteps(List<EntityDTO> entityDTOs, GridRenderer grid, float drawScale, MapProperties mapProperties)
    {
        final boolean showAnimals = mapProperties.showAnimals.get();
        final boolean showPets = mapProperties.showPets.get();
        final List<DrawStep> drawStepList = new ArrayList<DrawStep>();

        try
        {
            TextureImpl entityIcon, locatorImg;
            boolean isPlayer, isPet;

            String playername = Minecraft.getMinecraft().thePlayer.getDisplayName();
            TextureCache tc = TextureCache.instance();

            for (EntityDTO dto : entityDTOs)
            {
                try
                {
                    isPet = !Strings.isNullOrEmpty(dto.owner);

                    if(!showPets && isPet)
                    {
                        continue;
                    }

                    if(!showAnimals && dto.passiveAnimal)
                    {
                        if(!(isPet && showPets))
                        {
                            continue;
                        }
                    }

                    if (grid.getPixel(dto.posX, dto.posZ) != null)
                    {
                        isPlayer = dto.entityLiving instanceof EntityPlayer;

                        // Determine and draw locator
                        if (dto.hostile)
                        {
                            locatorImg = tc.getHostileLocator();
                        }
                        else
                        {
                            if (!Strings.isNullOrEmpty(dto.owner) && playername.equals(dto.owner))
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
                        drawStepList.add(new DrawEntityStep(dto.entityLiving, false, locatorImg, (int) (8 * drawScale)));

                        // Draw entity icon and label
                        if (isPlayer)
                        {
                            entityIcon = tc.getPlayerSkin(dto.username);
                            drawStepList.add(new DrawPlayerStep((EntityPlayer) dto.entityLiving, entityIcon));
                        }
                        else
                        {
                            entityIcon = tc.getEntityImage(dto.filename);
                            if (entityIcon != null)
                            {
                                int bottomMargin = isPlayer ? 0 : (int) (8 * drawScale);
                                drawStepList.add(new DrawEntityStep(dto.entityLiving, true, entityIcon, bottomMargin));
                            }

                            if (dto.customName != null)
                            {
                                drawStepList.add(new DrawCenteredLabelStep(dto.posX, dto.posZ, dto.customName, entityIcon.height / 2, labelBg, Color.white));
                            }
                        }
                    }
                }
                catch(Exception e)
                {
                    JourneyMap.getLogger().severe("Exception during prepareSteps: " + LogFormatter.toString(e));
                }
            }
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().severe("Throwable during prepareSteps: " + LogFormatter.toString(t));
        }

        return drawStepList;
    }

}
