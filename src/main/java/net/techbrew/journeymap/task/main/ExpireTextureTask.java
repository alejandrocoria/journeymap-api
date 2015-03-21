package net.techbrew.journeymap.task.main;

import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.render.texture.TextureImpl;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Ensure a texture is expired on the thread with OpenGL
 */
public class ExpireTextureTask implements IMainThreadTask
{
    private static final int MAX_FAILS = 5;
    private static String NAME = "Tick." + MappingMonitorTask.class.getSimpleName();
    private static Logger LOGGER = JourneyMap.getLogger();
    private final List<TextureImpl> textures;
    private volatile int fails;

    private ExpireTextureTask(TextureImpl texture)
    {
        textures = new ArrayList<TextureImpl>();
        textures.add(texture);
    }

    private ExpireTextureTask(Collection<TextureImpl> textureCollection)
    {
        this.textures = new ArrayList<TextureImpl>(textureCollection);
    }

    public static void queue(TextureImpl texture)
    {
        JourneyMap.getInstance().queueMainThreadTask(new ExpireTextureTask(texture));
    }

    public static void queue(Collection<TextureImpl> textureCollection)
    {
        JourneyMap.getInstance().queueMainThreadTask(new ExpireTextureTask(textureCollection));
    }

    @Override
    public IMainThreadTask perform(Minecraft mc, JourneyMap jm)
    {
        deleteTextures();
        if (!textures.isEmpty())
        {
            fails++;
            LOGGER.warn("ExpireTextureTask.perform() couldn't delete textures: " + textures.toArray() + ", fails: " + fails);
            if (fails <= MAX_FAILS)
            {
                return this;
            }
        }
        return null;
    }

    /**
     * Must be called on same thread as OpenGL Context
     */
    private boolean deleteTextures()
    {
        Iterator<TextureImpl> iter = textures.listIterator();
        while (iter.hasNext())
        {
            TextureImpl texture = iter.next();
            if (deleteTexture(texture))
            {
                iter.remove();
            }
            else
            {
                break;
            }
        }
        return textures.isEmpty();
    }

    private boolean deleteTexture(TextureImpl texture)
    {
        boolean success = false;
        if (texture.isBound())
        {
            try
            {
                if (Display.isCurrent())
                {
                    GL11.glDeleteTextures(texture.getGlTextureId());
                    //LOGGER.info("Successfully deleted " + texture);
                    texture.clear();
                    success = true;
                }
            }
            catch (LWJGLException t)
            {
                LOGGER.warn("Couldn't delete texture " + this + ": " + t);
                success = false;
            }
        }
        else
        {
            texture.clear();
            success = true;
        }

        return success;
    }

    @Override
    public String getName()
    {
        return NAME;
    }
}
