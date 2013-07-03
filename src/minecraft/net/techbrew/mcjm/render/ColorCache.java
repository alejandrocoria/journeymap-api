package net.techbrew.mcjm.render;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;

import javax.imageio.ImageIO;


import net.minecraft.src.BiomeGenBase;
import net.minecraft.src.Block;
import net.minecraft.src.BlockGrass;
import net.minecraft.src.BlockLeaves;
import net.minecraft.src.BlockLilyPad;
import net.minecraft.src.BlockTallGrass;
import net.minecraft.src.BlockVine;
import net.minecraft.src.Icon;
import net.minecraft.src.Item;
import net.minecraft.src.Resource;
import net.minecraft.src.ResourcePack;
import net.minecraft.src.ResourcePackRepository;
import net.minecraft.src.ResourcePackRepositoryEntry;
import net.minecraft.src.TextureAtlasSprite;
import net.minecraft.src.TextureMap;
import net.minecraft.src.TexturedQuad;

import net.minecraft.src.ItemBlock;
import net.minecraft.src.Minecraft;

import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.ChunkStub;

/**
 * Cache of block colors derived from the current texture pack.
 * @author mwoodman
 *
 */
public class ColorCache {
	
	final static float MAGIC = 0.003921569F;
	final Color grassOffsetColor = new Color(0x111111);
	final HashMap<BlockInfo, Color> colors = new HashMap<BlockInfo, Color>(256);
	final HashMap<BiomeGenBase, Color> grassBiomeColors = new HashMap<BiomeGenBase, Color>(16);
	final HashMap<BiomeGenBase, Color> waterBiomeColors = new HashMap<BiomeGenBase, Color>(16);
	final HashMap<String, Color> foliageBiomeColors = new HashMap<String, Color>(16);
	final HashMap<BlockInfo, Float> partialTransparencies = new HashMap<BlockInfo, Float>(16);
	
	final boolean useCustomTexturePack;
	final ResourcePackRepository texturePackList;
	volatile ResourcePack texturePack;
	
	volatile String lastTextureName = null;
	volatile byte[] lastTextureData;
	volatile long lastTextureUsed;
	
	public ColorCache() {
		texturePackList = Minecraft.getMinecraft().func_110438_M();
		useCustomTexturePack = PropertyManager.getInstance().getBoolean(PropertyManager.Key.USE_CUSTOM_TEXTUREPACK);
		init();
	}
	
	/**
	 * Reset the cache colors and use the selected texture pack.
	 */
	void init() {
		
		if(useCustomTexturePack) {
			texturePack = texturePackList.field_110620_b; // DefaultResourcePack
			// TODO
			//texturePack = ((ResourcePackRepositoryEntry) texturePackList.func_110613_c().get(0)).func_110514_c();
		} else {
			texturePack = texturePackList.field_110620_b; // DefaultResourcePack
		}
		
		JourneyMap.getLogger().info("Deriving block colors from texture pack: " + texturePack.func_130077_b());
		
		lastTextureName = null;
		lastTextureData = null;
		grassBiomeColors.clear();
		waterBiomeColors.clear();
		foliageBiomeColors.clear();		
		
		colors.clear();
				
		colors.put(new BlockInfo(0,0), new Color(0x000000)); // air
		
		MapBlocks.resetAlphas();
		
	}
	
