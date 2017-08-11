/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.dialog;

import journeymap.client.Constants;
import journeymap.client.cartography.color.RGB;
import journeymap.client.io.FileHandler;
import journeymap.client.model.SplashInfo;
import journeymap.client.model.SplashPerson;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.render.texture.TextureImpl;
import journeymap.client.ui.UIManager;
import journeymap.client.ui.component.Button;
import journeymap.client.ui.component.ButtonList;
import journeymap.client.ui.component.JmUI;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Splash extends JmUI
{
    protected TextureImpl patreonLogo = TextureCache.getTexture(TextureCache.Patreon);
    Button buttonClose, buttonOptions, buttonDonate;
    ButtonList peopleButtons;
    /**
     * The Dev buttons.
     */
    ButtonList devButtons;
    /**
     * The Bottom buttons.
     */
    ButtonList bottomButtons;
    /**
     * The Info buttons.
     */
    ButtonList infoButtons;

    private List<SplashPerson> people = Arrays.asList(
            new SplashPerson("AlexDurrani", "Sikandar Durrani", "jm.common.splash_patreon"),
            new SplashPerson("Davkas", "Davkas", "jm.common.splash_patreon"),
            new SplashPerson("_cache_", "Shaila Gray", "jm.common.splash_patreon"),
            new SplashPerson("_TheEndless_", "The Endless", "jm.common.splash_patreon"),
            new SplashPerson("eladjenkins", "eladjenkins", "jm.common.splash_patreon")
    );

    private List<SplashPerson> devs = Arrays.asList(
            new SplashPerson("mysticdrew", "mysticdrew", "jm.common.splash_developer"),
            new SplashPerson("techbrew", "techbrew", "jm.common.splash_developer")
    );

    private SplashInfo info;
    private TextureImpl brickTex;

    /**
     * Instantiates a new Splash.
     *
     * @param returnDisplay the return display
     */
    public Splash(JmUI returnDisplay)
    {
        super(Constants.getString("jm.common.splash_title", Journeymap.JM_VERSION), returnDisplay);

        // Get brick texture
        brickTex = TextureCache.getTexture(TextureCache.Brick);
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui()
    {
        Journeymap.getClient().getCoreProperties().splashViewed.set(Journeymap.JM_VERSION.toString());

        if (info == null)
        {
            // Get splash strings
            info = FileHandler.getMessageModel(SplashInfo.class, "splash");
            if (info == null)
            {
                info = new SplashInfo();
            }

            String bday = Constants.birthdayMessage();
            if (bday != null)
            {
                info.lines.add(0, new SplashInfo.Line(bday, "dialog.FullscreenActions#tweet#" + bday));
                devs = new ArrayList<SplashPerson>(devs);
                devs.add(new SplashPerson.Fake("", "", TextureCache.getTexture(TextureCache.ColorPicker2)));
            }
        }

        this.buttonList.clear();
        FontRenderer fr = getFontRenderer();
        int minWidth = 0;

        peopleButtons = new ButtonList();
        devButtons = new ButtonList();

        for (SplashPerson person : people)
        {
            Button button = new Button(person.name);// Just used for layout, not display
            peopleButtons.add(button);
            person.setButton(button);
            minWidth = Math.max(minWidth, person.getWidth(fr));
        }
        peopleButtons.setWidths(minWidth);

        infoButtons = new ButtonList();
        for (SplashInfo.Line line : info.lines)
        {
            SplashInfoButton button = new SplashInfoButton(line);
            button.setDrawBackground(false);
            button.setDefaultStyle(false);
            button.setDrawFrame(false);
            button.setHeight(fr.FONT_HEIGHT + 5);
            if (line.hasAction())
            {
                button.setTooltip(Constants.getString("jm.common.splash_action"));
            }
            infoButtons.add(button);
        }
        infoButtons.equalizeWidths(fr);
        buttonList.addAll(infoButtons);

        buttonDonate = new Button("");
        buttonDonate.setDefaultStyle(false);
        buttonDonate.setDrawBackground(false);
        buttonDonate.setDrawFrame(false);
        buttonDonate.setTooltip(Constants.getString("jm.webmap.donate_text"));

        buttonClose = new Button(Constants.getString("jm.common.close"));
        buttonOptions = new Button(Constants.getString("jm.common.options_button"));

        bottomButtons = new ButtonList(buttonOptions, buttonDonate);

        // Show the close button if not in Forge mod Config mode with no game running
        if (mc.world != null)
        {
            bottomButtons.add(buttonClose);
        }

        bottomButtons.equalizeWidths(getFontRenderer());
        bottomButtons.setWidths(Math.max(100, buttonOptions.getWidth()));
        buttonDonate.setWidth(50);
        buttonList.addAll(bottomButtons);

    }

    /**
     * Center buttons in UI.
     */
    @Override
    protected void layoutButtons()
    {
        if (buttonList.isEmpty())
        {
            initGui();
        }

        final int hgap = 4;
        final int vgap = 4;
        int bx = width / 2;
        int by = 45;

        FontRenderer fr = getFontRenderer();
        int lineHeight = (int) (fr.FONT_HEIGHT * 1.4);

        int estimatedInfoHeight = 60 + (fr.FONT_HEIGHT + 5) * infoButtons.size();
        by = ((this.height + this.headerHeight - estimatedInfoHeight) / 2);

        int estimatedWallHeight = 90;
        int gap = 0;

        if (estimatedInfoHeight + estimatedWallHeight + 25 < this.height - this.headerHeight)
        {
            int empty = (this.height + this.headerHeight - estimatedInfoHeight - estimatedWallHeight);
            by = Math.max(45, empty / 3);
            gap = by / 4;
        }

        // Wandering devs
        if (!devButtons.isEmpty())
        {
            int temp = by;
            for (SplashPerson dev : devs)
            {
                temp = drawPerson(temp, lineHeight, dev);
                dev.avoid(devs);
                dev.adjustVector(this.width, this.height);
            }
        }

        // Begin What's New
        if (!infoButtons.isEmpty())
        {
            int topY = by;

            by += (lineHeight * 1.5);
            infoButtons.layoutCenteredVertical(bx - (infoButtons.get(0).getWidth() / 2), by + (infoButtons.getHeight(0) / 2), true, 0);

            int listX = infoButtons.getLeftX() - 10;
            int listY = topY - 5;
            int listWidth = infoButtons.getRightX() + 10 - listX;
            int listHeight = infoButtons.getBottomY() + 5 - listY;
            DrawUtil.drawGradientRect(listX - 1, listY - 1, listWidth + 2, listHeight + 2, RGB.LIGHT_GRAY_RGB, .8f, RGB.LIGHT_GRAY_RGB, .8f);
            DrawUtil.drawGradientRect(listX, listY, listWidth, listHeight, RGB.DARK_GRAY_RGB, 1f, RGB.BLACK_RGB, 1f);
            DrawUtil.drawLabel(Constants.getString("jm.common.splash_whatisnew"), bx, topY,
                    DrawUtil.HAlign.Center, DrawUtil.VAlign.Below, RGB.BLACK_RGB, 0, RGB.CYAN_RGB, 1f, 1, true);

            by = listY + listHeight + 10;
        }

        by += gap;

        // Begin Wall of Fame
        if (by + estimatedWallHeight < this.height - 25)
        {
            int titleY = by;

            by += (lineHeight * 2);
            int listY = by - 30;
            int listHeight = 100;

            peopleButtons.layoutCenteredHorizontal(bx, by, true, 10);
            int listX = peopleButtons.getLeftX() - 10;
            int listWidth = peopleButtons.getRightX() + 10 - listX;


            DrawUtil.drawGradientRect(listX - 1, listY - 1, listWidth + 2, listHeight + 2, RGB.LIGHT_GRAY_RGB, .8f, RGB.LIGHT_GRAY_RGB, .8f);

            brickTex.bindTexture();
            GlStateManager.bindTexture(brickTex.getGlTextureId());
            GlStateManager.color(1, 1, 1, 1);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST); // GL11.GL_LINEAR_MIPMAP_NEAREST
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST); // GL11.GL_NEAREST

            DrawUtil.drawBoundTexture(0, 0, listX, listY, 0, 8, 2, listX + listWidth, listY + listHeight);

            DrawUtil.drawLabel(Constants.getString("jm.common.splash_walloffame"), bx, titleY,
                    DrawUtil.HAlign.Center, DrawUtil.VAlign.Below, RGB.BLACK_RGB, 0, Color.cyan.getRGB(), 1f, 1, true);


            if (devButtons.isEmpty())
            {
                for (SplashPerson dev : devs)
                {
                    Button button = new Button(dev.name);// Just used for layout, not display
                    devButtons.add(button);
                    dev.setButton(button);
                }
                devButtons.equalizeWidths(fr);
                devButtons.layoutCenteredHorizontal(bx, by, true, 10);
            }
//            int rowWidth = peopleButtons.getWidth(hgap);
//
//            Button topLeft = people.getTexture(0).getButton();
//            Button bottomRight = people.getTexture(people.size() - 1).getButton();

            for (SplashPerson person : people)
            {
                by = drawPerson(by, lineHeight, person);
            }

        }

        bx = (this.width) / 2;
        by = this.height - 25;

        bottomButtons.layoutCenteredHorizontal(bx, by, true, hgap);
        DrawUtil.drawImage(patreonLogo, buttonDonate.getCenterX() - 8, buttonDonate.getY() + 2, false, .5f, 0);
    }

    protected int drawPerson(int by, int lineHeight, SplashPerson person)
    {
        float scale = 1;
        Button button = person.getButton();
        int imgSize = (int) (person.getSkin().getWidth() * scale);
        int imgY = button.getY() - 2;
        int imgX = button.getCenterX() - (imgSize / 2);

        GlStateManager.enableAlpha();

        if (!(person instanceof SplashPerson.Fake))
        {
            DrawUtil.drawGradientRect(imgX - 1, imgY - 1, imgSize + 2, imgSize + 2, RGB.BLACK_RGB, .4f, RGB.BLACK_RGB, .8f);
            DrawUtil.drawImage(person.getSkin(), 1f, imgX, imgY, false, scale, 0);
        }
        else
        {
            float size = Math.min(person.getSkin().getWidth() * scale, 24 * scale);
            DrawUtil.drawQuad(person.getSkin(), 0xffffff, 1f, imgX, imgY, size, size, false, 0);
        }

        by = imgY + imgSize + 4;

        String name = person.name.trim();
        String name2 = null;
        boolean twoLineName = name.contains(" ");
        if (twoLineName)
        {
            String[] parts = person.name.split(" ");
            name = parts[0];
            name2 = parts[1];
        }

        DrawUtil.drawLabel(name, button.getCenterX(), by,
                DrawUtil.HAlign.Center, DrawUtil.VAlign.Below, RGB.BLACK_RGB, 0, RGB.WHITE_RGB, 1f, scale, true);

        by += lineHeight;

        if (name2 != null)
        {
            DrawUtil.drawLabel(name2, button.getCenterX(), by,
                    DrawUtil.HAlign.Center, DrawUtil.VAlign.Below, RGB.BLACK_RGB, 0, RGB.WHITE_RGB, 1f, scale, true);
            by += lineHeight;
        }

        DrawUtil.drawLabel(person.title, button.getCenterX(), by,
                DrawUtil.HAlign.Center, DrawUtil.VAlign.Below, RGB.BLACK_RGB, 0, RGB.GREEN_RGB, 1f, scale, true);

        by += lineHeight;

        return by;
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    { // actionPerformed

        if (guibutton == buttonClose)
        {
            closeAndReturn();
        }
        if (guibutton == buttonDonate)
        {
            FullscreenActions.launchPatreon();
        }
        if (guibutton == buttonOptions)
        {
            if (returnDisplay != null && returnDisplay instanceof OptionsManager)
            {
                closeAndReturn();
            }
            else
            {
                UIManager.INSTANCE.openOptionsManager(this);
            }
        }
    }

    @Override
    protected void keyTyped(char c, int i)
    {
        switch (i)
        {
            case Keyboard.KEY_ESCAPE:
            {
                closeAndReturn();
            }
        }
    }

    /**
     * Uses the action name in a SplashInfo.Line.action to do an action
     */
    class SplashInfoButton extends Button
    {
        final SplashInfo.Line infoLine;

        public SplashInfoButton(SplashInfo.Line infoLine)
        {
            super(infoLine.label);
            this.infoLine = infoLine;
        }

        @Override
        public boolean mouseOver(int mouseX, int mouseY)
        {
            return super.mouseOver(mouseX, mouseY);
        }

        @Override
        public boolean mousePressed(Minecraft minecraft, int i, int j)
        {
            boolean pressed = super.mousePressed(minecraft, i, j);
            if (pressed)
            {
                infoLine.invokeAction(Splash.this);
            }
            return pressed;
        }
    }

}
