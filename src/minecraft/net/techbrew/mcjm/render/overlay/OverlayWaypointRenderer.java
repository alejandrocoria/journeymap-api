package net.techbrew.mcjm.render.overlay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.Waypoint;

/**
 * Renders waypoints in the MapOverlay.
 * 
 * @author mwoodman
 *
 */
public class OverlayWaypointRenderer extends BaseOverlayRenderer<Waypoint> {
	
	final int fontHeight = 16;
	final Font labelFont = new Font("Arial", Font.BOLD, fontHeight);
	final BasicStroke thinStroke = new BasicStroke(2);
	final BasicStroke mediumStroke = new BasicStroke(4);
	final BasicStroke thinRoundStroke = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.CAP_ROUND);
	final BasicStroke thickRoundStroke = new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.CAP_ROUND);
	
	static MapTexture waypoint;
	static MapTexture deathpoint;
	static MapTexture offscreen;
	
	@Override
	public List<DrawStep> prepareSteps(List<Waypoint> waypoints, CoreRenderer core) {

		final List<DrawStep> drawStepList = new ArrayList<DrawStep>();
		
		try {		
			int wx, wz;
			
			Color color;
			Color labelColor;
			boolean inbounds;					

			for(Waypoint waypoint : waypoints) {
				wx = waypoint.getX();
				wz = waypoint.getZ();
				color = waypoint.getColor();							
				
				Point pixel = core.getBlockPixelInGrid(wx, wz);
				
				if(core.isOnScreen(pixel.x, pixel.y)) {

					// Draw marker
					MapTexture texture = null;
					if(waypoint.getType()==Waypoint.TYPE_DEATH) { // death spot
						texture = getDeathpointTexture();
					} else {						
						texture = getWaypointTexture();
					}
					drawStepList.add(new DrawColoredImageStep(pixel, texture, color, 255));
					
					// Draw label
					labelColor = (waypoint.getType()==Waypoint.TYPE_DEATH) ? Color.red : color;
					drawStepList.add(new DrawCenteredLabelStep(pixel, waypoint.getName(), fontHeight, -texture.height, Color.black, labelColor));
	
				} else {
					
					// Draw offscreen marker
					pixel = core.getClosestOnscreenPixel(wx, wz);
					drawStepList.add(new DrawColoredImageStep(pixel, getOffscreenTexture(), color, 255));
				}
				
			}
		} catch(Throwable t) {
			JourneyMap.getLogger().severe("Error during prepareSteps: " + LogFormatter.toString(t));
		}
		
		return drawStepList;
	}
	

	private MapTexture getWaypointTexture() {
		if(waypoint==null) {
			BufferedImage img = FileHandler.getWebImage("waypoint.png");	//$NON-NLS-1$ //$NON-NLS-2$		
			waypoint = new MapTexture(img);
		}
		return waypoint;
	}
	
	private MapTexture getDeathpointTexture() {
		if(deathpoint==null) {
			BufferedImage img = FileHandler.getWebImage("waypoint-death.png");	//$NON-NLS-1$ //$NON-NLS-2$		
			deathpoint = new MapTexture(img);
		}
		return deathpoint;
	}

	private MapTexture getOffscreenTexture() {
		if(offscreen==null) {
			BufferedImage img = FileHandler.getWebImage("waypoint-offscreen.png");	//$NON-NLS-1$ //$NON-NLS-2$		
			offscreen = new MapTexture(img);
		}
		return offscreen;
	}

	@Override
	public void clear() {
		if(waypoint!=null) waypoint.clear();
		if(deathpoint!=null) deathpoint.clear();
		if(offscreen!=null) offscreen.clear();
	}

}
