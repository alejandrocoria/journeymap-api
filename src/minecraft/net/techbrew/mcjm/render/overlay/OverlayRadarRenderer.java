package net.techbrew.mcjm.render.overlay;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.src.Minecraft;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.data.EntityKey;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.EntityHelper;

/**
 * Renders an entity image in the MapOverlay.
 * 
 * @author mwoodman
 *
 */
public class OverlayRadarRenderer extends BaseOverlayRenderer<Map> {
	
	final int fontHeight = 14;
	final Color labelBg = Color.darkGray.darker();
	
	boolean showAnimals;
	boolean showPets;

	/**
	 * Constructor.
	 * @param startCoords
	 * @param entityChunkSize
	 * @param canvasWidth
	 * @param canvasHeight
	 */
	public OverlayRadarRenderer(final boolean showAnimals, final boolean showPets) {
		super();
		this.showAnimals = showAnimals;
		this.showPets = showPets;
	}
	
	public void setShowAnimals(boolean showAnimals) {
		this.showAnimals = showAnimals;
	}

	public void setShowPets(boolean showPets) {
		this.showPets = showPets;
	}

	@Override
	public List<DrawStep> prepareSteps(List<Map> critters, CoreRenderer core) {
		
		final List<DrawStep> drawStepList = new ArrayList<DrawStep>();
		
		try {
			
			double heading;
			MapTexture entityIcon, locatorImg;
			String filename, owner;
			Boolean isHostile, isPet, isPlayer;
			boolean filterAnimals = (showAnimals!=showPets);
			//FontMetrics fm = g2D.getFontMetrics();
			String playername = Minecraft.getMinecraft().thePlayer.getEntityName();
			
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
				
				int posX = (Integer) critter.get(EntityKey.posX);
				int posZ = (Integer) critter.get(EntityKey.posZ);
				Point pixel = core.getPixel(posX, posZ);
				if(pixel!=null) {						
					filename = (String) critter.get(EntityKey.filename);
					heading = (Double) critter.get(EntityKey.heading);					
					isPlayer = EntityHelper.PLAYER_FILENAME.equals(filename);

					// Determine and draw locator
					if(isHostile) {
						locatorImg = EntityHelper.getHostileLocator();
					} else if(isPet) {
						locatorImg = EntityHelper.getPetLocator();
					} else if(isPlayer) {
						locatorImg = EntityHelper.getOtherLocator();
					} else {
						locatorImg = EntityHelper.getNeutralLocator();
					}			
					
					drawStepList.add(new DrawEntityStep(pixel, heading, false, locatorImg));
					
					// Draw entity image
					entityIcon = EntityHelper.getEntityImage(filename);
					if(entityIcon!=null) {
						drawStepList.add(new DrawEntityStep(pixel, heading, true, entityIcon));
					}
					
					if(isPlayer) {
						// Draw Label			
						String username = (String) critter.get(EntityKey.username);
						drawStepList.add(new DrawCenteredLabelStep(pixel, username, fontHeight, -entityIcon.height, labelBg, Color.green));
					} else if(critter.containsKey(EntityKey.customName)){
						String customName = (String) critter.get(EntityKey.customName);
						drawStepList.add(new DrawCenteredLabelStep(pixel, customName, fontHeight, entityIcon.height, labelBg, Color.white));
					}
				}
			}
		} catch(Throwable t) {
			JourneyMap.getLogger().severe("Error during prepareSteps: " + LogFormatter.toString(t));
		}
		
		return drawStepList;
	}

	@Override
	public void clear() {
	}
	
}
