package net.techbrew.mcjm.render.texture;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.ResourceManager;
import net.techbrew.mcjm.log.StatTimer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class TextureImpl extends AbstractTexture {
	
    /** width of this icon in pixels */
    public final int width;

    /** height of this icon in pixels */
    public final int height;
    
    /** keep image with object */
    public final boolean retainImage;
    
    /** optionally-retained image **/
    protected BufferedImage image;
    
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

    TextureImpl(int glId, BufferedImage image)
    {
        this.glTextureId = glId;
        this.retainImage = true;
        this.width = image.getWidth();
        this.height = image.getHeight();
    }

    protected void updateTexture(BufferedImage image, boolean allocateMemory)
    {
    	if(image.getWidth()!=width || image.getHeight()!=height) {
    		throw new IllegalArgumentException("Image dimensions don't match");
    	}
    	if(retainImage) this.image = image;
        if(allocateMemory) {
            TextureUtil.uploadTextureImage(getGlTextureId(), image);
        } else {

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, getGlTextureId());
            uploadTextureImageSubImpl(image, 0, 0, false, false);

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

    private static final IntBuffer dataBuffer = GLAllocation.createDirectIntBuffer(4194304);

    private static void uploadTextureImageSubImpl(BufferedImage image, int par1, int par2, boolean par3, boolean par4)
    {
        int var5 = image.getWidth();
        int var6 = image.getHeight();
        int var7 = 4194304 / var5;
        int[] var8 = new int[var7 * var5];


        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4); //4 for RGBA, 3 for RGB

        for(int y = 0; y < image.getHeight(); y++){
            for(int x = 0; x < image.getWidth(); x++){
                int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
                buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
                buffer.put((byte) (pixel & 0xFF));               // Blue component
                buffer.put((byte) ((pixel >> 24) & 0xFF));    // Alpha component. Only for RGBA
            }
        }

        buffer.flip(); //FOR THE LOVE OF GOD DO NOT FORGET THIS

        setTextureBlurred(par3);
        setTextureClamped(par4);
        StatTimer timer = StatTimer.get("TextureImpl.updateTexture.bind");
        timer.start();
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        timer.stop();
    }

    private static void copyToBuffer(int[] par0ArrayOfInteger, int par1)
    {
        copyToBufferPos(par0ArrayOfInteger, 0, par1);
    }

    private static void copyToBufferPos(int[] par0ArrayOfInteger, int par1, int par2)
    {
        int[] var3 = par0ArrayOfInteger;

        dataBuffer.clear();
        dataBuffer.put(var3, par1, par2);
        dataBuffer.position(0).limit(par2);
    }

    private static void setTextureClamped(boolean par0)
    {
        if (par0)
        {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        }
        else
        {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        }
    }

    private static void setTextureBlurred(boolean par0)
    {
        if (par0)
        {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        }
        else
        {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        }
    }

	@Override
	public void loadTexture(ResourceManager par1ResourceManager) throws IOException {}
}
