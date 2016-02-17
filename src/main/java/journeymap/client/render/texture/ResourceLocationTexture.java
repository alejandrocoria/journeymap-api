package journeymap.client.render.texture;

import com.google.common.cache.*;
import journeymap.client.JourneymapClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * TextureImpl with a resource location as the image source.
 * <p/>
 * TODO:  Look at Dynamic Texture to see if the data copy is more efficient
 */
public class ResourceLocationTexture extends TextureImpl
{
    private final boolean isExternallyBound;

    /**
     * Create a texture using an image found at location.
     *
     * @param location        Location where image resides
     * @param retainImage     Whether to keep the buffered image in memory
     * @param bindImmediately Whether to bind immediately. If true, this must be called on the main GL thread.
     */
    public ResourceLocationTexture(ResourceLocation location, boolean retainImage, boolean bindImmediately)
    {
        super(null, resolveImage(location), retainImage, false);

        // Check to see if TextureManager already has this - avoid binding it twice
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        ITextureObject existingTexture = textureManager.getTexture(location);
        if (existingTexture != null)
        {
            this.glTextureId = existingTexture.getGlTextureId();
            this.isExternallyBound = true;
        }
        else
        {
            this.isExternallyBound = false;
            if (bindImmediately)
            {
                bindTexture();
            }
        }
    }

    /**
     * Gets a buffered image from the resource location
     *
     * @param location location
     * @return image
     */
    private static BufferedImage resolveImage(ResourceLocation location)
    {
        IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
        try
        {
            InputStream is = resourceManager.getResource(location).getInputStream();
            if (is == null)
            {
                throw new IOException("Resource not found: " + location);
            }
            return TextureUtil.readBufferedImage(is);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Build a cache for ResourceLocationTextures.  Weak values
     * let textures get unbound when no longer used.
     *
     * @return
     */
    public static LoadingCache<ResourceLocation, TextureImpl> createCache()
    {
        CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
        builder.concurrencyLevel(1);
        if (JourneymapClient.getCoreProperties().recordCacheStats.get())
        {
            builder.recordStats();
        }

        builder.weakValues().removalListener(new RemovalListener<ResourceLocation, TextureImpl>()
        {
            /**
             * Only delete texture if Minecraft's TextureManager doesn't own it already.
             * @param notification removal
             */
            @Override
            public void onRemoval(@Nonnull RemovalNotification<ResourceLocation, TextureImpl> notification)
            {
                TextureImpl tex = notification.getValue();
                if (tex != null)
                {
                    if (tex instanceof ResourceLocationTexture)
                    {
                        ResourceLocationTexture resourceLocationTexture = (ResourceLocationTexture) tex;
                        if (resourceLocationTexture.isExternallyBound)
                        {
                            return;
                        }
                    }

                    tex.queueForDeletion();
                }
            }
        });

        return builder.build(
                new CacheLoader<ResourceLocation, TextureImpl>()
                {
                    public TextureImpl load(ResourceLocation key) throws Exception
                    {
                        return new ResourceLocationTexture(key, true, false);
                    }
                });
    }
}
