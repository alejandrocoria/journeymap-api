package net.techbrew.journeymap.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;

/**
 * Created by Mark on 8/30/2014.
 */
public class IconButton extends Button
{
    private final TextureImpl textureOn;
    private final TextureImpl textureOff;
    private final TextureImpl textureDisabled;
    private final TextureImpl textureIcon;
    private final Style style;

    public IconButton(Enum enumId, Style style, String iconName)
    {
        this(enumId.ordinal(), "", "", false, style, iconName);
    }


    public IconButton(Enum enumId, String label, Style style, String iconName)
    {
        this(enumId.ordinal(), label, label, false, style, iconName);
    }

    public IconButton(int id, String label, Style style, String iconName)
    {
        this(id, label, label, false, style, iconName);
    }

    public IconButton(Enum enumId, String labelOn, String labelOff, boolean toggled, Style style, String iconName)
    {
        this(enumId.ordinal(), labelOn, labelOff, toggled, style, iconName);
    }

    public IconButton(int id, String labelOn, String labelOff, boolean toggled, Style style, String iconName)
    {
        super(id, 32, 32, toggled ? labelOn : labelOff);

        String skinSetName = JourneyMap.getCoreProperties().skinIconSetName.get();

        String styleName = style.name().toLowerCase();
        String pathPattern = "button/%s_%s.png";
        TextureCache tc = TextureCache.instance();

        this.style = style;
        textureOn = tc.getUiSkinTexture(skinSetName, String.format(pathPattern, styleName, "on"));
        textureOff = tc.getUiSkinTexture(skinSetName, String.format(pathPattern, styleName, "off"));
        textureDisabled = tc.getUiSkinTexture(skinSetName, String.format(pathPattern, styleName, "disabled"));
        textureIcon = tc.getUiSkinTexture(skinSetName, String.format("icon/%s.png", iconName));

        if (style == Style.Button)
        {
            setToggled(false, false);
        }
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY)
    {
        if (!isDrawButton())
        {
            return;
        }

        TextureImpl activeTexture = null;
        if (this.isEnabled())
        {
            if (style == Style.Button)
            {
                activeTexture = Mouse.isButtonDown(0) && mouseOver(mouseX, mouseY) ? textureOn : textureOff;
            }
            else
            {
                activeTexture = this.toggled ? textureOn : textureOff;
            }
        }
        else
        {
            activeTexture = this.textureDisabled;
        }

        float scale = width / (activeTexture.width * 1F);


        int drawX = getX();
        int drawY = getY();

        boolean useResourcePackButton = false;

        if (useResourcePackButton)
        {
            String label = this.displayString;
            this.displayString = "";
            this.height = 20;
            super.drawButton(minecraft, mouseX, mouseY);
            this.displayString = label;
        }
        else
        {
            // Button Background
            DrawUtil.drawImage(activeTexture, drawX, drawY, false, scale, 0);
        }

        // Icon
        drawX += ((width - (textureIcon.width * scale)) / 2);
        drawY += ((height - (textureIcon.height * scale)) / 2);
        DrawUtil.drawImage(textureIcon, drawX, drawY, false, scale, 0);
    }

    @Override
    public ArrayList<String> getTooltip()
    {
        ArrayList<String> list = super.getTooltip();
        list.add(0, EnumChatFormatting.DARK_PURPLE + displayString);
        return list;
    }

    public enum Style
    {
        Button, Toggle
    }
}