	public Color getBlockColor(ChunkStub chunkStub, BlockInfo blockInfo, int x, int y, int z) {
		Color color = null;
		
		if(blockInfo.color!=null) {
			return blockInfo.color;
		}
		
		BiomeGenBase biome = chunkStub.getBiomeGenForWorldCoords(x, z, chunkStub.worldObj.getWorldChunkManager());
		String biomeName = biome.biomeName;
		
		Block block = blockInfo.getBlock();
		
		if(block instanceof BlockLeaves || block instanceof BlockVine) {
			
			color = foliageBiomeColors.get(blockInfo.hashCode() + biomeName);
			if(color==null) {
				color = getBlockColor(blockInfo);
				color = multiply(color, biome.getBiomeFoliageColor());
				foliageBiomeColors.put(blockInfo.hashCode() + biomeName, color);
			}
			
		} else if(block instanceof BlockGrass || block instanceof BlockTallGrass) {

			color = grassBiomeColors.get(biome);
			if(color==null) {
				// TODO: Use scanned color?
				color = multiply(Color.lightGray, biome.getBiomeGrassColor());
				grassBiomeColors.put(biome, color);
			}
			
		} else if(block==Block.waterStill || block==Block.waterMoving) {
			
			color = waterBiomeColors.get(biome);
			if(color==null) {
				color = multiply(getBlockColor(blockInfo), biome.waterColorMultiplier);
				waterBiomeColors.put(biome, color);
			}
			
		} else if(block instanceof BlockLilyPad) {
			
			color = ColorCache.multiply(getBlockColor(blockInfo), block.getRenderColor(blockInfo.meta));

		} else {
			
			int rc = block.getRenderColor(blockInfo.meta);
			if(rc!=16777215 && rc!=0) {
				color = new Color(rc);
			} else {			
				color = getBlockColor(blockInfo);
			}
		}
		
		retireCachedData();
		
		return color;
	}
	
	/**
	 * Gets the color for the block from the cache, or
	 * lazy-loads it first.
	 * @param blockInfo
	 * @return
	 */
	private Color getBlockColor(BlockInfo blockInfo) {
		
		if(blockInfo.color!=null) {
			return blockInfo.color;
		}

		// Check for changed texturepack
		// TODO
		//if(useCustomTexturePack && texturePack!=((ResourcePackRepositoryEntry) texturePackList.func_110613_c().get(0)).func_110514_c()) {
		if(useCustomTexturePack && texturePack!=texturePackList.field_110620_b) {
			init();
		}
		
		Color color = colors.get(blockInfo);
		if(color==null) {
			color = loadBlockColor(blockInfo);
			if(color==null) {
				color = Color.black;
			}
			colors.put(blockInfo, color);
		}
		return color;		
	}
		
	
	/**
	 * Derive block color from the corresponding texture.
	 * @param blockInfo
	 * @return
	 */
	protected Color loadBlockColor(BlockInfo blockInfo) {
		try {

			JourneyMap.getLogger().fine("Loading color for " + stringInfo(blockInfo));
			
        	double loadStart = System.nanoTime();    
        	
	        Block block = Block.blocksList[blockInfo.id];

            if (block == null) {
            	JourneyMap.getLogger().fine("No block type found");
            	
            	ItemBlock item = (ItemBlock) Item.itemsList[blockInfo.id];
            	if(item!=null) {
            		JourneyMap.getLogger().warning("ItemBlocks not supported: " + item.itemID + ":" + item.getMetadata(0) + " " + item.getUnlocalizedName());
            	}

            	return null;
            }

            TextureAtlasSprite blockIcon = (TextureAtlasSprite) block.getIcon(0, blockInfo.meta);
        	if(blockIcon==null) {
        		JourneyMap.getLogger().fine("No top icon");
        		return null;
        	}
        	
        	// TODO
//        	TextureStitchedStub icon = new TextureStitchedStub(blockIcon);	 
//        	Texture texSheet = icon.getTextureSheet();	       
//        	
//        	if(texSheet==null) {
//        		JourneyMap.getLogger().fine("No Texture");
//        		return null;
//        	}
//        	
//        	renewCachedData(texSheet);
        	
        	renewCachedData(blockIcon);
        	
        	Color color = getColorForIcon(blockInfo, blockIcon);            
             
			// Put the color in the map
			colors.put(blockInfo, color);
			if(color.equals(Color.black)) {
				JourneyMap.getLogger().fine("\tColor eval'd to black");
			}
			
		
			// Time the whole thing
            double loadStop = System.nanoTime();   
            double timer = (loadStop-loadStart)/1000000f;            
            JourneyMap.getLogger().fine("\tColor load time: " + timer + "ms");
			
			return color;                           

		} catch (Throwable t) {
			JourneyMap.getLogger().severe("Error getting color: " + LogFormatter.toString(t));
			return null;
		}
	}
	
