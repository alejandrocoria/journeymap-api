/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
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
import org.lwjgl.input.Mouse;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AboutDialog extends JmUI
{
    protected TextureImpl patreonLogo = TextureCache.getTexture(TextureCache.Patreon);
    protected TextureImpl discordLogo = TextureCache.getTexture(TextureCache.Discord);
    Button buttonClose, buttonOptions, buttonPatreon, buttonDiscord, buttonWebsite, buttonDownload;
    ButtonList peopleButtons;
    ButtonList devButtons;
    ButtonList logoButtons;
    ButtonList linkButtons;
    ButtonList bottomButtons;
    ButtonList infoButtons;
    private long lastPeopleMove;

    private List<SplashPerson> people = Arrays.asList(
            new SplashPerson("AlexDurrani", "Sikandar Durrani", "jm.common.splash_patreon"),
            new SplashPerson("_TheEndless_", "The Endless", "jm.common.splash_patreon"),
            new SplashPerson("eladjenkins", "eladjenkins", "jm.common.splash_patreon")
    );

    private List<SplashPerson> devs = Arrays.asList(
            new SplashPerson("mysticdrew", "mysticdrew", "jm.common.splash_developer"),
            new SplashPerson("techbrew", "techbrew", "jm.common.splash_developer")
    );

    private SplashInfo info;

    public AboutDialog(JmUI returnDisplay)
    {
        super(Constants.getString("jm.common.splash_title", Journeymap.JM_VERSION), returnDisplay);
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

            return;
        }

        this.buttonList.clear();
        FontRenderer fr = getFontRenderer();

        devButtons = new ButtonList();
        for (SplashPerson dev : devs)
        {
            // Button is just used for layout, not displayed as a button
            Button button = new Button(dev.name);
            devButtons.add(button);
            dev.setButton(button);
        }
        devButtons.setWidths(20);
        devButtons.setHeights(20);
        devButtons.layoutDistributedHorizontal(0, headerHeight, width, true);

        peopleButtons = new ButtonList();
        for (SplashPerson peep : people)
        {
            Button button = new Button(peep.name);// Just used for layout, not display
            peopleButtons.add(button);
            peep.setButton(button);
        }
        peopleButtons.setWidths(20);
        peopleButtons.setHeights(20);
        peopleButtons.layoutDistributedHorizontal(0, height-65, width, true);

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

        // Bottom Buttons
        buttonClose = new Button(Constants.getString("jm.common.close"));
        buttonClose.addClickListener(button -> {
            closeAndReturn();
            return true;
        });

        buttonOptions = new Button(Constants.getString("jm.common.options_button"));
        buttonOptions.addClickListener(button -> {
            if (returnDisplay != null && returnDisplay instanceof OptionsManager)
            {
                closeAndReturn();
            }
            else
            {
                UIManager.INSTANCE.openOptionsManager(this);
            }
            return true;
        });

        bottomButtons = new ButtonList(buttonOptions);

        // Show the close button if not in Forge mod Config mode with no game running
        if (mc.world != null)
        {
            bottomButtons.add(buttonClose);
        }

        bottomButtons.equalizeWidths(fr);
        bottomButtons.setWidths(Math.max(100, buttonOptions.getWidth()));
        buttonList.addAll(bottomButtons);

        // Link Buttons
        buttonWebsite = new Button("http://journeymap.info");
        buttonWebsite.setTooltip(Constants.getString("jm.common.website"));
        buttonWebsite.addClickListener(button -> {
            FullscreenActions.launchWebsite("");
            return true;
        });

        buttonDownload = new Button(Constants.getString("jm.common.download"));
        buttonDownload.setTooltip(Constants.getString("jm.common.download.tooltip"));
        buttonDownload.addClickListener(button -> {
            FullscreenActions.launchDownloadWebsite();
            return true;
        });

        linkButtons = new ButtonList(buttonWebsite, buttonDownload);
        linkButtons.equalizeWidths(fr);
        buttonList.addAll(linkButtons);

        int commonWidth = Math.max(bottomButtons.getWidth(0)/bottomButtons.size(), linkButtons.getWidth(0)/linkButtons.size());
        bottomButtons.setWidths(commonWidth);
        linkButtons.setWidths(commonWidth);

        // Logo Buttons
        buttonPatreon = new Button("");
        buttonPatreon.setDefaultStyle(false);
        buttonPatreon.setDrawBackground(false);
        buttonPatreon.setDrawFrame(false);
        buttonPatreon.setTooltip(Constants.getString("jm.common.patreon"), Constants.getString("jm.common.patreon.tooltip"));
        buttonPatreon.setWidth(patreonLogo.getWidth()/scaleFactor);
        buttonPatreon.setHeight(patreonLogo.getHeight()/scaleFactor);
        buttonPatreon.addClickListener(button -> {
            FullscreenActions.launchPatreon();
            return true;
        });

        buttonDiscord = new Button("");
        buttonDiscord.setDefaultStyle(false);
        buttonDiscord.setDrawBackground(false);
        buttonDiscord.setDrawFrame(false);
        buttonDiscord.setTooltip(Constants.getString("jm.common.discord"), Constants.getString("jm.common.discord.tooltip"));
        buttonDiscord.setWidth(discordLogo.getWidth()/scaleFactor);
        buttonDiscord.setHeight(discordLogo.getHeight()/scaleFactor);
        buttonDiscord.addClickListener(button -> {
            FullscreenActions.discord();
            return true;
        });

        logoButtons = new ButtonList(buttonDiscord, buttonPatreon);
        logoButtons.setLayout(ButtonList.Layout.Horizontal, ButtonList.Direction.LeftToRight);
        logoButtons.setHeights(Math.max(discordLogo.getHeight(), patreonLogo.getHeight())/scaleFactor);
        logoButtons.setWidths(Math.max(discordLogo.getWidth(), patreonLogo.getWidth())/scaleFactor);

        buttonList.addAll(logoButtons);
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

        final int mx = (Mouse.getEventX() * width) / mc.displayWidth;
        final int my = height - (Mouse.getEventY() * height) / mc.displayHeight - 1;

        final int hgap = 4;
        final int vgap = 4;
        FontRenderer fr = getFontRenderer();

        int estimatedInfoHeight = infoButtons.getHeight(vgap);
        int estimatedButtonsHeight = ((buttonClose.getHeight() + vgap) * 3) + vgap;
        int centerHeight = this.height - this.headerHeight - estimatedButtonsHeight;
        int lineHeight = (int) (fr.FONT_HEIGHT * 1.4);
        int bx = width / 2;
        int by = 0;

        boolean movePeople = System.currentTimeMillis()-lastPeopleMove>20;
        if(movePeople)
        {
            lastPeopleMove = System.currentTimeMillis();
        }

        // Devs
        Rectangle2D.Double screenBounds = new Rectangle2D.Double(0, 0, width, height);
        if (!devButtons.isEmpty())
        {
            for (SplashPerson dev : devs)
            {
                if(dev.getButton().mouseOver(mx, my))
                {
                    dev.randomizeVector();
                }
                drawPerson(by, lineHeight, dev);
                if(movePeople)
                {
                    dev.avoid(devs);
                    dev.adjustVector(screenBounds);
                }
            }
        }

        // Patrons, etc.
        if (!peopleButtons.isEmpty())
        {
            for (SplashPerson peep : people)
            {
                if(peep.getButton().mouseOver(mx, my))
                {
                    peep.randomizeVector();
                }
                drawPerson(by, lineHeight, peep);
                if(movePeople)
                {
                    peep.avoid(devs);
                    peep.adjustVector(screenBounds);
                }
            }
        }

        // Begin Info (What's New)
        if (!infoButtons.isEmpty())
        {
            by = this.headerHeight + ((centerHeight-estimatedInfoHeight)/2);

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
        }

        // Bottom buttons
        int rowHeight = buttonOptions.height + vgap;
        by = this.height - rowHeight - vgap;

        bottomButtons.layoutCenteredHorizontal(bx, by, true, hgap);
        by-= rowHeight;

        linkButtons.layoutCenteredHorizontal(bx, by, true, hgap);
        by-= (vgap + logoButtons.getHeight());

        logoButtons.layoutCenteredHorizontal(bx, by, true, hgap + 2);
        DrawUtil.drawImage(patreonLogo, buttonPatreon.getX(), buttonPatreon.getY(), false, 1f/scaleFactor, 0);
        DrawUtil.drawImage(discordLogo, buttonDiscord.getX(), buttonDiscord.getY(), false, 1f/scaleFactor, 0);

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
    {
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
        public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY)
        {
            boolean pressed = super.mousePressed(minecraft, mouseX, mouseY, false);
            if (pressed)
            {
                infoLine.invokeAction(AboutDialog.this);
            }
            return checkClickListeners();
        }
    }

}
