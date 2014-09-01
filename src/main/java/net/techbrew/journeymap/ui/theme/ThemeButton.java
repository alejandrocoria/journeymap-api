package net.techbrew.journeymap.ui.theme;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Mark on 8/30/2014.
 */
public class ThemeButton extends net.techbrew.journeymap.ui.Button
{
    private final TextureImpl textureOn;
    private final TextureImpl textureOff;
    private final TextureImpl textureDisabled;
    private final TextureImpl textureIcon;
    private final Style style;

    public ThemeButton(Enum enumId, Style style, String iconName)
    {
        this(enumId.ordinal(), "", "", false, style, iconName);
    }


    public ThemeButton(Enum enumId, String label, Style style, String iconName)
    {
        this(enumId.ordinal(), label, label, false, style, iconName);
    }

    public ThemeButton(int id, String label, Style style, String iconName)
    {
        this(id, label, label, false, style, iconName);
    }

    public ThemeButton(Enum enumId, String labelOn, String labelOff, boolean toggled, Style style, String iconName)
    {
        this(enumId.ordinal(), labelOn, labelOff, toggled, style, iconName);
    }

    public ThemeButton(int id, String labelOn, String labelOff, boolean toggled, Style style, String iconName)
    {
        super(id, 20, 20, toggled ? labelOn : labelOff);

        String themeSetName = JourneyMap.getCoreProperties().themeName.get();

        String styleName = style.name().toLowerCase();
        String pathPattern = "button/%s_%s.png";
        TextureCache tc = TextureCache.instance();

        this.style = style;
        textureOn = tc.getThemeTexture(themeSetName, String.format(pathPattern, styleName, "on"));
        textureOff = tc.getThemeTexture(themeSetName, String.format(pathPattern, styleName, "off"));
        textureDisabled = tc.getThemeTexture(themeSetName, String.format(pathPattern, styleName, "disabled"));
        textureIcon = tc.getThemeTexture(themeSetName, String.format("icon/%s.png", iconName));

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

        // Check hover
        this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
        int k = this.getHoverState(this.field_146123_n);
        boolean isMouseOver = this.field_146123_n;

        TextureImpl activeTexture = null;
        if (this.isEnabled())
        {
            if (style == Style.Button)
            {
                activeTexture = textureOff;
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

        float scale;
        int drawX = getX();
        int drawY = getY();
        //setWidth(activeTexture.width);
        //setHeight(activeTexture.height);

        boolean useThemeButton = true;

        if (useThemeButton)
        {
            // Theme Button Background
            scale = width / (activeTexture.width * 1F);
            DrawUtil.drawImage(activeTexture, drawX, drawY, false, scale, 0);
        }
        else
        {
            // Resourcepack Button Background
            String label = this.displayString;
            this.displayString = "";
            this.height = 20;
            this.width = 20;
            scale = (this.width - 2) / (textureIcon.width * 1.0F);
            super.drawButton(minecraft, mouseX, mouseY);
            this.displayString = label;
        }

        // Icon
        scale = scale * .8f;
        drawX += ((width - (textureIcon.width * scale)) / 2);
        drawY += ((height - (textureIcon.height * scale)) / 2);
        DrawUtil.drawImage(textureIcon, drawX, drawY, false, scale, 0);

        if (!useThemeButton)
        {
            DrawUtil.drawColoredImage(textureIcon, 255, Color.black, drawX + .5, drawY + .5, scale, 0);
        }
        showDisabledHoverText = true;
        Color iconColor = isMouseOver && isEnabled() ? Color.white : Color.lightGray;
        DrawUtil.drawColoredImage(textureIcon, 255, iconColor, drawX, drawY, scale, 0);
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
