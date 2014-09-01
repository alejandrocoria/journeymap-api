package net.techbrew.journeymap.ui.theme;

import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;
import net.techbrew.journeymap.ui.Button;
import net.techbrew.journeymap.ui.ButtonList;
import scala.actors.threadpool.Arrays;

import java.util.ArrayList;

/**
 * Created by Mark on 8/30/2014.
 */
public class ThemeToolbar extends Button
{
    private final TextureImpl textureLeft;
    private final TextureImpl textureInner;
    private final TextureImpl textureRight;
    private final ButtonList buttonList;
    private final int hgap;
    private final int vgap;

    public ThemeToolbar(int id, Button... buttons)
    {
        this(id, new ButtonList(buttons), 3, 3);
    }

    public ThemeToolbar(int id, ButtonList buttonList, int hgap, int vgap)
    {
        super(id, 0, 0, "");
        this.buttonList = buttonList;
        this.hgap = hgap;
        this.vgap = vgap;

        TextureCache tc = TextureCache.instance();
        String themeName = JourneyMap.getCoreProperties().themeName.get();
        String pathPattern = "container/toolbar_%s.png";
        textureLeft = tc.getThemeTexture(themeName, String.format(pathPattern, "left"));
        textureInner = tc.getThemeTexture(themeName, String.format(pathPattern, "inner"));
        textureRight = tc.getThemeTexture(themeName, String.format(pathPattern, "right"));
        setToggled(false, false);

        setWidth(buttonList.getWidth(hgap) + textureLeft.width + textureRight.width);
        setHeight(buttonList.getHeight(vgap));
    }

    public ButtonList getButtonList()
    {
        return buttonList;
    }

    public <B extends Button> void add(B... buttons)
    {
        buttonList.addAll(Arrays.asList(buttons));
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY)
    {
        boolean useThemeButton = true;

        if (useThemeButton)
        {

            float scale = height / (textureInner.height * 1F);
            setWidth(buttonList.getWidth(hgap));
            setHeight(32);


            int drawX = buttonList.getLeftX();
            int drawY = buttonList.getTopY() - 6;
            double innerWidth = textureInner.width * scale;

            DrawUtil.drawImage(textureLeft, drawX - (textureLeft.width * scale), drawY, false, scale, 0);
            for (int i = 0; i < buttonList.size() - 1; i++)
            {
                DrawUtil.drawImage(textureInner, drawX, drawY, false, scale, 0);
                drawX += (textureInner.width * scale);
            }
            DrawUtil.drawImage(textureRight, buttonList.getRightX(), drawY, false, scale, 0);
        }
    }

    @Override
    public ArrayList<String> getTooltip()
    {
        return null;
    }
}
