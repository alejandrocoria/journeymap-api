package net.techbrew.journeymap.render.overlay;

import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.render.draw.DrawStep;
import net.techbrew.journeymap.render.draw.DrawWayPointStep;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders waypoints in the MapOverlay.
 * 
 * @author mwoodman
 *
 */
public class OverlayWaypointRenderer {

	final BasicStroke thinStroke = new BasicStroke(2);
	final BasicStroke mediumStroke = new BasicStroke(4);
	final BasicStroke thinRoundStroke = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.CAP_ROUND);
	final BasicStroke thickRoundStroke = new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.CAP_ROUND);

	public List<DrawStep> prepareSteps(List<Waypoint> waypoints, GridRenderer grid, double fontScale) {

		final List<DrawStep> drawStepList = new ArrayList<DrawStep>();
		
		try {		
			int wx, wz;
			
			Color color;
			Color labelColor;
			boolean inbounds;			
			TextureCache tc = TextureCache.instance();
            final int dimension = Minecraft.getMinecraft().thePlayer.dimension;
            final int netherOffset = (dimension==-1) ? 8 : 1;

			for(Waypoint waypoint : waypoints) {
				wx = waypoint.getX() * netherOffset;
				wz = waypoint.getZ() * netherOffset;
				color = waypoint.getColor();

                // Draw marker
                TextureImpl texture = null;
                if(waypoint.getType()==Waypoint.Type.Death) { // death spot
                    texture = tc.getDeathpoint();
                } else {
                    texture = tc.getWaypoint();
                }
                labelColor = (waypoint.getType()==Waypoint.Type.Death) ? Color.red : color;
                drawStepList.add(new DrawWayPointStep(wx, wz, texture, tc.getWaypointOffscreen(), waypoint.getName(), color, labelColor, 220, fontScale));
			}
		} catch(Throwable t) {
			JourneyMap.getLogger().severe("Error during prepareSteps: " + LogFormatter.toString(t));
		}
		
		return drawStepList;
	}
	

}
