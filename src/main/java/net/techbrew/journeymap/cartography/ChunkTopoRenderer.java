package net.techbrew.journeymap.cartography;

import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.model.BlockMD;
import net.techbrew.journeymap.model.BlockUtils;
import net.techbrew.journeymap.model.ChunkMD;
import net.techbrew.journeymap.model.RGB;

import java.awt.*;
import java.util.ArrayList;
import java.util.Stack;

public class ChunkTopoRenderer extends ChunkStandardRenderer implements IChunkRenderer {

	static final int alphaDepth = 5;
    ArrayList<RGB> water = new ArrayList<RGB>(32);
    ArrayList<RGB> land = new ArrayList<RGB>(32);

    // http://soliton.vm.bytemark.co.uk/pub/cpt-city/views/topo.html

    public ChunkTopoRenderer()
    {

        water.add(new RGB(new Color(31,40,79)));
        water.add(new RGB(new Color(38,60,106)));
        water.add(new RGB(new Color(46,80,133)));
        water.add(new RGB(new Color(53,99,160)));
        water.add(new RGB(new Color(60,119,188)));
        water.add(new RGB(new Color(72,151,211)));
        water.add(new RGB(new Color(90,185,233)));
        water.add(new RGB(new Color(95,198,242)));
        water.add(new RGB(new Color(114,202,238)));
        water.add(new RGB(new Color(141,210,239)));


        land.add(new RGB(new Color(113,171,216)));
        land.add(new RGB(new Color(121,178,222)));
        land.add(new RGB(new Color(132,185,227)));
        land.add(new RGB(new Color(141,193,234)));
        land.add(new RGB(new Color(150,201,240)));
        land.add(new RGB(new Color(161,210,247)));
        land.add(new RGB(new Color(172,219,251)));
        land.add(new RGB(new Color(185,227,255)));
        land.add(new RGB(new Color(198,236,255)));
        land.add(new RGB(new Color(216,242,254)));
        land.add(new RGB(new Color(172,208,165)));
        land.add(new RGB(new Color(148,191,139)));
        land.add(new RGB(new Color(168,198,143)));
        land.add(new RGB(new Color(189,204,150)));
        land.add(new RGB(new Color(209,215,171)));
        land.add(new RGB(new Color(225,228,181)));
        land.add(new RGB(new Color(239,235,192)));
        land.add(new RGB(new Color(232,225,182)));
        land.add(new RGB(new Color(222,214,163)));
        land.add(new RGB(new Color(211,202,157)));
        land.add(new RGB(new Color(202,185,130)));
        land.add(new RGB(new Color(195,167,107)));
        land.add(new RGB(new Color(185,152,90)));
        land.add(new RGB(new Color(170,135,83)));
        land.add(new RGB(new Color(172,154,124)));
        land.add(new RGB(new Color(186,174,154)));
        land.add(new RGB(new Color(202,195,184)));
        land.add(new RGB(new Color(224,222,216)));
        land.add(new RGB(new Color(245,244,242)));
        land.add(new RGB(new Color(255,255,255)));
    }

	/**
	 * Render blocks in the chunk for the standard world.
	 */
	@Override
	public boolean render(final Graphics2D g2D, final ChunkMD chunkMd, final boolean underground, 
			final Integer vSlice, final ChunkMD.Set neighbors) {

        if(underground) return false;

        // Initialize ChunkSub slopes if needed
        if(chunkMd.surfaceSlopes==null) {
            initSurfaceSlopes(chunkMd, neighbors);
        }

        return renderSurface(g2D, chunkMd, vSlice, neighbors, false);
	}

