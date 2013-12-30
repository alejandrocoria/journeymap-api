package net.techbrew.mcjm.render.texture;


import net.minecraft.client.renderer.texture.TextureUtil;
import net.techbrew.mcjm.io.RegionImageHandler;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

/**
 * Created by mwoodman on 12/22/13.
 */
public class DelayedTexture {

    Integer glId;
    BufferedImage image;

    final int width;
    final int height;
    final ByteBuffer buffer;

    /**
     * Can be safely called without the OpenGL Context.
     * @param glId
     * @param image
     */
    public DelayedTexture(Integer glId, BufferedImage image, String debugString)
    {
        this.glId = glId;
        this.image = image;
        width = image.getWidth();
        height = image.getHeight();

        if(debugString!=null) {
            Graphics2D g = RegionImageHandler.initRenderingHints(image.createGraphics());
            g.setPaint(Color.WHITE);
            g.setStroke(new BasicStroke(3));
            g.drawRect(0, 0, image.getWidth(), image.getHeight());
            final Font labelFont = new Font("Arial", Font.BOLD, 16);
            g.setFont(labelFont); //$NON-NLS-1$
            g.drawString(debugString, 16, 16);
            g.dispose();
        }

        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);
        buffer = BufferUtils.createByteBuffer(width * height * 4);
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                int pixel = pixels[y * width + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
                buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
                buffer.put((byte) (pixel & 0xFF));             // Blue component
                buffer.put((byte) ((pixel >> 24) & 0xFF));     // Alpha component
            }
        }
        buffer.flip();
    }

    /**
     * Must be called on same thread as OpenGL Context
     * @return
     */
    public TextureImpl bindTexture() {
        if(glId==null) {
            glId = TextureUtil.glGenTextures();
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, glId);

        // Setup wrap mode
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

        //Setup texture scaling filtering
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        //Send texel data to OpenGL
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        return new TextureImpl(glId, width, height);
    }

//    private static void setTextureClamped(boolean par0)
//    {
//        if (par0)
//        {
//            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
//            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
//        }
//        else
//        {
//            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
//            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
//        }
//    }
//
//    private static void setTextureBlurred(boolean par0)
//    {
//        if (par0)
//        {
//            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
//            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
//        }
//        else
//        {
//            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
//            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
//        }
//    }
//
//    class Stripe {
//        int yOffset;
//        int width;
//        int height;
//        IntBuffer data;
//
//        Stripe(int yOffset, int width, int height, IntBuffer data) {
//            this.yOffset = yOffset;
//            this.width = width;
//            this.height = height;
//            this.data = data;
//        }
//    }
}
