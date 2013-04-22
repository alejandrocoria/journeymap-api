package net.techbrew.mcjm.render.overlay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.List;

import net.minecraft.src.ChunkCoordIntPair;
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
	public OverlayWaypointRenderer(OverlayEntityRenderer parentRenderer, int widthCutoff, int heightCutoff) {
		super(parentRenderer.startCoords, parentRenderer.endCoords, 
			  parentRenderer.canvasWidth, parentRenderer.canvasHeight, widthCutoff, heightCutoff);
		this.blockSize = parentRenderer.blockSize;
	}

	/**
	 * Render waypoints.
	 */
	@Override
	public void render(List<Waypoint> waypoints, Graphics2D g2D) {

		try {
			int x, z, wx, wz;
			Color color;
			Color labelColor;
			boolean outofbounds;					
			final int diameter = (int) Math.max(6, Math.round(super.blockSize));
			g2D.setFont(labelFont); //$NON-NLS-1$
			final FontMetrics fm = g2D.getFontMetrics();
			
			int xOob = canvasWidth-(widthCutoff)-diameter;
			int zOob = canvasHeight-(heightCutoff)-diameter;
			
			for(Waypoint waypoint : waypoints) {
				wx = waypoint.getX();
				wz = waypoint.getZ();
				color = waypoint.getColor();
				
				x = (int) Math.floor(getScaledEntityX(wx));
				z = (int) Math.floor(getScaledEntityZ(wz));
				
				outofbounds = false;
	
				if(x<0) {
					x = -diameter;
					outofbounds = true;
				} else if(x > xOob) {
					x = xOob;
					outofbounds = true;
				}
				
				if(z<0) {
					z = -diameter;
					outofbounds = true;
				} else if(z > zOob) {
					z = zOob;
					outofbounds = true;
				}
							
				// Draw waypoint using copy of G2D			
				final Graphics2D g = (Graphics2D) g2D.create();
				
				if(!outofbounds) {
					
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
					
					// Edge marker (semicircle)
					g.setComposite(OPAQUE);
					g.setStroke(thickRoundStroke);
					g.setPaint(Color.black);
					g.drawArc(x, z, diameter*2, diameter*2, 0, 360);
					g.setPaint(color);
					g.fillArc(x, z, diameter*2, diameter*2, 0, 360);
				}
				
				g.dispose();
			}
		} catch(Throwable t) {
			JourneyMap.getLogger().severe("Error during render: " + LogFormatter.toString(t));
		}
	}

}
