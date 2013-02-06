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
	public Color getFoliageColor(BiomeGenBase biome, BlockInfo blockInfo) {
		Color color = foliageBiomeColors.get(biome.biomeName + blockInfo.meta);
		if(color==null) {
			color = average(new Color(biome.getBiomeFoliageColor()), getColor(blockInfo));
			foliageBiomeColors.put(biome.biomeName + blockInfo.meta, color.darker());
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
	
	/**
	 * Returns a simple wrapper object of the blockId and the block meta values.
	 * @param chunkStub
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	static BlockInfo getBlockInfo(ChunkStub chunkStub, int x, int y, int z) {
		try {
			int blockId = chunkStub.getBlockID(x, y, z);
			int meta = chunkStub.getBlockMetadata(x, y, z);
			return new BlockInfo(blockId, meta);
		} catch (ArrayIndexOutOfBoundsException e) {
			JourneyMap.getLogger().warning("Can't get blockId/meta for chunk " + chunkStub.xPosition + "," + chunkStub.zPosition + " block " + x + "," + y + "," + z); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			return null;
		}
	}
	
	/**
	 * Get the color for the BlockInfo.
	 * If not found, tries the same block ID with 0 meta.
	 * If still not found, returns black.  Adds alternate
	 * keys to make the second access faster.
	 * @param blockInfo
	 * @return
	 */
	Color getColor(BlockInfo blockInfo) {
		Color color = colors.get(blockInfo);
		if(color!=null) {
			return color;
		}
		
		color = colors.get(new BlockInfo(blockInfo.id, 0));
		if(color==null) {
			color = Color.black;
			JourneyMap.getLogger().warning("Using black for unknown block " + blockInfo.id + "," + blockInfo.meta);
		} else {
			JourneyMap.getLogger().info("Using color for meta 0 with original " + blockInfo.id + "," + blockInfo.meta);
		}
		colors.put(new BlockInfo(blockInfo.id, blockInfo.meta), color);		
		return color;
	}
	
	Float getBlockAlpha(BlockInfo blockInfo) {
		Float alpha = alphas.get(blockInfo.id);
		return (alpha==null) ? 1f : alpha;
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
	
	Color getBlockColor(ChunkStub chunkStub, BlockInfo blockInfo, int x, int y, int z) {
		Color color = null;
		
		BiomeGenBase biome = chunkStub.getBiomeGenForWorldCoords(x, z, chunkStub.worldObj.getWorldChunkManager());
		String biomeName = biome.biomeName;

		switch(blockInfo.id) {
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
	public final static HashSet<Integer> sky = new HashSet<Integer>(7);
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
	 * Map of block ids that shouldn't cast shadows
	 */
	public final static HashSet<Integer> excludeHeight = new HashSet<Integer>(5);
	{
		excludeHeight.add(0); // air 
		excludeHeight.add(31); // grass, fern 
		excludeHeight.add(32); // shrub 
		excludeHeight.add(106); // vines
	}
	
	/**
	 * Alpha values for block ids.
	 */
	public final static HashMap<Integer, Float> alphas = new HashMap<Integer, Float>(5);
	{
		alphas.put(8,.65F); // water
		alphas.put(9,.65F); // water
		alphas.put(20,.3F); // glass
		alphas.put(79,.8F); // ice
		alphas.put(102,.3F); // glass		
		alphas.put(131,0F); // tripwire hook
		alphas.put(132,0F); // tripwire
	}

	/**
	 * Block colors by id + meta
	 */
	HashMap<BlockInfo, Color> colors = new HashMap<BlockInfo, Color>(256);
	{
		colors.put(new BlockInfo(0,0), new Color(0x000000)); // air
		colors.put(new BlockInfo(1,0), new Color(0x686868)); // stone
		colors.put(new BlockInfo(2,0), new Color(0x7fb238)); // grass 
		colors.put(new BlockInfo(3,0), new Color(0x79553a)); // dirt 
		colors.put(new BlockInfo(4,0), new Color(0x959595)); // cobblestone
		colors.put(new BlockInfo(5,0), new Color(0xbc9862)); // oak plank
		colors.put(new BlockInfo(5,1), new Color(0x342919)); // spruce plank
		colors.put(new BlockInfo(5,2), new Color(0x455b2e)); // birch plank
		colors.put(new BlockInfo(5,3), new Color(0x3A4D27)); // jungle plank
		colors.put(new BlockInfo(6,0), new Color(0xa2c978)); // sapling
		colors.put(new BlockInfo(6,1), new Color(0xa2c978)); // redwood sapling 
		colors.put(new BlockInfo(6,2), new Color(0xa2c978)); // birch sapling
		colors.put(new BlockInfo(7,0), new Color(0x333333)); // bedrock
		colors.put(new BlockInfo(8,0), new Color(0x4040ff)); // water 
		colors.put(new BlockInfo(9,0), new Color(0x4040ff)); // stationary water 
		colors.put(new BlockInfo(10,0), new Color(0xE25822)); // lava
		colors.put(new BlockInfo(11,0), new Color(0xE25822)); // stationary lava
		colors.put(new BlockInfo(12,0), new Color(0xddd7a0)); // sand
		colors.put(new BlockInfo(13,0), new Color(0x747474)); // gravel
		colors.put(new BlockInfo(14,0), new Color(0x747474)); // gold ore
		colors.put(new BlockInfo(15,0), new Color(0x747474)); // iron ore
		colors.put(new BlockInfo(16,0), new Color(0x747474)); // coal ore		
		colors.put(new BlockInfo(17,0), new Color(0x675132)); // oak wood
		colors.put(new BlockInfo(17,1), new Color(0x342919)); // spruce wood
		colors.put(new BlockInfo(17,2), new Color(0x455b2e)); // birch wood
		colors.put(new BlockInfo(17,3), new Color(0x3A4D27)); // jungle wood		
		colors.put(new BlockInfo(17,4), new Color(0x675132)); // oak wood east/west
		colors.put(new BlockInfo(17,5), new Color(0x342919)); // spruce wood  east/west
		colors.put(new BlockInfo(17,6), new Color(0x455b2e)); // birch wood  east/west
		colors.put(new BlockInfo(17,7), new Color(0x3A4D27)); // jungle wood  east/west		
		colors.put(new BlockInfo(17,8), new Color(0x675132)); // oak wood north/south
		colors.put(new BlockInfo(17,9), new Color(0x342919)); // spruce wood north/south
		colors.put(new BlockInfo(17,10), new Color(0x455b2e)); // birch wood north/south
		colors.put(new BlockInfo(17,11), new Color(0x3A4D27)); // jungle wood north/south		
		colors.put(new BlockInfo(17,12), new Color(0x675132)); // oak wood bark only
		colors.put(new BlockInfo(17,13), new Color(0x342919)); // spruce wood bark only
		colors.put(new BlockInfo(17,14), new Color(0x455b2e)); // birch wood bark only
		colors.put(new BlockInfo(17,15), new Color(0x3A4D27)); // jungle wood bark only		
		colors.put(new BlockInfo(18,0), new Color(0x21530B)); // oak leaves 
		colors.put(new BlockInfo(18,1), new Color(0x21530B)); // spruce leaves  
		colors.put(new BlockInfo(18,2), new Color(0x3D4F28)); // birch leaves
		colors.put(new BlockInfo(18,3), new Color(0x135502)); // jungle leaves	
		colors.put(new BlockInfo(18,4), new Color(0x21530B)); // oak leaves permanent
		colors.put(new BlockInfo(18,5), new Color(0x21530B)); // spruce leaves permanent
		colors.put(new BlockInfo(18,6), new Color(0x3D4F28)); // birch leaves permanent
		colors.put(new BlockInfo(18,7), new Color(0x135502)); // jungle leaves permanent
		colors.put(new BlockInfo(18,8), new Color(0x21530B)); // oak leaves decay
		colors.put(new BlockInfo(18,9), new Color(0x21530B)); // spruce leaves decay
		colors.put(new BlockInfo(18,10), new Color(0x3D4F28)); // birch leaves decay
		colors.put(new BlockInfo(18,11), new Color(0x135502)); // jungle leaves decay
		colors.put(new BlockInfo(19,0), new Color(0xe5e54e)); // sponge
		colors.put(new BlockInfo(20,0), new Color(0xffffff)); // glass
		colors.put(new BlockInfo(21,0), new Color(0x6d7484)); // lapis lazuli ore
		colors.put(new BlockInfo(22,0), new Color(0x1542b2)); // lapis lazuli block
		colors.put(new BlockInfo(23,0), new Color(0x585858)); // dispenser
		colors.put(new BlockInfo(24,0), new Color(0xc6bd6d)); // sandstone
		colors.put(new BlockInfo(25,0), new Color(0x784f3a)); // note block
		colors.put(new BlockInfo(26,0), new Color(0xa95d5d)); // bed block		
		colors.put(new BlockInfo(27,0), new Color(0xa4a4a4)); // powered rail
		colors.put(new BlockInfo(28,0), new Color(0xa4a4a4)); // detector rail
		colors.put(new BlockInfo(29,0), new Color(0x784f3a)); // sticky piston
		colors.put(new BlockInfo(30,0), new Color(0xcccccc)); // web		
		colors.put(new BlockInfo(31,0), new Color(0x648540)); // dead shrub
		colors.put(new BlockInfo(31,1), new Color(0x265c0e)); // tall grass
		colors.put(new BlockInfo(31,2), new Color(0x265c0e)); // fern (living shrub)
		colors.put(new BlockInfo(31,3), new Color(0x265c0e)); // tall grass	
		colors.put(new BlockInfo(32,0), new Color(0x648540)); // dead shrub				
		colors.put(new BlockInfo(33,0), new Color(0x550000)); // piston
		colors.put(new BlockInfo(34,0), new Color(0x550000)); // piston head		
		colors.put(new BlockInfo(35,0), new Color(0xdddddd)); // white wool
		colors.put(new BlockInfo(35,1), new Color(0xeb8138)); // orange wool
		colors.put(new BlockInfo(35,2), new Color(0xc04cca)); // magenta wool
		colors.put(new BlockInfo(35,3), new Color(0x8aa3d8)); // light blue wool
		colors.put(new BlockInfo(35,4), new Color(0xd3ba27)); // yellow wool
		colors.put(new BlockInfo(35,5), new Color(0x38b62d)); // light green wool
		colors.put(new BlockInfo(35,6), new Color(0xd8879e)); // pink wool
		colors.put(new BlockInfo(35,7), new Color(0x3a3a3a)); // gray wool
		colors.put(new BlockInfo(35,8), new Color(0xa6adad)); // light gray wool 
		colors.put(new BlockInfo(35,9), new Color(0x246985)); // cyan wool
		colors.put(new BlockInfo(35,10), new Color(0x8639cb)); // purple wool 
		colors.put(new BlockInfo(35,11), new Color(0x2937a5)); // blue wool
		colors.put(new BlockInfo(35,12), new Color(0x51301b)); // brown wool
		colors.put(new BlockInfo(35,13), new Color(0x354a18)); // dark green wool 
		colors.put(new BlockInfo(35,14), new Color(0x9c2a27)); // red wool 
		colors.put(new BlockInfo(35,15), new Color(0x181414)); // black wool 		
		colors.put(new BlockInfo(37,0), new Color(0xf1f902)); // dandelion
		colors.put(new BlockInfo(38,0), new Color(0xf7070f)); // rose
		colors.put(new BlockInfo(39,0), new Color(0x916d55)); // brown mushroom
		colors.put(new BlockInfo(40,0), new Color(0x9a171c)); // red mushroom		
		colors.put(new BlockInfo(41,0), new Color(0xfefb5d)); // gold block
		colors.put(new BlockInfo(42,0), new Color(0xe9e9e9)); // iron block		
		colors.put(new BlockInfo(43,0), new Color(0xa8a8a8)); // double stone slab
		colors.put(new BlockInfo(43,1), new Color(0xe5ddaf)); // double sandstone slab
		colors.put(new BlockInfo(43,2), new Color(0x94794a)); // double wooden slab
		colors.put(new BlockInfo(43,3), new Color(0x828282)); // Double Cobblestone Slab
		colors.put(new BlockInfo(43,4), new Color(0xaa543b)); // Double Brick Slab
		colors.put(new BlockInfo(43,5), new Color(0xa8a8a8)); // double stone brick slab		
		colors.put(new BlockInfo(44,0), new Color(0xa8a8a8)); // stone slab
		colors.put(new BlockInfo(44,1), new Color(0xc6bd6d)); // sandstone slab
		colors.put(new BlockInfo(44,2), new Color(0x94794a)); // wooden slab
		colors.put(new BlockInfo(44,3), new Color(0x828282)); // cobblestone slab
		colors.put(new BlockInfo(44,4), new Color(0xaa543b)); // brick slab
		colors.put(new BlockInfo(44,5), new Color(0xa8a8a8)); // stone brick slab
		colors.put(new BlockInfo(44,6), new Color(0x34191e)); // nether brick slab
		colors.put(new BlockInfo(44,7), new Color(0xa8a8a8)); // stone slab duplicate		
		colors.put(new BlockInfo(44,8), new Color(0xa8a8a8)); // upsidedown stone slab
		colors.put(new BlockInfo(44,9), new Color(0xc6bd6d)); // upsidedown sandstone slab
		colors.put(new BlockInfo(44,10), new Color(0x94794a)); // upsidedown wooden slab
		colors.put(new BlockInfo(44,11), new Color(0x828282)); // upsidedown cobblestone slab
		colors.put(new BlockInfo(44,12), new Color(0xaa543b)); // upsidedown brick slab
		colors.put(new BlockInfo(44,13), new Color(0xa8a8a8)); // upsidedown stone brick slab
		colors.put(new BlockInfo(44,14), new Color(0x34191e)); // upsidedown nether brick slab		 
		colors.put(new BlockInfo(45,0), new Color(0xaa543b)); // brick
		colors.put(new BlockInfo(46,0), new Color(0xdb441a)); // TNT
		colors.put(new BlockInfo(47,0), new Color(0xb4905a)); // Bookshelf
		colors.put(new BlockInfo(48,0), new Color(0x1f471f)); // Mossy Cobblestone
		colors.put(new BlockInfo(49,0), new Color(0x101018)); // Obsidian		
		colors.put(new BlockInfo(50,0), new Color(0xffd800)); // Torch
		colors.put(new BlockInfo(51,0), new Color(0xc05a01)); // Fire
		colors.put(new BlockInfo(52,0), new Color(0x265f87)); // Monster Spawner
		colors.put(new BlockInfo(53,0), new Color(0xbc9862)); // Wooden Stairs
		colors.put(new BlockInfo(54,0), new Color(0x8f691d)); // Chest
		colors.put(new BlockInfo(55,0), new Color(0x480000)); // Redstone Wire
		colors.put(new BlockInfo(56,0), new Color(0x747474)); // Diamond Ore
		colors.put(new BlockInfo(57,0), new Color(0x82e4e0)); // Diamond Block
		colors.put(new BlockInfo(58,0), new Color(0xa26b3e)); // Workbench
		colors.put(new BlockInfo(59,0), new Color(0xe210));   // Wheat Crops
		colors.put(new BlockInfo(60,0), new Color(0x633f24)); // Soil
		colors.put(new BlockInfo(61,0), new Color(0x747474)); // Furnace
		colors.put(new BlockInfo(62,0), new Color(0x808080)); // Burning Furnace
		colors.put(new BlockInfo(63,0), new Color(0xb4905a)); // Sign Post
		colors.put(new BlockInfo(64,0), new Color(0x7a5b2b)); // Wooden Door Block
		colors.put(new BlockInfo(65,0), new Color(0xac8852)); // Ladder
		colors.put(new BlockInfo(66,0), new Color(0xa4a4a4)); // Rails
		colors.put(new BlockInfo(67,0), new Color(0x9e9e9e)); // Cobblestone Stairs
		colors.put(new BlockInfo(68,0), new Color(0x9f844d)); // Wall Sign
		colors.put(new BlockInfo(69,0), new Color(0x695433)); // Lever
		colors.put(new BlockInfo(70,0), new Color(0x8f8f8f)); // Stone Pressure Plate
		colors.put(new BlockInfo(71,0), new Color(0xc1c1c1)); // Iron Door Block
		colors.put(new BlockInfo(72,0), new Color(0xbc9862)); // Wooden Pressure Plate
		colors.put(new BlockInfo(73,0), new Color(0x747474)); // Redstone Ore
		colors.put(new BlockInfo(74,0), new Color(0x747474)); // Glowing Redstone Ore
		colors.put(new BlockInfo(75,0), new Color(0x290000)); // Redstone Torch (off)
		colors.put(new BlockInfo(76,0), new Color(0xfd0000)); // Redstone Torch (on)
		colors.put(new BlockInfo(77,0), new Color(0x747474)); // Stone Button
		colors.put(new BlockInfo(78,0), new Color(0xeeeeee)); // Snow
		colors.put(new BlockInfo(79,0), new Color(0x8ebfff)); // Ice
		colors.put(new BlockInfo(80,0), new Color(0xfafaff)); // Snow Block
		colors.put(new BlockInfo(81,0), new Color(0x11801e)); // Cactus
		colors.put(new BlockInfo(82,0), new Color(0xbbbbcc)); // Clay
		colors.put(new BlockInfo(83,0), new Color(0x265c0e)); // Sugar Cane
		colors.put(new BlockInfo(84,0), new Color(0xaadb74)); // Jukebox
		colors.put(new BlockInfo(85,0), new Color(0xbc9862)); // Fence
		colors.put(new BlockInfo(86,0), new Color(0xce7b14)); // Pumpkin
		colors.put(new BlockInfo(87,0), new Color(0x582218)); // Netherrack
		colors.put(new BlockInfo(88,0), new Color(0x996731)); // Soul Sand
		colors.put(new BlockInfo(89,0), new Color(0xcda838)); // Glowstone
		colors.put(new BlockInfo(90,0), new Color(0x643993)); // Nether Portal
		colors.put(new BlockInfo(91,0), new Color(0xe08e1d)); // Jack-O-Lantern
		colors.put(new BlockInfo(92,0), new Color(0xe7e7e9)); // Cake		
		colors.put(new BlockInfo(93,0), new Color(0x9e9e9e)); // Redstone Repeater Block (off)
		colors.put(new BlockInfo(94,0), new Color(0x9e9e9e)); // Redstone Repeater Block (on)			
		colors.put(new BlockInfo(95,0), new Color(0x8f691d)); // Locked Chest
		colors.put(new BlockInfo(96,0), new Color(0xbc9855)); // Trapdoor
		colors.put(new BlockInfo(97,0), new Color(0x8f8f8f)); // Stone (Silverfish)
		colors.put(new BlockInfo(97,1), new Color(0x828282)); // Cobblestone (Silverfish)
		colors.put(new BlockInfo(97,2), new Color(0xa8a8a8)); // Stone Brick (Silverfish)
		colors.put(new BlockInfo(98,0), new Color(0x8f8f8f)); // Stone Brick
		colors.put(new BlockInfo(98,1), new Color(0x1f471f)); // Mossy Stone Brick
		colors.put(new BlockInfo(98,2), new Color(0x8f8f8f)); // Cracked Stone Brick
		colors.put(new BlockInfo(99,0), new Color(0x8F0000)); // Red Mushroom Cap
		colors.put(new BlockInfo(100,0), new Color(0xc4a476)); // Brown Mushroom Cap  
		colors.put(new BlockInfo(101,0), new Color(0xe9e9e9)); // Iron Bars
		colors.put(new BlockInfo(102,0), new Color(0xffffff)); // Glass Pane
		colors.put(new BlockInfo(103,0), new Color(0xbcb628)); // Melon Block
		colors.put(new BlockInfo(104,0), new Color(0x74b422)); // Pumpkin Stem 
		colors.put(new BlockInfo(105,0), new Color(0x74b422)); // Melon Stem
		colors.put(new BlockInfo(106,0), new Color(0x74b422)); // Vines
		colors.put(new BlockInfo(107,0), new Color(0xbc9852)); // Fence Gate
		colors.put(new BlockInfo(108,0), new Color(0xaa543b)); // Brick Stairs
		colors.put(new BlockInfo(109,0), new Color(0x8f8f8f)); // Stone Brick Stairs
		colors.put(new BlockInfo(110,0), new Color(0x6E5F6E)); // Mycelium
		colors.put(new BlockInfo(111,0), new Color(0x0d5f15)); // Lily Pad 
		colors.put(new BlockInfo(112,0), new Color(0x34191e)); // Nether Brick
		colors.put(new BlockInfo(113,0), new Color(0x55191e)); // Nether Brick Fence
		colors.put(new BlockInfo(114,0), new Color(0x34191e)); // Nether Brick Stairs
		colors.put(new BlockInfo(115,0), new Color(0x891c31)); // Nether Wart		
		colors.put(new BlockInfo(116,0), new Color(0x550000)); // Enchanting Table
		colors.put(new BlockInfo(117,0), new Color(0xbbc185)); // Brewing Stand		
		colors.put(new BlockInfo(118,0), new Color(0x424242)); // Cauldron
		colors.put(new BlockInfo(119,0), new Color(0x0b0b0b)); // End Portal		
		colors.put(new BlockInfo(120,0), new Color(0x4986bc)); // End Portal Frame
		colors.put(new BlockInfo(121,0), new Color(0xc6bd6d)); // End Stone		
		colors.put(new BlockInfo(122,0), new Color(0x050507)); // Dragon Egg
		colors.put(new BlockInfo(123,0), new Color(0x785b3b)); // Redstone Lamp (off)
		colors.put(new BlockInfo(124,0), new Color(0xab8a55)); // Redstone Lamp (on)		
		colors.put(new BlockInfo(125,0), new Color(0x94794a)); // double wooden slab
		colors.put(new BlockInfo(125,1), new Color(0x675132)); // oak double wooden slab
		colors.put(new BlockInfo(125,2), new Color(0x342919)); // spruce double wooden slab
		colors.put(new BlockInfo(125,3), new Color(0x455b2e)); // birch double wooden slab
		colors.put(new BlockInfo(125,4), new Color(0x3A4D27)); // jungle double wooden slab
		colors.put(new BlockInfo(126,0), new Color(0x94794a)); // wooden slab
		colors.put(new BlockInfo(126,1), new Color(0x675132)); // oak wooden slab
		colors.put(new BlockInfo(126,2), new Color(0x342919)); // spruce wooden slab
		colors.put(new BlockInfo(126,3), new Color(0x455b2e)); // birch wooden slab
		colors.put(new BlockInfo(126,4), new Color(0x3A4D27)); // jungle wooden slab
		colors.put(new BlockInfo(127,0), new Color(0xcd8e4b)); // cocoa plant
		colors.put(new BlockInfo(128,0), new Color(0xc6bd6d)); // Sandstone Stairs
		colors.put(new BlockInfo(129,0), new Color(0x37b957)); // Emerald Ore
		colors.put(new BlockInfo(130,0), new Color(0x101019)); // Ender Chest
		colors.put(new BlockInfo(131,0), new Color(0x785b3b)); // Tripwire Hook
		colors.put(new BlockInfo(132,0), new Color(0x785b3b)); // Tripwire
		colors.put(new BlockInfo(133,0), new Color(0x37b957)); // Emerald Block
		colors.put(new BlockInfo(134,0), new Color(0x342919)); // spruce wooden stairs
		colors.put(new BlockInfo(135,0), new Color(0x455b2e)); // birch wooden stairs
		colors.put(new BlockInfo(136,0), new Color(0x3A4D27)); // jungle wooden stairs		
		colors.put(new BlockInfo(137,0), new Color(0xc39b81)); // Command Block
		colors.put(new BlockInfo(138,0), new Color(0xbff9fd)); // Beacon Block		
		colors.put(new BlockInfo(139,0), new Color(0x959595)); // cobblestone wall
		colors.put(new BlockInfo(139,1), new Color(0x1f471f)); // Mossy Cobblestone wall		
		colors.put(new BlockInfo(140,0), new Color(0x7d4536)); // Flower pot		
		colors.put(new BlockInfo(141,0), new Color(0x0ac200)); // Carrot crop
		colors.put(new BlockInfo(142,0), new Color(0x00e01a)); // Potato crop
		colors.put(new BlockInfo(143,0), new Color(0x94794a)); // wooden button
		colors.put(new BlockInfo(144,0), new Color(0xff5b3b)); // Monster head
		colors.put(new BlockInfo(145,0), new Color(0x424242)); // Anvil
		colors.put(new BlockInfo(146,0), new Color(0x8f691d)); // Trapped Chest
		colors.put(new BlockInfo(147,0), new Color(0xf9f249)); // Light Pressure Plate
		colors.put(new BlockInfo(148,0), new Color(0xe0e0e0)); // Heavy Pressure Plate
		colors.put(new BlockInfo(149,0), new Color(0x8c8c8c)); // Redstone Comparator Inactive
		colors.put(new BlockInfo(150,0), new Color(0x8c8c8c)); // Redstone Comparator Active
		colors.put(new BlockInfo(151,0), new Color(0xbaa890)); // Daylight Sensor
		colors.put(new BlockInfo(152,0), new Color(0xc22b18)); // Redstone Block
		colors.put(new BlockInfo(153,0), new Color(0x915c56)); // Nether Quartz
		colors.put(new BlockInfo(154,0), new Color(0x434343)); // Hopper
		colors.put(new BlockInfo(155,0), new Color(0xdddcd7)); // Block of Quartz
		colors.put(new BlockInfo(156,0), new Color(0xdddcd7)); // Quartz Stairs
		colors.put(new BlockInfo(157,0), new Color(0xa4a4a4)); // Activator rail
		colors.put(new BlockInfo(158,0), new Color(0x585858)); // Dropper
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
