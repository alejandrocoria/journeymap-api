package net.techbrew.mcjm.render.overlay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.src.ChunkCoordIntPair;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.data.AnimalsData;
import net.techbrew.mcjm.data.DataCache;
import net.techbrew.mcjm.data.EntityKey;
import net.techbrew.mcjm.data.MobsData;
import net.techbrew.mcjm.data.PlayersData;
import net.techbrew.mcjm.data.VillagersData;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.EntityHelper;
import net.techbrew.mcjm.model.Waypoint;
import net.techbrew.mcjm.render.MapBlocks;

/**
 * Renders an entity image in the MapOverlay.
 * 
 * @author mwoodman
 *
 */
public class OverlayEntityRenderer extends BaseOverlayRenderer<List<Map>> {
	
	final int fontHeight = 8;
	final Font labelFont = new Font("Arial", Font.BOLD, fontHeight);
	final Color labelBg = Color.darkGray.darker();
	
	final boolean showAnimals;
	final boolean showPets;
	
	/**
	 * Constructor.
	 * @param startCoords
	 * @param entityChunkSize
	 * @param canvasWidth
	 * @param canvasHeight
	 */
	public OverlayEntityRenderer(final ChunkCoordIntPair startCoords, final ChunkCoordIntPair endCoords, final int entityChunkSize, final int canvasWidth, final int canvasHeight, final int widthCutoff, final int heightCutoff, final boolean showAnimals, final boolean showPets) {
		super(startCoords, endCoords, entityChunkSize, canvasWidth, canvasHeight, widthCutoff, heightCutoff);
		this.showAnimals = showAnimals;
		this.showPets = showPets;
	}

	/**
	 * Render list of entities.
	 */
	@Override
	public void render(List<Map> critters, Graphics2D g2D) {

		try {
			
			int cx, cz, x, z;
			double heading;
			BufferedImage entityIcon, locatorImg;
			String filename, owner;
			Boolean isHostile, isPet, isPlayer;
			boolean filterAnimals = (showAnimals!=showPets);
			FontMetrics fm = g2D.getFontMetrics();
			String playername = Minecraft.getMinecraft().thePlayer.username;
			
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
				
				if(inBounds(critter)) {						
					filename = (String) critter.get(EntityKey.filename);
					cx = (Integer) critter.get(EntityKey.chunkCoordX);
					cz = (Integer) critter.get(EntityKey.chunkCoordZ);
					x = (Integer) critter.get(EntityKey.posX);
					z = (Integer) critter.get(EntityKey.posZ);
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
					g2D.setComposite(MapBlocks.OPAQUE);
					drawEntity(cx, x, cz, z, heading, false, locatorImg, g2D);
					
					// Draw entity image
					entityIcon = EntityHelper.getEntityImage(filename);
					if(entityIcon!=null) {
						g2D.setComposite(MapBlocks.OPAQUE);
						drawEntity(cx, x, cz, z, heading, true, entityIcon, g2D);
					}
					
					int lx = getScaledEntityX(cx, x);
					int lz = getScaledEntityZ(cz, z);
					
					g2D.setComposite(MapBlocks.SLIGHTLYCLEAR);
					if(isPlayer) {
						// Draw Label			
						String username = (String) critter.get(EntityKey.username);
						drawCenteredLabel(username, lx, lz, fontHeight*2, 32, g2D, fm, labelBg, Color.green);
					} else if(critter.containsKey(EntityKey.customName)){
						String customName = (String) critter.get(EntityKey.customName);
						drawCenteredLabel(customName, lx, lz, fontHeight*2, entityIcon.getWidth()-8, g2D, fm, labelBg, Color.white);
					}
				}
			}
		} catch(Throwable t) {
			JourneyMap.getLogger().severe("Error during render: " + LogFormatter.toString(t));
		}
	}

	@Override
	public void drawEntity(int chunkX, double posX, int chunkZ, double posZ,
			Double heading, boolean flipNotRotate, BufferedImage entityIcon,
			Graphics2D g2d) {
		
		super.drawEntity(chunkX, posX, chunkZ, posZ, heading, flipNotRotate,
				entityIcon, g2d);
	}
	
	

}
