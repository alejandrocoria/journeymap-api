package net.techbrew.journeymap.ui.theme;

import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;
import net.techbrew.journeymap.ui.Button;
import net.techbrew.journeymap.ui.ButtonList;
import org.lwjgl.opengl.GL11;
import scala.actors.threadpool.Arrays;

import java.util.ArrayList;

/**
 * Created by Mark on 8/30/2014.
 */
public class ThemeToolbar extends Button
{
    private final Theme theme;
    private Theme.Container.Toolbar.ToolbarSpec toolbarSpec;
    private TextureImpl textureBegin;
    private TextureImpl textureInner;
    private TextureImpl textureEnd;
    private final ButtonList buttonList;

    public ThemeToolbar(Enum enumId, Theme theme, Button... buttons)
    {
        this(enumId.ordinal(), theme, buttons);
    }

    public ThemeToolbar(int id, Theme theme, Button... buttons)
    {
        this(id, theme, new ButtonList(buttons));
    }

    public ThemeToolbar(int id, Theme theme, ButtonList buttonList)
    {
        super(id, 0, 0, "");
        this.theme = theme;
        this.buttonList = buttonList;
        setToggled(false, false);
        updateTextures();
    }

    public Theme.Container.Toolbar.ToolbarSpec updateTextures()
    {
        Theme.Container.Toolbar.ToolbarSpec toolbarSpec;
        if(buttonList.isHorizontal())
        {
            toolbarSpec = theme.container.toolbar.horizontal;
            setWidth(toolbarSpec.beginWidth + (toolbarSpec.innerWidth*buttonList.getVisibleButtonCount()) + toolbarSpec.endWidth);
            setHeight(toolbarSpec.innerHeight);
        }
        else
        {
            toolbarSpec = theme.container.toolbar.vertical;
            setWidth(toolbarSpec.innerWidth);
            setHeight(toolbarSpec.beginHeight + (toolbarSpec.innerHeight*buttonList.getVisibleButtonCount()) + toolbarSpec.endHeight);
        }

        //if(this.toolbarSpec==null || toolbarSpec!=this.toolbarSpec)
        {
            this.toolbarSpec = toolbarSpec;

            if(toolbarSpec.useBackgroundImages)
            {
                String pathPattern = "container/" + toolbarSpec.prefix + "%s.png";
                TextureCache tc = TextureCache.instance();
                textureBegin = tc.getThemeTexture(theme, String.format(pathPattern, "begin"), toolbarSpec.beginWidth, toolbarSpec.beginHeight);
                textureInner = tc.getThemeTexture(theme, String.format(pathPattern, "inner"), toolbarSpec.innerWidth, toolbarSpec.innerHeight);
                textureEnd = tc.getThemeTexture(theme, String.format(pathPattern, "end"), toolbarSpec.endWidth, toolbarSpec.endHeight);
            }
        }

        return this.toolbarSpec;
    }

    public Theme.Container.Toolbar.ToolbarSpec getToolbarSpec()
    {
        return toolbarSpec;
    }

    public ButtonList getButtonList()
    {
        return buttonList;
    }

    public <B extends Button> void add(B... buttons)
    {
        buttonList.addAll(Arrays.asList(buttons));
    }

    public int getVMargin()
    {
        if(buttonList.isHorizontal())
        {
            int heightDiff = (toolbarSpec.innerHeight - theme.control.button.height) / 2;
            return heightDiff + toolbarSpec.margin;
        }
        else
        {
            return toolbarSpec.margin;
        }
    }

    public int getHMargin()
    {
        if(buttonList.isHorizontal())
        {
            return toolbarSpec.beginWidth + toolbarSpec.margin;
        }
        else
        {
            int widthDiff = (toolbarSpec.innerWidth - theme.control.button.width) / 2;
            return widthDiff + toolbarSpec.margin;
        }
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY)
    {
        if(!drawButton)
        {
            return;
        }

        updateTextures();
        boolean isHorizontal = buttonList.isHorizontal();

        double drawX, drawY;
        if(isHorizontal)
        {
            drawX = buttonList.getLeftX() - ((width-buttonList.getWidth(toolbarSpec.padding)) / 2);
            drawY = buttonList.getTopY() - ((height - theme.control.button.height) / 2);
        }
        else
        {
            drawX = buttonList.getLeftX() - ((toolbarSpec.innerWidth - theme.control.button.width) / 2);
            drawY = buttonList.getTopY() - ((height - buttonList.getHeight(toolbarSpec.padding)) / 2);
        }

        this.setPosition((int)drawX, (int)drawY);

        if(!toolbarSpec.useBackgroundImages)
        {
            return;
        }

        float scale = 1f;

        DrawUtil.Default_glTexParameteri = GL11.GL_NEAREST;

        try
        {

            // Draw Begin
            if (toolbarSpec.beginWidth != textureBegin.width)
            {
                scale = (1f * toolbarSpec.beginWidth / textureBegin.width);
            }
            DrawUtil.drawImage(textureBegin, drawX, drawY, false, scale, 0);

            if (isHorizontal)
            {
                drawX += (toolbarSpec.beginWidth);
            }
            else
            {
                drawY += (toolbarSpec.beginHeight);
            }

            // Draw Inner
            scale = 1f;
            if (toolbarSpec.innerWidth != textureInner.width)
            {
                scale = (1f * toolbarSpec.innerWidth / textureInner.width);
            }
            for (Button button : buttonList)
            {
                if (button.isDrawButton())
                {
                    DrawUtil.drawImage(textureInner, drawX, drawY, false, scale, 0);
                    if (isHorizontal)
                    {
                        drawX += toolbarSpec.innerWidth;
                    }
                    else
                    {
                        drawY += toolbarSpec.innerHeight;
                    }
                }
            }

            // Draw End
            scale = 1f;
            if (toolbarSpec.endWidth != textureEnd.width)
            {
                scale = (1f * toolbarSpec.endWidth / textureEnd.width);
            }
            DrawUtil.drawImage(textureEnd, drawX, drawY, false, scale, 0);

        }
        finally
        {
            DrawUtil.Default_glTexParameteri = GL11.GL_LINEAR;
        }
    }

    public int getCenterX()
    {
        return this.xPosition + (this.width / 2);
    }

    public int getMiddleY()
    {
        return this.yPosition + (this.height / 2);
    }

    public int getBottomY()
    {
        return this.yPosition + this.height;
    }

    public int getRightX()
    {
        return this.xPosition + this.width;
    }

    @Override
    public ArrayList<String> getTooltip()
    {
        return null;
    }
}