	Color getColorForIcon(BlockInfo blockInfo, TextureAtlasSprite icon) {

//		TextureStitchedStub icon = new TextureStitchedStub(blockIcon);
		int width = icon.getOriginX();// + icon.getWidth();
		int height = icon.getOriginY();// + icon.getHeight();
		int originX = icon.func_130010_a();
		int originY = icon.func_110967_i();
		//Texture texSheet = icon.getTextureSheet();	
		
//		int sheetWidth = texSheet.getWidth();
//		int sheetHeight = texSheet.getHeight();
		
		int sheetWidth = width;
		int sheetHeight = height;
    	
        // TODO: Track percentage of pixels that aren't transparent, use that as factor when multiplying biome/grass/foliage color
        
        int count = 0;
        int alpha;
        int argb;
        int a=0, r=0, g=0, b=0;
        for(int x=originX; x<width; x++) {
        	for(int y=originY; y<height; y++) {
        		argb = getARGBfromArray(lastTextureData, x, y, sheetWidth);
        		alpha = (argb >> 24) & 0xFF;
        		if(alpha>0) {
        			count++;
        			a+= alpha;
        			r+= (argb >> 16) & 0xFF;
        			g+= (argb >> 8) & 0xFF;
        			b+= (argb >> 0) & 0xFF;
        		}
        	}
        }
        
        if(count>0) {    
        	if(a>0) a = a/count;
        	if(r>0) r = r/count;
        	if(g>0) g = g/count;
        	if(b>0) b = b/count;
        } else {
        	JourneyMap.getLogger().fine("Unusable texture for " + stringInfo(blockInfo));
        	r = g = b = 0;
        }
		
		Color color;
		
		try {        
			color = new Color(r,g,b);
		} catch(IllegalArgumentException e) {
			JourneyMap.getLogger().fine("Bad color for " + stringInfo(blockInfo));
			color = Color.black;
		}
		        
		// Put the alpha in MapBlocks		
		if(blockInfo.getBlock().getRenderBlockPass()>0){
			float blockAlpha = a * 1.0f/255;
			MapBlocks.alphas.put(blockInfo.id, blockAlpha);
			JourneyMap.getLogger().fine("Setting transparency for " + stringInfo(blockInfo));
		} else if(MapBlocks.alphas.containsKey(blockInfo.id)) {
			blockInfo.setAlpha(MapBlocks.alphas.get(blockInfo.id));
		}
		
        return color;
	}
	
	int getARGBfromArray(byte[] bytes, int x, int y, int textureWidth) {
		int pos = y * textureWidth * 4 + x * 4;
        int argb = 0 | (bytes[pos + 2] & 255) << 0; //b
        argb |= (bytes[pos + 1] & 255) << 8;  //g
        argb |= (bytes[pos + 0] & 255) << 16; //r
        argb |= (bytes[pos + 3] & 255) << 24; //a
        return argb;
	}	

	static Color blend(Color color1, int multiplier) {
		int r1 = color1.getRed();
		int g1 = color1.getGreen();
		int b1 = color1.getBlue();

		Color color2 = new Color(multiplier);

		int r2 = 255-color2.getRed();
		int g2 = 255-color2.getGreen();
		int b2 = 255-color2.getBlue();

		int r3 = Math.max(0, r1-r2);
		int g3 = Math.max(0, g1-g2);
		int b3 = Math.max(0, b1-b2);

		return new Color(r3,g3,b3);
	}
	

