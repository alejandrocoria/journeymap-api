package net.techbrew.mcjm.render.overlay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Entity;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.Waypoint;
import net.techbrew.mcjm.render.overlay.BaseOverlayRenderer.BaseEntityOverlayRenderer;

/**
 * Renders waypoints in the MapOverlay.
 * 
 * @author mwoodman
 *
 */
public class OverlayWaypointRenderer extends BaseEntityOverlayRenderer<List<Waypoint>> {
	
	final int fontHeight = 16;
	final Font labelFont = new Font("Arial", Font.BOLD, fontHeight);
	final BasicStroke thinStroke = new BasicStroke(2);
	final BasicStroke mediumStroke = new BasicStroke(4);
	final BasicStroke thinRoundStroke = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.CAP_ROUND);
	final BasicStroke thickRoundStroke = new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.CAP_ROUND);
	
	/**
	 * Constructor.
	 * @param startCoords
	 * @param entityChunkSize
	 * @param canvasWidth
	 * @param canvasHeight
	 */
	public OverlayWaypointRenderer(OverlayEntityRenderer parentRenderer, int layerWidth, int layerHeight, int widthCutoff, int heightCutoff) {
		super(parentRenderer.startCoords, parentRenderer.endCoords, 
			  parentRenderer.canvasWidth, parentRenderer.canvasHeight, layerWidth, layerHeight, widthCutoff, heightCutoff);
		this.blockSize = parentRenderer.blockSize;
	}

	/**
	 * Render waypoints.
	 */
	@Override
	public void render(List<Waypoint> waypoints, Graphics2D g2D) {

		try {		
			int wx, wz, x, z;
			
			Color color;
			Color labelColor;
			boolean inbounds;					
			final int diameter = blockSize>16 ? new Double(blockSize).intValue() : 16;
			g2D.setFont(labelFont); //$NON-NLS-1$
			final FontMetrics fm = g2D.getFontMetrics();
			
			for(Waypoint waypoint : waypoints) {
				wx = waypoint.getX();
				wz = waypoint.getZ();
				color = waypoint.getColor();
				
				x = new Double(getScaledEntityX(wx)).intValue();
				z = new Double(getScaledEntityZ(wz)).intValue();
				
				if(waypoint.getName().equals("Last")) {
					
					System.out.println("Scaled EndCoord: " + getScaledEntityX(endCoords.chunkXPos, 0) + ", " + getScaledEntityZ(endCoords.chunkZPos, 0));
					
				}
				
				int maxX = new Double(getScaledEntityX(endCoords.chunkXPos-1, 0)).intValue() - diameter;
				int maxZ = new Double(getScaledEntityZ(endCoords.chunkZPos-1, 0)).intValue() - diameter;
				
				inbounds = x>diameter && x<maxX &&
						   z>diameter && z<maxZ;
				
				// Draw waypoint using copy of G2D			
				final Graphics2D g = (Graphics2D) g2D.create();
				
				if(inbounds) {
					
					System.out.println("Inbound Waypoint: " + x + ", " + z);	
					
					// Draw marker
					if(waypoint.getType()==Waypoint.TYPE_DEATH) { // death spot
						
						g.setComposite(OPAQUE);
						
						// Black X						
						g.setStroke(thickRoundStroke);
						g.setPaint(Color.black);
						g.drawLine(x-diameter, z-diameter, x+diameter, z+diameter);
						g.drawLine(x+diameter, z-diameter, x-diameter, z+diameter);
	
						// Colored X
						g.setStroke(thinRoundStroke);
						g.setPaint(color);					
						g.drawLine(x-diameter, z-diameter, x+diameter, z+diameter);
						g.drawLine(x+diameter, z-diameter, x-diameter, z+diameter);
						
					} else {
						
						// Diamond
						int[] xcoords = new int[]{x-diameter, x, x+diameter, x, x-diameter};
						int[] zcoords = new int[]{z, z-diameter, z, z+diameter, z};
						int clen = 5;
						
						g.setComposite(OPAQUE);
						g.setStroke(thinStroke);
						g.setPaint(color);
						g.fillPolygon(xcoords, zcoords, clen);
						g.setPaint(Color.black);
						g.drawPolygon(xcoords, zcoords, clen);
						
						// Cross on Diamond
						g.setComposite(SLIGHTLYOPAQUE);
						g.setStroke(thinStroke);
						g.setPaint(Color.white);
						g.drawLine(x-diameter, z, x+diameter, z);
						g.drawLine(x, z-diameter, x, z+diameter);
					}
					
					// Draw label
					labelColor = (waypoint.getType()==Waypoint.TYPE_DEATH) ? Color.red : color;
					drawCenteredLabel(waypoint.getName(), x, z, fontHeight, -diameter*2, g, fm, Color.black, labelColor);
	
				} else {
					
					if(x<0) x = 0;
					if(z<0) z = 0;
					
					
					
					if(x>maxX) x = maxX;
					if(z>maxZ) z = maxZ;
					
					System.out.println("Clamped Waypoint: " + x + ", " + z);	
					
					// Edge marker (semicircle)
					g.setComposite(OPAQUE);
					g.setStroke(thickRoundStroke);
					g.setPaint(Color.black);
					g.drawArc(x-diameter/2, z-diameter/2, diameter*2, diameter*2, 0, 360);
					g.setPaint(color);
					g.fillArc(x-diameter/2, z-diameter/2, diameter*2, diameter*2, 0, 360);
				}
				
				g.dispose();
			}
		} catch(Throwable t) {
			JourneyMap.getLogger().severe("Error during render: " + LogFormatter.toString(t));
		}
	}
	
	public boolean inBounds(Waypoint waypoint) {
		int chunkX = (int) waypoint.getX()>>4;
		int chunkZ = (int) waypoint.getZ()>>4;
		return (chunkX>=startCoords.chunkXPos && chunkX<=endCoords.chunkXPos && 
				chunkZ>=startCoords.chunkZPos && chunkZ<=endCoords.chunkZPos);
	}

}
