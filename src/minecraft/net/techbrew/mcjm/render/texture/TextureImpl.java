package net.techbrew.mcjm.render.texture;

import java.awt.image.BufferedImage;
import java.io.IOException;

import net.minecraft.src.AbstractTexture;
import net.minecraft.src.ResourceManager;
import net.minecraft.src.TextureUtil;

import org.lwjgl.opengl.GL11;

public class TextureImpl extends AbstractTexture {
	
    /** width of this icon in pixels */
    public final int width;

    /** height of this icon in pixels */
    public final int height;
    
    /** keep image with object */
    public final boolean retainImage;
    
    /** optionally-retained image **/
    private BufferedImage image;
    
    public TextureImpl(BufferedImage image) {
    	this(image, false);
    }
    
    TextureImpl(BufferedImage image, boolean retainImage)
    {    	    	
    	this.retainImage = retainImage;
    	this.width = image.getWidth();
        this.height = image.getHeight();
        updateTexture(image, true);
    }    

    private void updateTexture(BufferedImage image, boolean allocateMemory)
    {
    	if(image.getWidth()!=width || image.getHeight()!=height) {
    		throw new IllegalArgumentException("Image dimensions don't match");
    	}
    	if(retainImage) this.image = image;
        if(allocateMemory) {
            TextureUtil.uploadTextureImage(getGlTextureId(), image);
        } else {
            TextureUtil.uploadTextureImageSub(getGlTextureId(), image, 0, 0, false, false);
        }
    }

    public void updateTexture(BufferedImage image)
    {
        updateTexture(image, false);
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
