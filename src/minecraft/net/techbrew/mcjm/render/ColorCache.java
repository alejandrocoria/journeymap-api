package net.techbrew.mcjm.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.HashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.src.BiomeGenBase;
import net.minecraft.src.Block;
import net.minecraft.src.ITexturePack;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.Texture;
import net.minecraft.src.TexturePackDefault;
import net.minecraft.src.TexturePackList;
import net.minecraft.src.TextureStitched;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.ChunkStub;
import net.techbrew.mcjm.model.TextureStitchedStub;

/**
 * Cache of block colors derived from the current texture pack.
 * @author mwoodman
 *
 */
public class ColorCache {
	
	final Color grassOffsetColor = new Color(0x111111);
	final HashMap<BlockInfo, Color> colors = new HashMap<BlockInfo, Color>(256);
	final HashMap<BiomeGenBase, Color> grassBiomeColors = new HashMap<BiomeGenBase, Color>(16);
	final HashMap<BiomeGenBase, Color> waterBiomeColors = new HashMap<BiomeGenBase, Color>(16);
	final HashMap<String, Color> foliageBiomeColors = new HashMap<String, Color>(16);
	final HashMap<BlockInfo, Float> partialTransparencies = new HashMap<BlockInfo, Float>(16);
	
	final boolean useCustomTexturePack;
	final TexturePackList texturePackList;
	ITexturePack texturePack;
	
	String lastTextureName = null;
	BufferedImage lastTexture;
	
	long loadTimes = 0;
	
	public ColorCache() {
		texturePackList = Minecraft.getMinecraft().texturePackList;
		useCustomTexturePack = PropertyManager.getInstance().getBoolean(PropertyManager.Key.USE_CUSTOM_TEXTUREPACK);
		init();
	}
	
	/**
	 * Reset the cache colors and use the selected texture pack.
	 */
	void init() {
		
		if(useCustomTexturePack) {
			texturePack = texturePackList.getSelectedTexturePack();
		} else {
			texturePack = new TexturePackDefault();
		}
		
		JourneyMap.getLogger().info("Deriving block colors from texture pack: " + texturePack.getTexturePackID());
		
		loadTimes = 0;
		lastTextureName = null;
		lastTexture = null;
		grassBiomeColors.clear();
		waterBiomeColors.clear();
		foliageBiomeColors.clear();		
		
		colors.clear();
		colors.put(new BlockInfo(0,0), new Color(0x000000)); // air
		colors.put(new BlockInfo(8,0), new Color(0x112299)); // water 
		colors.put(new BlockInfo(9,0), new Color(0x112299)); // stationary water 
		
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
		
		if(block==Block.leaves) {
			color = getFoliageColor(biome, blockInfo);
		} else if(block==Block.grass || block==Block.tallGrass) {
			color = getGrassColor(biome, blockInfo);
		} else if(block==Block.waterStill || block==Block.waterMoving) {
			color = getWaterColor(biome);
		} else {
			color = getBlockColor(blockInfo);
		}


		return color;
	}
	
	/**
	 * Gets the color for the block from the cache, or
	 * lazy-loads it first.
	 * @param blockInfo
	 * @return
	 */
	public Color getBlockColor(BlockInfo blockInfo) {
		
		if(blockInfo.color!=null) {
			return blockInfo.color;
		}

		// Check for changed texturepack
		if(useCustomTexturePack && texturePackList.getSelectedTexturePack()!=texturePack) {
			init();
		}
		
		Color color = colors.get(blockInfo);
		if(color==null) {
			color = loadBlockColor(blockInfo);
			if(color==null) {
				color = Color.black;
			}
			//colors.put(blockInfo, color);
		}
		return color;		
	}
	