    /**
     * Initialize surface slopes in chunk if needed.
     * @param chunkMd
     * @param neighbors
     */
    @Override
    protected void initSurfaceSlopes(final ChunkMD chunkMd, final ChunkMD.Set neighbors) {
        StatTimer timer = StatTimer.get("ChunkStandardRenderer.initSurfaceSlopes");
        timer.start();
        float slope, h, hN, hW, hE, hS;
        chunkMd.surfaceSlopes = new float[16][16];
        for(int y=0; y<16; y++)
        {
            for(int x=0; x<16; x++)
            {
                h = chunkMd.getSlopeHeightValue(x, y);
                hN = (y==0)  ? getBlockHeight(x, y, 0, -1, chunkMd, neighbors, h) : chunkMd.getSlopeHeightValue(x, y - 1);
                hW = (x==0)  ? getBlockHeight(x, y, -1, 0, chunkMd, neighbors, h) : chunkMd.getSlopeHeightValue(x - 1, y);
                hS = (y==15)  ? getBlockHeight(x, y, 0, 1, chunkMd, neighbors, h) : chunkMd.getSlopeHeightValue(x, y + 1);
                hE = (x==15)  ? getBlockHeight(x, y, 1, 0, chunkMd, neighbors, h) : chunkMd.getSlopeHeightValue(x + 1, y);

                h = (int)h>>3;
                hN = (int)hN>>3;
                hW = (int)hW>>3;
                hE = (int)hE>>3;
                hS = (int)hS>>3;

                slope = ((h/hN)+(h/hW)+(h/hE)+(h/hS))/4f;
                chunkMd.surfaceSlopes[x][y] = slope;
            }
        }
        timer.stop();
    }

    /**
     * Get the color for a block based on its location, neighbor slopes.
     */
    protected RGB getBaseBlockColor(final ChunkMD chunkMd, final BlockMD blockMD, final ChunkMD.Set neighbors, int x, int y, int z)
    {
        float orthoY = y>>3;
        if(blockMD.isWater())
        {
            float saturation = orthoY==0 ? 0 : (orthoY / 32f);
            return new RGB(saturation,saturation, 1);
        }
        else
        {
            int index = 0;
            if(orthoY>0 && y<=63)
            {
                // At or below sea level: Use values 1-9 in land
                index = (int) Math.floor(orthoY/(15f/9)); // 15 orthos in range across 10 values
            }
            else
            {
                // Above sea level: Use last 20 values in land
                index = 2 + (int)orthoY;
            }

            //index = (int) Math.floor((orthoY/20)*(land.size()-1));
            index = Math.min(index, land.size()-1);

            return land.get(index).copy();
        }
    }

    protected RGB renderSurfaceAlpha(final Graphics2D g2D, final ChunkMD chunkMd, final BlockMD blockMD, final ChunkMD.Set neighbors, int x, int y, int z) {

        RGB color = getBaseBlockColor(chunkMd, blockMD, neighbors, x, y, z);

        // Paint depth layers
        paintDepth(chunkMd, blockMD, x, y, z, g2D, false);

        return color;
    }

    protected void surfaceSlopeColor(final RGB color, final ChunkMD chunkMd, final BlockMD blockMD, final ChunkMD.Set neighbors, int x, int ignored, int z)
    {
        float slope = chunkMd.surfaceSlopes[x][z];
        if(slope<1)
        {
            color.setFrom(Color.lightGray.getRGB());
        }
        if(slope>1)
        {
            color.setFrom(Color.darkGray.getRGB());
        }
    }

    protected void paintDepth(ChunkMD chunkMd, BlockMD blockMD, int x, int y, int z, final Graphics2D g2D, final boolean useLighting) {

        // See how deep the alpha goes

        Stack<BlockMD> stack = new Stack<BlockMD>();
        stack.push(blockMD);
        int maxDepth = 256;
        int down = y;
        while(down>0) {
            down--;
            BlockMD lowerBlock = BlockMD.getBlockMD(chunkMd, x, down, z);
            if(lowerBlock!=null) {
                stack.push(lowerBlock);

                if (lowerBlock.getAlpha()==1f || y-down>maxDepth) {
                    break;
                }

            } else {
                break;
            }
        }

        RGB color = getBaseBlockColor(chunkMd, blockMD, null, x, down, z);

        g2D.setComposite(BlockUtils.OPAQUE);
        g2D.setPaint(color.toColor());
        g2D.fillRect(x, z, 1, 1);
    }
}
