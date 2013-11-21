package net.techbrew.mcjm.render.texture;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.src.AbstractTexture;
import net.minecraft.src.ResourceManager;
import net.minecraft.src.TextureUtil;
import net.techbrew.mcjm.JourneyMap;

import org.lwjgl.opengl.GL11;

public class TextureImpl extends AbstractTexture {
	
	static Set<Integer> texIds = Collections.synchronizedSet(new HashSet<Integer>());

    /** width of this icon in pixels */
    public final int width;

    /** height of this icon in pixels */
    public final int height;
    
    /** keep image with object */
    public final boolean retainImage;
    
    /** optionally-retained image **/
    private BufferedImage image;
    
    TextureImpl(BufferedImage image) {
    	this(image, false);
    }
    
    TextureImpl(BufferedImage image, boolean retainImage)
    {    	    	
    	this.retainImage = retainImage;
    	this.width = image.getWidth();
        this.height = image.getHeight();
        updateTexture(image);
        
        if(texIds.contains(glTextureId)) {
        	JourneyMap.getLogger().severe("*** WARNING!  Texture ID has already been used: " + glTextureId);
        } else {
        	texIds.add(glTextureId);
        }
        
    }    

    public void updateTexture(BufferedImage image)
    {
    	if(image.getWidth()!=width || image.getHeight()!=height) {
    		throw new IllegalArgumentException("Image dimensions don't match");
    	}
    	if(retainImage) this.image = image;
        TextureUtil.uploadTextureImage(getGlTextureId(), image);
    }
    
    public boolean hasImage() {
    	return image!=null;
    }
    
    public BufferedImage getImage() {
    	return image;
    }

	public void deleteTexture() {
		if(this.glTextureId!=-1) {
			GL11.glDeleteTextures(this.getGlTextureId());
		}
		if(this.image!=null) {
			this.image = null;
		}
	}

	@Override
	public void loadTexture(ResourceManager par1ResourceManager) throws IOException {}
}
