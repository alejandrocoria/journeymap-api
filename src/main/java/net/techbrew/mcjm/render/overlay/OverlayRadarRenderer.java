package net.techbrew.mcjm.render.overlay;

import net.minecraft.client.Minecraft;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.data.EntityKey;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.render.draw.DrawCenteredLabelStep;
import net.techbrew.mcjm.render.draw.DrawEntityStep;
import net.techbrew.mcjm.render.draw.DrawStep;
import net.techbrew.mcjm.render.texture.TextureCache;
import net.techbrew.mcjm.render.texture.TextureImpl;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

;

/**
 * Renders an entity image in the MapOverlay.
 * 
 * @author mwoodman
 *
 */
public class OverlayRadarRenderer {

	public List<DrawStep> prepareSteps(List<Map> critters, GridRenderer grid, double fontScale) {
		
		final boolean showAnimals = PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_ANIMALS);
		final boolean showPets = PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_PETS);
		final int fontHeight = 14;
		final Color labelBg = Color.darkGray.darker();
		
		final List<DrawStep> drawStepList = new ArrayList<DrawStep>();
		
		try {
			
			double heading;
			TextureImpl entityIcon, locatorImg;
			String filename, owner;
			Boolean isHostile, isPet, isPlayer;
			boolean filterAnimals = (showAnimals!=showPets);
			//FontMetrics fm = g2D.getFontMetrics();
			String playername = Minecraft.getMinecraft().thePlayer.getDisplayName();
			TextureCache tc = TextureCache.instance();
			
			for(Map critter : critters) {
				
				isHostile = Boolean.TRUE.equals(critter.get(EntityKey.hostile));
				
				owner = (String) critter.get(EntityKey.owner);
				isPet = playername.equals(owner);					
				
				// Skip animals/pets if needed
				if(filterAnimals && !isHostile) {						
					if(showPets != isPet) {
						continue;
					}
				}
				
				double posX = (Double) critter.get(EntityKey.posX);
                double posZ = (Double) critter.get(EntityKey.posZ);

				if(grid.getPixel(posX, posZ)!=null) {
					filename = (String) critter.get(EntityKey.filename);
					heading = (Double) critter.get(EntityKey.heading);					
					isPlayer = filename.startsWith("/skin/");

					// Determine and draw locator
					if(isHostile) {
						locatorImg = tc.getHostileLocator();
					} else if(isPet) {
						locatorImg = tc.getPetLocator();
					} else if(isPlayer) {
						locatorImg = tc.getOtherLocator();
					} else {
						locatorImg = tc.getNeutralLocator();
					}			
					
					drawStepList.add(new DrawEntityStep(posX, posZ, heading, false, locatorImg, 8));
					
					// Draw entity image
					if(isPlayer) {
						entityIcon = tc.getPlayerSkin((String) critter.get(EntityKey.username));
					} else {
						entityIcon = tc.getEntityImage(filename);
					}
					if(entityIcon!=null) {
						int bottomMargin = isPlayer ? 0 : 8;
						drawStepList.add(new DrawEntityStep(posX, posZ, heading, true, entityIcon, bottomMargin));
					}
					
					if(isPlayer) {
						// Draw Label			
						String username = (String) critter.get(EntityKey.username);
						drawStepList.add(new DrawCenteredLabelStep(posX, posZ, username, -entityIcon.height, labelBg, Color.green, fontScale));
					} else if(critter.containsKey(EntityKey.customName)){
						String customName = (String) critter.get(EntityKey.customName);
						drawStepList.add(new DrawCenteredLabelStep(posX, posZ, customName, entityIcon.height/2, labelBg, Color.white, fontScale));
					}
				}
			}
		} catch(Throwable t) {
			JourneyMap.getLogger().severe("Error during prepareSteps: " + LogFormatter.toString(t));
		}
		
		return drawStepList;
	}
	
}
