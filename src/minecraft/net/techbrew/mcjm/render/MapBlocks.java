package net.techbrew.mcjm.render;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import net.minecraft.src.BiomeGenBase;
import net.minecraft.src.Block;
import net.minecraft.src.BlockFluid;
import net.techbrew.mcjm.ChunkStub;
import net.techbrew.mcjm.ExtendedBlockStorageStub;
import net.techbrew.mcjm.JourneyMap;

public class MapBlocks extends HashMap {
	
	public static AlphaComposite OPAQUE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1F);
	public static AlphaComposite CLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0F);
	public static AlphaComposite SEMICLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F);
	public static Color COLOR_TRANSPARENT = new Color(0,0,0,0);
	
	final HashMap<BiomeGenBase, Color> grassBiomeColors = new HashMap<BiomeGenBase, Color>(16);
	final HashMap<BiomeGenBase, Color> waterBiomeColors = new HashMap<BiomeGenBase, Color>(16);
	final HashMap<String, Color> foliageBiomeColors = new HashMap<String, Color>(16);
	
	/**
	 * Constructor
	 */
	public MapBlocks() {
		JourneyMap.getLogger().info("MapBlocks instantiated"); //$NON-NLS-1$ 
	}
	
	/**
	 * Gets the color for grass in the biome.  Lazy-loads
	 * the result into a map for faster access.
	 * 
	 * @param biome
	 * @return
	 */
	public Color getGrassColor(BiomeGenBase biome) {
		
		Color color = grassBiomeColors.get(biome);
		if(color==null) {
			color = new Color(biome.getBiomeGrassColor()).darker();
			grassBiomeColors.put(biome, color);
		}
		return color;
	}
	
	/**
	 * Gets the color for foliage in the biome.  Lazy-loads
	 * the result into a map for faster access.
	 * 
	 * @param biome
	 * @return
	 */
	public Color getFoliageColor(BiomeGenBase biome, int[] blockInfo) {
		Color color = foliageBiomeColors.get(biome.biomeName + blockInfo[1]);
		if(color==null) {
			color = average(new Color(biome.getBiomeFoliageColor()), getColor(blockInfo));
			foliageBiomeColors.put(biome.biomeName + blockInfo[1], color.darker());
		}
		return color;
	}

	/**
	 * Gets the color for water in the biome.  Lazy-loads
	 * the result into a map for faster access.
	 * 
	 * @param biome
	 * @return
	 */
	public Color getWaterColor(BiomeGenBase biome) {		
		Color color = waterBiomeColors.get(biome);
		if(color==null) {
			color = blend(Color.blue.darker(), biome.waterColorMultiplier);
			waterBiomeColors.put(biome, color);
		}
		return color;
	}
	
	
	static int[] getBlockInfo(ChunkStub chunkStub, int x, int y, int z) {
		try {
			int blockId = chunkStub.getBlockID(x, y, z);
			int meta = chunkStub.getBlockMetadata(x, y, z);
			return new int[]{blockId, meta};
		} catch (ArrayIndexOutOfBoundsException e) {
			JourneyMap.getLogger().warning("Can't get blockId/meta for chunk " + chunkStub.xPosition + "," + chunkStub.zPosition + " block " + x + "," + y + "," + z); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			return null;
		}
	}
	
	Color getColor(int[] blockInfo) {
		Color color = colors[blockInfo[0]][blockInfo[1]];
		if(color==null && blockInfo[1]!=0) {			
			color = colors[blockInfo[0]][0];
		}
		if(color==null) {
			color = Color.black;
			JourneyMap.getLogger().warning("Can't get color for " + blockInfo[0] + "," + blockInfo[1]);
		}
		return color;
	}
	
	Float getBlockAlpha(int[] blockInfo) {
		return alphas[blockInfo[0]][blockInfo[1]];
	}
	
	/**
	 * Attempt at faster way to figure out if there is sky above
	 * @param chunkStub
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static boolean skyAbove(ChunkStub chunkStub, final int x, final int y, final int z) {
		boolean seeSky = true;
		int blockId;
		
		final int topY = chunkStub.getTopFilledSegment();
		if(y<topY) {
			return false;
		}
		int checkY = y;
		while(seeSky && checkY<chunkStub.worldHeight) {
			blockId = chunkStub.getBlockID(x, checkY, z);
			if(sky.contains(blockId)) {
				checkY++;
			} else {
				seeSky = false;
				break;
			}
		}
		return seeSky;
	}
	
	Color getBlockColor(ChunkStub chunkStub, int[] blockInfo, int x, int y, int z) {
		Color color = null;
		
		BiomeGenBase biome = chunkStub.getBiomeGenForWorldCoords(x, z, chunkStub.worldObj.getWorldChunkManager());
		String biomeName = biome.biomeName;

		switch(blockInfo[0]) {
			case 2 : {
				color = getGrassColor(biome);
				//color = blend(new Color(chunkStub.grassColor), Color.black, .5);
				break;
			}
			case 8 : {
				color = getWaterColor(biome);
				break;
			}
			case 9 : {
				color = getWaterColor(biome);
				break;
			}
			case 18 : {
				//color = getBiomeFoliageColor(biome, chunk, y); // foliage
				color = getFoliageColor(biome, blockInfo);
				break;
			} 
			default : {
				color = getColor(blockInfo);
				break;
			}
		}
		
		return color;
	}
		
	/**
	 * Map of transparent block ids that don't block view of the sky
	 */
	public final static HashSet<Integer> sky = new HashSet<Integer>(2);
	{
		sky.add(0); // air 
		sky.add(8); // water 
		sky.add(9); // stationary water 
		sky.add(18); // leaves
		sky.add(30); // web
		sky.add(65); // ladder
		sky.add(106); // vines
	}
	
	/**
	 * Alpha values for block ids.
	 */
	float[][] alphas = new float[256][16];
	{
		for(int i=0;i<alphas.length;i++) {
			Arrays.fill(alphas[i], 1F);
		}
		alphas[8][0] = .65F; // water
		alphas[9][0] = .65F; // water
		alphas[20][0] = .3F; // glass
		alphas[102][0] = .3F; // glass
		alphas[79][0] = .8F; // ice
	}

	/**
	 * Block colors by id.
	 */
	Color[][] colors = new Color[391][16];
	{
		colors[0][0] = new Color(0x000000); // air
		colors[1][0] = new Color(0x686868); // stone
		colors[2][0] = new Color(0x7fb238); // grass 
		colors[3][0] = new Color(0x79553a); // dirt 
		colors[4][0] = new Color(0x959595); // cobblestone
		colors[5][0] = new Color(0xbc9862); // wooden plank
		colors[6][0] = new Color(0xa2c978); // sapling
		colors[6][1] = new Color(0xa2c978); // redwood sapling 
		colors[6][2] = new Color(0xa2c978); // birch sapling
		colors[7][0] = new Color(0x333333); // bedrock
		colors[8][0] = new Color(0x4040ff); // water 
		colors[9][0] = new Color(0x4040ff); // stationary water 
		colors[10][0] = new Color(0xE25822); // lava
		colors[11][0] = new Color(0xE25822); // stationary lava
		colors[12][0] = new Color(0xddd7a0); // sand
		colors[13][0] = new Color(0x747474); // gravel
		colors[14][0] = new Color(0x747474); // gold ore
		colors[15][0] = new Color(0x747474); // iron ore
		colors[16][0] = new Color(0x747474); // coal ore
		
		colors[17][0] = new Color(0x675132); // oak wood
		colors[17][1] = new Color(0x342919); // spruce wood
		colors[17][2] = new Color(0x455b2e); // birch wood
		colors[17][3] = new Color(0x3A4D27); // jungle wood
		
		colors[17][4] = new Color(0x675132); // oak wood east/west
		colors[17][5] = new Color(0x342919); // spruce wood  east/west
		colors[17][6] = new Color(0x455b2e); // birch wood  east/west
		colors[17][7] = new Color(0x3A4D27); // jungle wood  east/west
		
		colors[17][8] = new Color(0x675132); // oak wood north/south
		colors[17][9] = new Color(0x342919); // spruce wood north/south
		colors[17][10] = new Color(0x455b2e); // birch wood north/south
		colors[17][11] = new Color(0x3A4D27); // jungle wood north/south
		
		colors[17][12] = new Color(0x675132); // oak wood bark only
		colors[17][13] = new Color(0x342919); // spruce wood bark only
		colors[17][14] = new Color(0x455b2e); // birch wood bark only
		colors[17][15] = new Color(0x3A4D27); // jungle wood bark only
		
		colors[18][0] = new Color(0x21530B); // oak leaves 
		colors[18][1] = new Color(0x21530B); // spruce leaves  
		colors[18][2] = new Color(0x3D4F28); // birch leaves
		colors[18][3] = new Color(0x135502); // jungle leaves
		
		colors[19][0] = new Color(0xe5e54e); // sponge
		colors[20][0] = new Color(0xffffff); // glass
		colors[21][0] = new Color(0x6d7484); // lapis lazuli ore
		colors[22][0] = new Color(0x1542b2); // lapis lazuli block
		colors[23][0] = new Color(0x585858); // dispenser
		colors[24][0] = new Color(0xc6bd6d); // sandstone
		colors[25][0] = new Color(0x784f3a); // note block
		colors[26][0] = new Color(0xa95d5d); // bed block
		
		colors[27][0] = new Color(0xa4a4a4); // powered rail
		colors[28][0] = new Color(0xa4a4a4); // detector rail
		colors[29][0] = new Color(0x784f3a); // sticky piston
		colors[30][0] = new Color(0xcccccc); // web
		
		colors[31][0] = new Color(0x648540); // dead shrub
		colors[31][1] = new Color(0x265c0e); // tall grass
		colors[31][2] = new Color(0x265c0e); // fern (living shrub)
		colors[31][3] = new Color(0x265c0e); // tall grass
		
		colors[32][0] = new Color(0x648540); // dead shrub
				
		colors[33][0] = new Color(0x550000); // piston
		colors[34][0] = new Color(0x550000); // piston head
		
		colors[35][0] = new Color(0xdddddd); // white wool
		colors[35][1] = new Color(0xeb8138); // orange wool
		colors[35][2] = new Color(0xc04cca); // magenta wool
		colors[35][3] = new Color(0x8aa3d8); // light blue wool
		colors[35][4] = new Color(0xd3ba27); // yellow wool
		colors[35][5] = new Color(0x38b62d); // light green wool
		colors[35][6] = new Color(0xd8879e); // pink wool
		colors[35][7] = new Color(0x3a3a3a); // gray wool
		colors[35][8] = new Color(0xa6adad); // light gray wool 
		colors[35][9] = new Color(0x246985); // cyan wool
		colors[35][10] = new Color(0x8639cb); // purple wool 
		colors[35][11] = new Color(0x2937a5); // blue wool
		colors[35][12] = new Color(0x51301b); // brown wool
		colors[35][13] = new Color(0x354a18); // dark green wool 
		colors[35][14] = new Color(0x9c2a27); // red wool 
		colors[35][15] = new Color(0x181414); // black wool 
		
		colors[37][0] = new Color(0xf1f902); // dandelion
		colors[38][0] = new Color(0xf7070f); // rose
		colors[39][0] = new Color(0x916d55); // brown mushroom
		colors[40][0] = new Color(0x9a171c); // red mushroom
		
		colors[41][0] = new Color(0xfefb5d); // gold block
		colors[42][0] = new Color(0xe9e9e9); // iron block
		
		colors[43][0] = new Color(0xa8a8a8); // double stone slab
		colors[43][1] = new Color(0xe5ddaf); // double sandstone slab
		colors[43][2] = new Color(0x94794a); // double wooden slab
		colors[43][3] = new Color(0x828282); // Double Cobblestone Slab
		colors[43][4] = new Color(0xaa543b); // Double Brick Slab
		colors[43][5] = new Color(0xa8a8a8); // double stone brick slab
		
		colors[44][0] = new Color(0xa8a8a8); // stone slab
		colors[44][1] = new Color(0xc6bd6d); // sandstone slab
		colors[44][2] = new Color(0x94794a); // wooden slab
		colors[44][3] = new Color(0x828282); // cobblestone slab
		colors[44][4] = new Color(0xaa543b); // brick slab
		colors[44][5] = new Color(0xa8a8a8); // stone brick slab
		colors[44][6] = new Color(0x34191e); // nether brick slab
		colors[44][7] = new Color(0xa8a8a8); // stone slab duplicate
		
		colors[44][8] = new Color(0xa8a8a8); // upsidedown stone slab
		colors[44][9] = new Color(0xc6bd6d); // upsidedown sandstone slab
		colors[44][10] = new Color(0x94794a); // upsidedown wooden slab
		colors[44][11] = new Color(0x828282); // upsidedown cobblestone slab
		colors[44][12] = new Color(0xaa543b); // upsidedown brick slab
		colors[44][13] = new Color(0xa8a8a8); // upsidedown stone brick slab
		colors[44][14] = new Color(0x34191e); // upsidedown nether brick slab
		 
		colors[45][0] = new Color(0xaa543b); // brick
		colors[46][0] = new Color(0xdb441a); // TNT
		colors[47][0] = new Color(0xb4905a); // Bookshelf
		colors[48][0] = new Color(0x1f471f); // Mossy Cobblestone
		colors[49][0] = new Color(0x101018); // Obsidian
		
		colors[50][0] = new Color(0xffd800); // Torch
		colors[51][0] = new Color(0xc05a01); // Fire
		colors[52][0] = new Color(0x265f87); // Monster Spawner
		colors[53][0] = new Color(0xbc9862); // Wooden Stairs
		colors[54][0] = new Color(0x8f691d); // Chest
		colors[55][0] = new Color(0x480000); // Redstone Wire
		colors[56][0] = new Color(0x747474); // Diamond Ore
		colors[57][0] = new Color(0x82e4e0); // Diamond Block
		colors[58][0] = new Color(0xa26b3e); // Workbench
		colors[59][0] = new Color(0xe210); // Wheat Crops
		colors[60][0] = new Color(0x633f24); // Soil
		colors[61][0] = new Color(0x747474); // Furnace
		colors[62][0] = new Color(0x808080); // Burning Furnace
		colors[63][0] = new Color(0xb4905a); // Sign Post
		colors[64][0] = new Color(0x7a5b2b); // Wooden Door Block
		colors[65][0] = new Color(0xac8852); // Ladder
		colors[66][0] = new Color(0xa4a4a4); // Rails
		colors[67][0] = new Color(0x9e9e9e); // Cobblestone Stairs
		colors[68][0] = new Color(0x9f844d); // Wall Sign
		colors[69][0] = new Color(0x695433); // Lever
		colors[70][0] = new Color(0x8f8f8f); // Stone Pressure Plate
		colors[71][0] = new Color(0xc1c1c1); // Iron Door Block
		colors[72][0] = new Color(0xbc9862); // Wooden Pressure Plate
		colors[73][0] = new Color(0x747474); // Redstone Ore
		colors[74][0] = new Color(0x747474); // Glowing Redstone Ore
		colors[75][0] = new Color(0x290000); // Redstone Torch (off)
		colors[76][0] = new Color(0xfd0000); // Redstone Torch (on)
		colors[77][0] = new Color(0x747474); // Stone Button
		colors[78][0] = new Color(0xeeeeee); // Snow
		colors[79][0] = new Color(0x8ebfff); // Ice
		colors[80][0] = new Color(0xfafaff); // Snow Block
		colors[81][0] = new Color(0x11801e); // Cactus
		colors[82][0] = new Color(0xbbbbcc); // Clay
		colors[83][0] = new Color(0x265c0e); // Sugar Cane
		colors[84][0] = new Color(0xaadb74); // Jukebox
		colors[85][0] = new Color(0xbc9862); // Fence
		colors[86][0] = new Color(0xce7b14); // Pumpkin
		colors[87][0] = new Color(0x582218); // Netherrack
		colors[88][0] = new Color(0x996731); // Soul Sand
		colors[89][0] = new Color(0xcda838); // Glowstone
		colors[90][0] = new Color(0x643993); // Nether Portal
		colors[91][0] = new Color(0xe08e1d); // Jack-O-Lantern
		colors[92][0] = new Color(0xe7e7e9); // Cake
		
		colors[93][0] = new Color(0x9e9e9e); // Redstone Repeater Block (off)
		colors[94][0] = new Color(0x9e9e9e); // Redstone Repeater Block (on)
			
		colors[95][0] = new Color(0x8f691d);	// 95		Locked Chest
		colors[96][0] = new Color(0xbc9855);	// 96		Trapdoor
		colors[97][0] = new Color(0x8f8f8f);	// 97		Stone (Silverfish)
		colors[97][1] = new Color(0x828282);	// 97][1		Cobblestone (Silverfish)
		colors[97][2] = new Color(0xa8a8a8);	// 97][2		Stone Brick (Silverfish)
		colors[98][0] = new Color(0x8f8f8f); // Stone Brick
		colors[98][1] = new Color(0x1f471f); // Mossy Stone Brick
		colors[98][2] = new Color(0x8f8f8f); // Cracked Stone Brick
		colors[99][0] = new Color(0x8F0000); // 99		Red Mushroom Cap
		colors[100][0] = new Color(0xc4a476); // 100		Brown Mushroom Cap  
		colors[101][0] = new Color(0xe9e9e9); // Iron Bars
		colors[102][0] = new Color(0xffffff); // Glass Pane
		colors[103][0] = new Color(0xbcb628); // 103		Melon Block
		colors[104][0] = new Color(0x74b422); // 104		Pumpkin Stem 
		colors[105][0] = new Color(0x74b422);  // 105		Melon Stem
		colors[106][0] = new Color(0x74b422); // Vines
		colors[107][0] = new Color(0xbc9852); // Fence Gate
		colors[108][0] = new Color(0xaa543b); // Brick Stairs
		colors[109][0] = new Color(0x8f8f8f); // Stone Brick Stairs
		colors[110][0] = new Color(0x6E5F6E); // 110		Mycelium
		colors[111][0] = new Color(0x0d5f15); // 111		Lily Pad 0d5f15
		colors[112][0] = new Color(0x34191e);	// 112		Nether Brick
		colors[113][0] = new Color(0x34191e);	// 113		Nether Brick Fence
		colors[114][0] = new Color(0x34191e);	// 114		Nether Brick Stairs
		colors[115][0] = new Color(0x891c31);	// Nether Wart		
		colors[116][0] = new Color(0x550000);	// Enchanting Table
		colors[117][0] = new Color(0xbbc185);	// Brewing Stand		
		colors[118][0] = new Color(0x424242);   // Cauldron
		colors[119][0] = new Color(0x0b0b0b);   // End Portal		
		colors[120][0] = new Color(0x4986bc);	// End Portal Frame
		colors[121][0] = new Color(0xc6bd6d);	// End Stone		
		colors[122][0] = new Color(0x050507);	// Dragon Egg
		colors[123][0] = new Color(0x785b3b);	// Redstone Lamp (off)
		colors[124][0] = new Color(0xab8a55);	// Redstone Lamp (on)		
		colors[125][0] = new Color(0x94794a); // double wooden slab
		colors[126][0] = new Color(0x94794a); // double wooden slab
		
		// TODO: Wood slab colors
		colors[127][0] = new Color(0xcd8e4b); // cocoa plant
		colors[128][0] = new Color(0xc6bd6d);   // 128 Sandstone Stairs
		colors[129][0] = new Color(0x37b957);   // 129 Emerald Ore
		// 130 Ender Chest
		// 131 Tripwire Hook
		// 132 Tripwire
		colors[133][0] = new Color(0x37b957);   // 133 Emerald Block
		
		colors[137][0] = new Color(0xc39b81);   // 137 Command Block
		colors[138][0] = new Color(0xbff9fd);   // 138 Beacon Block
		
		colors[139][0] = new Color(0x959595); // cobblestone wall
		colors[139][1] = new Color(0x1f471f);  // Mossy Cobblestone wall
		
		colors[140][0] = new Color(0x7d4536);   // 390 flower pot
		
		colors[141][0] = new Color(0x0ac200);  // Carrot crop
		colors[142][0] = new Color(0x00e01a);  // Potato crop
		
		colors[145][0] = new Color(0x424242);   // 145 Anvil
		colors[200][0] = new Color(0xd270f7);	// 200 Ender Crystal
		
		colors[390][0] = new Color(0x7d4536);   // 390 flower pot
		
	}
	
	
	
	static Color blend(Color color1, int multiplier)
	  {
		int r1 = color1.getRed();
        int g1 = color1.getGreen();
        int b1 = color1.getBlue();

        int r2 = 255-((multiplier & 16711680) >> 16);
        int g2 = 255-((multiplier & 65280) >> 8);
        int b2 = 255-((multiplier & 255));
        
        int r3 = Math.max(0, r1-r2);
        int g3 = Math.max(0, g1-g2);
        int b3 = Math.max(0, b1-b2);

	    return new Color(r3,g3,b3);
	}
	
	
	static Color average(Color color1, Color color2)
	{
      int r = (color1.getRed() + color2.getRed()) / 2;
      int g = (color1.getGreen() + color2.getGreen()) / 2;
      int b = (color1.getBlue() + color2.getBlue()) / 2;
      int a = (color1.getAlpha() + color2.getAlpha()) / 2;

	  return new Color(r,g,b,a);
	}
	
	
	static float safeColor(float original) {
		return Math.min(1F, (Math.max(0F, original)));
	}
	
}