	static Color average(Collection<Color> colors)
	{
		if(colors.isEmpty()) return null;
		
		int count = colors.size();
		
		int r = 0;
		int g = 0;
		int b = 0;
		
		for(Color color : colors) {
			r+= color.getRed();			
			g+= color.getGreen();
			b+= color.getBlue();
		}
		return new Color(r/count, g/count, b/count);
	}
	
	static Color average(Color... colors)
	{	
		int count = colors.length;
		
		int r = 0;
		int g = 0;
		int b = 0;
		
		for(Color color : colors) {
			r+= color.getRed();			
			g+= color.getGreen();
			b+= color.getBlue();
		}
		return new Color(r/count, g/count, b/count);
	}

	static Color multiply(Color original, int multiplier) {
		int rgba = original.getRGB();
		
		float r = (rgba >> 16 & 0xFF) * MAGIC * ((multiplier >> 16 & 0xFF) * MAGIC);
		float g = (rgba >> 8 & 0xFF)  * MAGIC * ((multiplier >> 8 & 0xFF)  * MAGIC);
		float b = (rgba >> 0 & 0xFF)  * MAGIC * ((multiplier >> 0 & 0xFF)  * MAGIC);

		return new Color(safeColor(r),safeColor(g),safeColor(b)); 
	}

	static float safeColor(float original) {
		return Math.min(1F, (Math.max(0F, original)));
	}
	
	static Color environment(int envMult) {
		
		float r = ((envMult >> 16 & 0xFF) * MAGIC);
		float g = ((envMult >> 8 & 0xFF) * MAGIC);
		float b = ((envMult >> 0 & 0xFF) * MAGIC);

		return new Color(safeColor(r),safeColor(g),safeColor(b)); 
	}
	
	String stringInfo(BlockInfo info) {
		if(info==null) {
			return "BlockInfo null";
		}
		StringBuffer sb = new StringBuffer();
		Block block = info.getBlock();
		if(block!=null) {
			sb.append("Block ").append(block.getUnlocalizedName()).append(" ");
		} else {
			sb.append("Non-Block ");
		}
		sb.append(info.id).append(":").append(info.meta);
		if(block!=null) {
			int bcolor = block.getBlockColor();
			if(bcolor!=16777215) {
				sb.append(", blockColor=").append(Integer.toHexString(bcolor));
			}
			sb.append(", renderType=").append(block.getRenderType());
			int rcolor = block.getBlockColor();
			if(rcolor!=16777215) {
				sb.append(", renderColor=").append(Integer.toHexString(rcolor));
			}
		}
		return sb.toString();
	}
	
	void renewCachedData(TextureAtlasSprite icon) {
		
		// TODO:  Get the resource of the icon this way
//		Minecraft.getMinecraft().func_110442_L().
//		Resource var3 = par1ResourceManager.func_110536_a(this.field_110568_b);
//        var2 = var3.func_110527_b();
//        BufferedImage var4 = ImageIO.read(var2);
        
		// This is here just in case a texture can have more than one sheet for blocks
    	// If the texture sheet has changed, will need a new bufferedimage
//    	if(!texSheet.getTextureName().equals(lastTextureName)) {
//    		
//            ByteBuffer buffer = texSheet.getTextureData();
//            lastTextureName = texSheet.getTextureName();
//            lastTextureData = new byte[texSheet.getWidth() * texSheet.getHeight() * 4];
//            buffer.position(0);
//            buffer.get(lastTextureData);
//            lastTextureUsed = System.currentTimeMillis();
//    		JourneyMap.getLogger().fine("Cached texture bytes (" + lastTextureData.length/1024 + "KB): " + lastTextureName);
//    	}
	}
	
	void retireCachedData() {
		if(lastTextureUsed+10000<System.currentTimeMillis()) {
			if(lastTextureData!=null) {
				JourneyMap.getLogger().fine("Dumped texture bytes (" + lastTextureData.length/1024 + "KB): " + lastTextureName);
				lastTextureUsed = 0;
				lastTextureData = null;
				lastTextureName = null;
			}
		}
	}

}