	/**
	 * Gets the color for grass in the biome.  Lazy-loads
	 * the result into a map for faster access.
	 * 
	 * @param biome
	 * @return
	 */
	public Color getGrassColor(BiomeGenBase biome, BlockInfo blockInfo) {
		
		Color color = grassBiomeColors.get(biome);
		if(color==null) {
			//color = multiply(getBlockColor(blockInfo),  biome.getBiomeGrassColor());
			//color = new Color(biome.getBiomeGrassColor());
			color = average(grassOffsetColor, new Color(biome.getBiomeGrassColor()));
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
		Color color = foliageBiomeColors.get(biome.biomeName + blockInfo.hashCode());
		if(color==null) {
			color = multiply2(getBlockColor(blockInfo),  biome.getBiomeFoliageColor());
			//color = new Color(biome.getBiomeFoliageColor());
			//color = blend(getBlockColor(blockInfo), biome.getBiomeFoliageColor());
			foliageBiomeColors.put(biome.biomeName + blockInfo.hashCode(), color);
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
			color = blend(colors.get(new BlockInfo(8,0)), biome.waterColorMultiplier);
			waterBiomeColors.put(biome, color);
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

	        Block block = Block.blocksList[blockInfo.id];

            if (block == null) {
            	JourneyMap.getLogger().warning("No block type found for " + blockInfo);
            	return null;
            }
            
            // Find out which angle icon to use
            int side = 0;//block.renderAsNormalBlock() ? 0 : 1;
            
        	TextureStitched blockIcon = (TextureStitched) block.getIcon(side, blockInfo.meta);
        	if(blockIcon==null && side>0) {
        		JourneyMap.getLogger().warning("No side icon for " + block.getUnlocalizedName2() + "("+ blockInfo + ")");
        		blockIcon = (TextureStitched) block.getIcon(0, blockInfo.meta);
        	}
        	
        	if(blockIcon==null) {
        		JourneyMap.getLogger().warning("No top icon for " + block.getUnlocalizedName2() + "("+ blockInfo + ")");
        		return null;
        	}
        	
        	TextureStitchedStub icon = new TextureStitchedStub(blockIcon);	 
        	int width = icon.getWidth();
        	int height = icon.getHeight();
        	Texture texSheet = icon.getTextureSheet();	       
        	
        	if(texSheet==null) {
        		JourneyMap.getLogger().warning("No Texture for " + block.getUnlocalizedName2() + "("+ blockInfo + ")");
        		return null;
        	}
        	
        	// This is here just in case a texture can have more than one sheet for blocks
        	// If the texture sheet has changed, will need a new bufferedimage
        	if(!texSheet.getTextureName().equals(lastTextureName)) {
        		lastTextureName = texSheet.getTextureName();
        		lastTexture = null;
        		JourneyMap.getLogger().info("Using texture: " + lastTextureName);
        	}
        	
        	// Keep texture in bufferedimage, trading memory for performance
        	if(lastTexture==null) {        	
        		int sheetWidth = texSheet.getWidth();
        		int sheetHeight = texSheet.getHeight();
            	lastTexture = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
                ByteBuffer buffer = texSheet.getTextureData();
                byte[] bytes = new byte[sheetWidth * sheetHeight * 4];
                buffer.position(0);
                buffer.get(bytes);

                for (int x = 0; x < sheetWidth; ++x)
                {
                    for (int y = 0; y < sheetHeight; ++y)
                    {
                        lastTexture.setRGB(x, y, getARGBfromArray(bytes, x, y, sheetWidth));
                    }
                }
        	}
        	
        	long loadStart = System.currentTimeMillis();        	            
            Color color = getColorForIcon(blockInfo, blockIcon);            
            long loadStop = System.currentTimeMillis();
            
            loadTimes += (loadStop-loadStart);
            
            if(colors.size() % 10 == 0) {
            	JourneyMap.getLogger().info("Average color load time: " + (loadTimes*1f/(colors.size()-3)) + "ms");
            }
            
			// Put the color in the map
			colors.put(blockInfo, color);
			if(color.equals(Color.black)) {
				JourneyMap.getLogger().warning("Black color for " + block.getUnlocalizedName2() + "("+ blockInfo + ")");
			}
			
			return color;                           

		} catch (Throwable t) {
			JourneyMap.getLogger().severe("Error getting color for " + blockInfo + ": " + LogFormatter.toString(t));
			return null;
		}
	}
	
	Color getColorForIcon(BlockInfo blockInfo, TextureStitched blockIcon) {

		TextureStitchedStub icon = new TextureStitchedStub(blockIcon);
		int width = icon.getWidth();
		int height = icon.getHeight();
		Texture texSheet = icon.getTextureSheet();	
		

        
    	// Create a bufferedimage for just the block texture
        BufferedImage blockImage = new BufferedImage(width, height, 2);
        blockImage.getGraphics().drawImage(lastTexture, 0, 0, width, height, blockIcon.getOriginX(), blockIcon.getOriginY(), blockIcon.getOriginX()+width,  blockIcon.getOriginY()+height, null);
			
        // TODO: Track percentage of pixels that aren't transparent, use that as factor when multiplying biome/grass/foliage color
        
        int count = 0;
        int alpha;
        int argb;
        int a=0, r=0, g=0, b=0;
        for(int x=0; x<width; x++) {
        	for(int y=0; y<height; y++) {
        		argb = blockImage.getRGB(x, y);
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
        	JourneyMap.getLogger().warning("Unusable texture for " + blockInfo.getBlock().getUnlocalizedName2() + "("+ blockInfo + ")");
        }
        
        Color color = new Color(r,g,b);
        
		// Put the alpha in MapBlocks
		if(blockInfo.getBlock().getRenderBlockPass()>0){
			float blockAlpha = a * 1.0f/255;
			MapBlocks.alphas.put(blockInfo.id, blockAlpha);
			JourneyMap.getLogger().info("Setting transparency for " + blockInfo.getBlock().getUnlocalizedName2() + ": " + blockAlpha);
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
	
	static Color multiply(Color original, int multiplier) {
		float[] rgba = original.getComponents(null);
		
		float r = rgba[0] * ((multiplier >> 16 & 0xFF) * 0.003921569F);
		float g = rgba[1] * ((multiplier >> 8 & 0xFF) * 0.003921569F);
		float b = rgba[2] * ((multiplier >> 0 & 0xFF) * 0.003921569F);

		return new Color(r,g,b); 
	}
	
	static Color multiply2(Color original, int multiplier) {
		float[] rgba = original.getComponents(null);
		
        float r = rgba[0] * (float)(multiplier >> 16 & 255) / 255.0F * 0.9F;
        float g = rgba[1] * (float)(multiplier >> 8 & 255) / 255.0F * 0.9F;
        float b = rgba[2] * (float)(multiplier & 255) / 255.0F * 0.9F;

		return new Color(r,g,b); 
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
