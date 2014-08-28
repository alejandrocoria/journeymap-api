package net.techbrew.journeymap.render.overlay;

import com.google.common.base.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.EntityDTO;
import net.techbrew.journeymap.properties.MapProperties;
import net.techbrew.journeymap.render.draw.DrawEntityStep;
import net.techbrew.journeymap.render.draw.DrawStep;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders an entity image in the MapOverlay.
 *
 * @author mwoodman
 */
public class OverlayRadarRenderer
{

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
            String iconSetName = mapProperties.getEntityIconSetName().get();

            for (EntityDTO dto : entityDTOs)
            {
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

                        // Draw entity icon and label
                        if (isPlayer)
                        {
                            entityIcon = tc.getPlayerSkin(dto.username);
                            DrawEntityStep drawStep = DataCache.instance().getDrawEntityStep(dto);
                            drawStep.update(false, locatorImg, entityIcon);
                            drawStepList.add(drawStep);
                        }
                        else
                        {
                            entityIcon = tc.getEntityIconTexture(iconSetName, dto.filename);
                            if (entityIcon != null)
                            {
                                DrawEntityStep drawStep = DataCache.instance().getDrawEntityStep(dto);
                                drawStep.update(false, locatorImg, entityIcon);
                                drawStepList.add(drawStep);
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    JourneyMap.getLogger().error("Exception during prepareSteps: " + LogFormatter.toString(e));
                }
            }
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().error("Throwable during prepareSteps: " + LogFormatter.toString(t));
        }

        return drawStepList;
    }

}
