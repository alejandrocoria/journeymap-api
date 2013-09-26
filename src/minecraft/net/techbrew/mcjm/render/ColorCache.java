package net.techbrew.mcjm.render;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;

import javax.imageio.ImageIO;

import net.minecraft.src.Block;
import net.minecraft.src.BlockGrass;
import net.minecraft.src.BlockLeaves;
import net.minecraft.src.BlockLilyPad;
import net.minecraft.src.BlockSand;
import net.minecraft.src.BlockTallGrass;
import net.minecraft.src.BlockVine;
import net.minecraft.src.Minecraft;
import net.minecraft.src.TextureAtlasSprite;
import net.minecraft.src.ResourceLocation;
import net.minecraft.src.ResourcePack;
import net.minecraft.src.ResourcePackFileNotFoundException;
import net.minecraft.src.ResourcePackRepository;
import net.minecraft.src.ResourcePackRepositoryEntry;
import net.minecraft.src.Item;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.BiomeGenBase;
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
	volatile long lastTextureUsed;
	
	public ColorCache() {
		texturePackList = Minecraft.getMinecraft().getResourcePackRepository();
		useCustomTexturePack = PropertyManager.getInstance().getBoolean(PropertyManager.Key.USE_CUSTOM_TEXTUREPACK);
		validateResourcePack();
	}
	

	public Color getBlockColor(ChunkStub chunkStub, BlockInfo blockInfo, int x, int y, int z) {
		
		Color color = blockInfo.getColor();
		
		if(color==null) {
			
			Block block = blockInfo.getBlock();
			BiomeGenBase biome = chunkStub.getBiomeGenForWorldCoords(x, z, chunkStub.worldObj.getWorldChunkManager());		
			
			if(block instanceof BlockLeaves || block instanceof BlockVine) {
				
				String key = blockInfo.hashCode() + biome.biomeName;
				color = foliageBiomeColors.get(key);
				if(color==null) {
					color = colorMultiplier(getBasicBlockColor(blockInfo), biome.getBiomeFoliageColor());
					//color = new Color(block.colorMultiplier(chunkStub.worldObj, x, y, z));
					foliageBiomeColors.put(key, color);				
				}
				
			} else if(block instanceof BlockGrass || block instanceof BlockTallGrass) {
	
				color = grassBiomeColors.get(biome);
				if(color==null) {
					color = colorMultiplier(getBasicBlockColor(blockInfo), biome.getBiomeGrassColor());
					//color = tint(getBasicBlockColor(blockInfo), block.colorMultiplier(chunkStub.worldObj, x, y, z));
					//color = average(getBasicBlockColor(blockInfo), new Color(block.colorMultiplier(chunkStub.worldObj, x, y, z)));
					grassBiomeColors.put(biome, color);
				}
				
			} else if(block==Block.waterStill || block==Block.waterMoving) {
				
				color = waterBiomeColors.get(biome);
				if(color==null) {
					color = colorMultiplier(getBasicBlockColor(blockInfo), biome.waterColorMultiplier);
					waterBiomeColors.put(biome, color);
				}
				
			} else if(block instanceof BlockLilyPad) {
				
				color = colorMultiplier(getBasicBlockColor(blockInfo), block.getBlockColor());
	
			} else {
				
				int rc = block.getRenderColor(blockInfo.meta);
				if(rc!=16777215 && rc!=0) {
					color = new Color(rc);
				} else {			
					color = getBasicBlockColor(blockInfo);
				}
			}
		}
		return color;
	}
	
	/**
	 * Gets the color for the block from the cache, or
	 * lazy-loads it first.
	 * @param blockInfo
	 * @return
	 */
	private Color getBasicBlockColor(BlockInfo blockInfo) {

		// Check if the resourcepack has changed
		if(lastTextureUsed+5000<System.currentTimeMillis()) {
			validateResourcePack();	    	
		}
		
		Color color = blockInfo.getColor();
		if(color==null) {		
			color = colors.get(blockInfo);			
		}
		if(color==null) {
			color = loadBlockColor(blockInfo);
		}
		if(color==null) {
			JourneyMap.getLogger().warning("\tColor null for " + stringInfo(blockInfo));
			color = Color.black;
		}
		blockInfo.setColor(color);
			
		return color;		
	}
		
	
	/**
	 * Derive block color from the corresponding texture.
	 * @param blockInfo
	 * @return
	 */
	protected Color loadBlockColor(BlockInfo blockInfo) {
		
		Color color = null;
		
		try {
						
			JourneyMap.getLogger().fine("Loading color for " + stringInfo(blockInfo));
			
        	//double loadStart = System.nanoTime();    
        	
	        Block block = Block.blocksList[blockInfo.id];

            if (block == null) {
            	JourneyMap.getLogger().warning("No block type found");
            	
            	ItemBlock item = (ItemBlock) Item.itemsList[blockInfo.id];
            	if(item!=null) {
            		JourneyMap.getLogger().warning("ItemBlocks not supported: " + item.itemID + ":" + item.getMetadata(0) + " " + item.getUnlocalizedName());
            	}

            } else {
            	int side = MapBlocks.side2Textures.contains(block.blockID) ? 2 : 1;
	            TextureAtlasSprite blockIcon = (TextureAtlasSprite) block.getIcon(side, blockInfo.meta);
	        	if(blockIcon==null) {
	        		JourneyMap.getLogger().warning("No TextureAtlasSprite for " + stringInfo(blockInfo));
	        	} else {
	        		color = getColorForIcon(blockInfo, blockIcon);  
	        	}
            }
        	
        	if(color==null){
        		color = Color.black;
        	}
        	
			if(color.equals(Color.black)) {
				JourneyMap.getLogger().warning("\tColor eval'd to black for " + stringInfo(blockInfo));
			}
             
			// Put the color in the map
			colors.put(blockInfo, color);
		
			// Time the whole thing
//            double loadStop = System.nanoTime();   
//            double timer = (loadStop-loadStart)/1000000f;            
//            JourneyMap.getLogger().fine("\tColor load time: " + timer + "ms");
			
			return color;                           

		} catch (Throwable t) {
			JourneyMap.getLogger().severe("Error getting color: " + LogFormatter.toString(t));
			return Color.black;
		}
	}
	
	InputStream getIconStream(ResourceLocation loc, ResourcePack resourcePack) {
		InputStream is = null;
		try {
        	is = resourcePack.getInputStream(loc);
        } catch(ResourcePackFileNotFoundException e) {
        	JourneyMap.getLogger().fine("ResourcePack doesn't have icon for " + loc);
        } catch(IOException e) {
        	JourneyMap.getLogger().severe("Can't get ResourcePack icon for " + loc + ": " + LogFormatter.toString(e));
        }
		return is;
	}
	
	Color getColorForIcon(BlockInfo blockInfo, TextureAtlasSprite icon) {
		
		ResourceLocation loc = new ResourceLocation("textures/blocks/" + icon.getIconName() + ".png");
		ResourcePack rp = texturePack;
        Color color = null;
        BufferedImage img = null;
        InputStream imgIs = getIconStream(loc, rp);
        if(imgIs==null && rp!=texturePackList.rprDefaultResourcePack) {
        	rp = texturePackList.rprDefaultResourcePack;
        	imgIs = getIconStream(loc, rp);
        }
        
        int x=0,y=0;
    	int width = 0;
    	int height = 0;
    	
        if(imgIs==null) {
        	JourneyMap.getLogger().warning("Couldn't access texture for " + stringInfo(blockInfo) + " at " + loc.getResourcePath());        	
        } else {
        	
	        try {
				img = ImageIO.read(imgIs);
				
				width = icon.getIconWidth();
				height = icon.getIconHeight();
		    	
		        int count = 0;
		        int argb, alpha;
		    	int a=0, r=0, g=0, b=0;
		        outer: for(x=0; x<width; x++) {
		        	for(y=0; y<height; y++) {
		        		try {
		        			argb = img.getRGB(x, y);
		        		} catch(Throwable e) {
		        			JourneyMap.getLogger().severe("Couldn't get RGB from texture at " + x + "," + y + " for " + loc.getResourcePath());
		        			JourneyMap.getLogger().severe(LogFormatter.toString(e));
		        			break outer;
		        		}
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
		        	JourneyMap.getLogger().warning("Unusable texture for " + stringInfo(blockInfo));
		        	r = g = b = 0;
		        }
				
		        // Set color
		        color = new Color(r,g,b);
				
		        // Determine alpha
		        float blockAlpha = 0f;		
		        if(MapBlocks.alphas.containsKey(blockInfo.id)) {
		        	blockAlpha = MapBlocks.alphas.get(blockInfo.id);
		        	//JourneyMap.getLogger().info("Using transparency for " + stringInfo(blockInfo) + ": " + blockAlpha);
				} else if(blockInfo.getBlock().getRenderBlockPass()>0) {
					blockAlpha = a * 1.0f/255;
					MapBlocks.alphas.put(blockInfo.id, blockAlpha);
					// JourneyMap.getLogger().info("Setting transparency for " + stringInfo(blockInfo) + ": " + blockAlpha);					
				}
		        blockInfo.setAlpha(blockAlpha);
		        
								
			} catch (Throwable e1) {				
				if(x<width || y<height)
				JourneyMap.getLogger().warning("Error deriving color from texture " + loc.getResourcePath());
				JourneyMap.getLogger().severe(LogFormatter.toString(e1));
			} 
        }

        if(color==null) {
        	JourneyMap.getLogger().warning("Defaulted to black for " + loc.getResourcePath());
        	color = Color.black;
        } else {
    		JourneyMap.getLogger().fine("Derived color for " + loc.getResourcePath() + ": " + Integer.toHexString(color.getRGB()));
        }
        
        return color;
	}
	
	void validateResourcePack() {
		// Check if the resourcepack has changed
    	ResourcePack currentPack = null;
    	if(useCustomTexturePack && texturePackList.getRepositoryEntries().size()>0) {
			ResourcePackRepositoryEntry rpre = (ResourcePackRepositoryEntry) texturePackList.getRepositoryEntries().get(0);
			currentPack = rpre.getResourcePack();
		} else {
			currentPack = texturePackList.rprDefaultResourcePack; // DefaultResourcePack
		}
    	if(texturePack!=currentPack) {
    		texturePack = currentPack;
    		grassBiomeColors.clear();
    		waterBiomeColors.clear();
    		foliageBiomeColors.clear();				
    		colors.clear();    				
    		colors.put(new BlockInfo(0,0), new Color(0x000000)); // air    		
    		MapBlocks.resetAlphas();
    		JourneyMap.getLogger().info("Deriving block colors from ResourcePack: " + texturePack.getPackName());
    	} 
    	lastTextureUsed = System.currentTimeMillis();
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
	

	static Color colorMultiplier(Color color, int mult) {
		int rgb = color.getRGB();
		
	    int alpha1 = rgb >> 24 & 0xFF;
	    int red1 = rgb >> 16 & 0xFF;
	    int green1 = rgb >> 8 & 0xFF;
	    int blue1 = rgb >> 0 & 0xFF;

	    int alpha2 = mult >> 24 & 0xFF;
	    int red2 = mult >> 16 & 0xFF;
	    int green2 = mult >> 8 & 0xFF;
	    int blue2 = mult >> 0 & 0xFF;

	    int alpha = alpha1 * alpha2 / 255;
	    int red = red1 * red2 / 255;
	    int green = green1 * green2 / 255;
	    int blue = blue1 * blue2 / 255;

	    return new Color((alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF);
	}

	static float safeColor(float original) {
		return Math.min(1F, (Math.max(0F, original)));
	}
	
	static int safeColor(int original) {
		return Math.max(0, (Math.min(255, original)));
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
	

}
