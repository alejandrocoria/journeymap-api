package net.techbrew.mcjm.render;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import net.minecraft.src.BiomeGenBase;
import net.techbrew.mcjm.ChunkStub;
import net.techbrew.mcjm.JourneyMap;

public class MapBlocks extends HashMap {
	
	public static AlphaComposite OPAQUE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1F);
	public static AlphaComposite CLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0F);
	public static AlphaComposite SEMICLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F);
	public static Color COLOR_TRANSPARENT = new Color(0,0,0,0);
	
	public static final HashSet<Integer> sky = new HashSet<Integer>(2);
	static
	{
		sky.add(0); // air 
		sky.add(8); // water 
		sky.add(9); // stationary water 
		sky.add(18); // leaves
		sky.add(30); // web
		sky.add(65); // ladder
		sky.add(106); // vines
	}
	
	float[][] alphas = new float[256][16];
	{
		for(int i=0;i<alphas.length;i++) {
			Arrays.fill(alphas[i], 1F);
		}
		alphas[8][0] = .5F; // water
		alphas[9][0] = .5F; // water
		alphas[20][0] = .3F; // glass
		alphas[102][0] = .3F; // water
		alphas[79][0] = .8F; // ice
	}
	
	public Color getGrassColor(String biomeName) {
		if(biomeName!=null && grassBiomeColors.containsKey(biomeName)) {
			return grassBiomeColors.get(biomeName);
		} else {
			return grassGeneric;
		}
	}
	
	public Color getFoliageColor(String biomeName) {
		if(biomeName!=null && foliageBiomeColors.containsKey(biomeName)) {
			return foliageBiomeColors.get(biomeName);
		} else {
			return foliageGeneric;
		}
	}
	
	Color grassGeneric = new Color(75,103,65);
	private HashMap<String, Color> grassBiomeColors = new HashMap<String, Color>(10);
	{
		grassBiomeColors.put(BiomeGenBase.beach.biomeName, new Color(98,105,45));
		grassBiomeColors.put(BiomeGenBase.desert.biomeName, new Color(98,105,45));
		grassBiomeColors.put(BiomeGenBase.desertHills.biomeName, new Color(98,105,45));
		grassBiomeColors.put(BiomeGenBase.extremeHills.biomeName, new Color(74,97,73));
		grassBiomeColors.put(BiomeGenBase.extremeHillsEdge.biomeName, new Color(74,97,73));
		grassBiomeColors.put(BiomeGenBase.forest.biomeName, new Color(70,110,52));
		grassBiomeColors.put(BiomeGenBase.forestHills.biomeName, new Color(70,110,52));
		grassBiomeColors.put(BiomeGenBase.frozenOcean.biomeName, new Color(84,110,52));
		grassBiomeColors.put(BiomeGenBase.frozenRiver.biomeName, new Color(84,110,52));
		//grassBiomeColors.put(BiomeGenBase.hell.biomeName, new Color(70,110,52));
		grassBiomeColors.put(BiomeGenBase.iceMountains.biomeName, new Color(70,110,52));
		grassBiomeColors.put(BiomeGenBase.icePlains.biomeName, new Color(70,110,52));
		grassBiomeColors.put(BiomeGenBase.jungle.biomeName, new Color(50,150,30));
		grassBiomeColors.put(BiomeGenBase.jungleHills.biomeName, new Color(50,150,30));
		grassBiomeColors.put(BiomeGenBase.mushroomIsland.biomeName, new Color(110,95,110));
		grassBiomeColors.put(BiomeGenBase.mushroomIslandShore.biomeName, new Color(110,95,110)); 
		grassBiomeColors.put(BiomeGenBase.ocean.biomeName, new Color(84,110,52)); 
		grassBiomeColors.put(BiomeGenBase.plains.biomeName, new Color(84,110,52)); 
		grassBiomeColors.put(BiomeGenBase.river.biomeName, new Color(84,110,52)); 
		grassBiomeColors.put(BiomeGenBase.swampland.biomeName, new Color(52,59,44));
		grassBiomeColors.put(BiomeGenBase.taiga.biomeName, new Color(84,110,52));
		grassBiomeColors.put(BiomeGenBase.taigaHills.biomeName, new Color(84,110,52));
	}
	
	public Color getWaterColor(String biomeName) {
		if(biomeName!=null && waterBiomeColors.containsKey(biomeName)) {
			return waterBiomeColors.get(biomeName);
		} else {
			return waterGeneric;
		}
	}
	
	Color waterGeneric = new Color(25,39,153);
	private HashMap<BiomeGenBase, Color> waterBiomeColors = new HashMap<BiomeGenBase, Color>(10);
	{
		waterBiomeColors.put(BiomeGenBase.swampland, new Color(29,29,50));
		waterBiomeColors.put(BiomeGenBase.taiga, waterGeneric);
	}

	Color foliageGeneric = new Color(35,90,17);
	private HashMap<String, Color> foliageBiomeColors = new HashMap<String, Color>(10);
	{
		foliageBiomeColors.put(BiomeGenBase.beach.biomeName, new Color(36,95,17));
		foliageBiomeColors.put(BiomeGenBase.desert.biomeName, new Color(36,95,17));
		foliageBiomeColors.put(BiomeGenBase.desertHills.biomeName, new Color(36,95,17));
		foliageBiomeColors.put(BiomeGenBase.extremeHills.biomeName, new Color(47,87,24));
		foliageBiomeColors.put(BiomeGenBase.extremeHillsEdge.biomeName, new Color(47,87,24));
		foliageBiomeColors.put(BiomeGenBase.jungle.biomeName, new Color(20,151,2));
		foliageBiomeColors.put(BiomeGenBase.jungleHills.biomeName, new Color(20,151,2));
		foliageBiomeColors.put(BiomeGenBase.mushroomIsland.biomeName, new Color(110,95,110));
		foliageBiomeColors.put(BiomeGenBase.mushroomIslandShore.biomeName, new Color(110,95,110)); 
		foliageBiomeColors.put(BiomeGenBase.river.biomeName, new Color(43,88,22));
		
		foliageBiomeColors.put(BiomeGenBase.swampland.biomeName, new Color(37,49,28));
		foliageBiomeColors.put(BiomeGenBase.taiga.biomeName, new Color(36,90,17));
		foliageBiomeColors.put(BiomeGenBase.taigaHills.biomeName, new Color(36,90,17));
	}
	
	//HashMap<int[], Color> colors = new HashMap<int[], Color>(121);
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
		
		colors[18][0] = new Color(0x2b7115); // leaves 
		colors[18][1] = new Color(0x2c462c); // redwood leaves  
		colors[18][2] = new Color(0x485e30); // birchwood leaves
		colors[18][8] = new Color(0x395939); // leaves ?
		colors[18][9] = new Color(0x395939); // leaves ?
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
		
		colors[31][0] = new Color(0x74b422); // dead shrub
		colors[31][1] = new Color(0x74b44a); // tall grass
		colors[31][2] = new Color(0x74b422); // live shrub
		colors[32][0] = new Color(0x74b422); // dead shrub
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
		colors[78][0] = new Color(0xffffff); // Snow
		colors[79][0] = new Color(0x8ebfff); // Ice
		colors[80][0] = new Color(0xfafaff); // Snow Block
		colors[81][0] = new Color(0x11801e); // Cactus
		colors[82][0] = new Color(0xbbbbcc); // Clay
		colors[83][0] = new Color(0xa1a7b2); // Sugar Cane
		colors[84][0] = new Color(0xaadb74); // Jukebox
		colors[85][0] = new Color(0xbc9862); // Fence
		colors[86][0] = new Color(0xce7b14); // Pumpkin
		colors[87][0] = new Color(0x582218); // Netherrack
		colors[88][0] = new Color(0x996731); // Soul Sand
		colors[89][0] = new Color(0xcda838); // Glowstone
		colors[90][0] = new Color(0x643993); // Nether Portal
		colors[91][0] = new Color(0xe08e1d); // Jack-O-Lantern
						   						    // 93		Redstone Repeater Block (off)
													// 94		Redstone Repeater Block (on)
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
		  
		colors[120][0] = new Color(0x0b0b0b);	// 119 End Portal 
		colors[120][0] = new Color(0x2d544f);	// 120 End Portal Frame
		colors[121][0] = new Color(0xc6bd6d);	// 121 End Stone
		
		colors[125][0] = new Color(0x94794a); // double wooden slab
		colors[126][0] = new Color(0x94794a); // double wooden slab
		
		colors[128][0] = new Color(0xc6bd6d);   // 128 Sandstone Stairs
		colors[129][0] = new Color(0x37b957);   // 129 Emerald Ore
		colors[133][0] = new Color(0x37b957);   // 133 Emerald Block
		
		colors[137][0] = new Color(0xc39b81);   // 137 Command Block
		colors[138][0] = new Color(0xbff9fd);   // 138 Beacon Block
		
		colors[139][0] = new Color(0x959595); // cobblestone wall
		colors[139][1] = new Color(0x1f471f);  // Mossy Cobblestone wall
		
		colors[145][0] = new Color(0x424242);   // 145 Anvil
		colors[200][0] = new Color(0xd270f7);	// 200 Ender Crystal
		
		colors[390][0] = new Color(0x7d4536);   // 390 flower pot
		
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
	boolean skyAbove(ChunkStub chunkStub, final int x, final int y, final int z) {
		boolean seeSky = true;
		int blockId;
		
		final int topY = chunkStub.getTopSolidOrLiquidBlock();
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
		
		BiomeGenBase biome = chunkStub.getBlockBiome(x, z);
		String biomeName = biome.biomeName;

		switch(blockInfo[0]) {
			case 2 : {
				color = getGrassColor(biomeName);
				//color = blend(new Color(chunkStub.grassColor), Color.black, .5);
				break;
			}
			case 8 : {
				//color = getBiomeWaterColor(chunk, x, y, z); // water
				color = getWaterColor(biomeName);
				break;
			}
			case 9 : {
				//color = getBiomeWaterColor(chunk, x, y, z); // water
				color = getWaterColor(biomeName);
				break;
			}
			case 18 : {
				//color = getBiomeFoliageColor(biome, chunk, y); // foliage
				color = getFoliageColor(biomeName);
				//color = blend(new Color(chunkStub.foliageColor), Color.black, .5); // foliage
				break;
			} 
			default : {
				color = getColor(blockInfo);
				break;
			}
		}
		
		return color;
	}
	
//	Color getBiomeWaterColor(Chunk chunk, int x, int y, int z) {
//		int l = Block.waterStill.colorMultiplier(chunk.worldObj, x,y,z);
//        float r = (float)(l >> 16 & 0xff) / 255F;
//        float g = (float)(l >> 8 & 0xff) / 255F;
//        float b = (float)(l & 0xff) / 255F;
//		return new Color(r*.05F,g*.15F,b*.8F);
//	}
	
	static Color blend(Color color1, Color color2, double ratio)
	  {
	    float r  = (float) ratio;
	    float ir = (float) 1.0 - r;

	    float rgb1[] = new float[3];
	    float rgb2[] = new float[3];    

	    color1.getColorComponents (rgb1);
	    color2.getColorComponents (rgb2);    

	    Color color = new Color (rgb1[0] * r + rgb2[0] * ir, 
	                             rgb1[1] * r + rgb2[1] * ir, 
	                             rgb1[2] * r + rgb2[2] * ir);
	    
	    return color;
	}

	static float safeColor(float original) {
		return Math.min(1F, (Math.max(0F, original)));
	}
	
}
