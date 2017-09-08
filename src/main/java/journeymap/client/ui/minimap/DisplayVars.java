/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.minimap;

import journeymap.client.cartography.color.RGB;
import journeymap.client.io.ThemeLoader;
import journeymap.client.model.MapType;
import journeymap.client.properties.MiniMapProperties;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.render.texture.TextureImpl;
import journeymap.client.ui.option.LocationFormat;
import journeymap.client.ui.theme.Theme;
import journeymap.client.ui.theme.ThemeCompassPoints;
import journeymap.client.ui.theme.ThemeLabelSource;
import journeymap.client.ui.theme.ThemeMinimapFrame;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.Tuple;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Display variables for the Minimap.
 * <p/>
 * Encapsulates all the layout and display specifics for rendering the Minimap
 * given a Shape, Position, screen size, and user preferences.  All of the values
 * only need to be calculated once after a change of shape/position/screen size,
 * so it's done here rather than during the minimap renderloop.
 */
public class DisplayVars
{
    /**
     * The Position.
     */
    final Position position;
    /**
     * The Shape.
     */
    final Shape shape;
    /**
     * The Orientation.
     */
    final Orientation orientation;
    /**
     * The Font scale.
     */
    final double fontScale;
    /**
     * The Display width.
     */
    final int displayWidth;
    /**
     * The Display height.
     */
    final int displayHeight;
    /**
     * The Terrain alpha.
     */
    final float terrainAlpha;
    /**
     * The Scaled resolution.
     */
    final ScaledResolution scaledResolution;
    /**
     * The Minimap width.
     */
    final int minimapWidth;
    /**
     * The Minimap height.
     */
    final int minimapHeight;

    /**
     * The Texture x.
     */
    final int textureX;
    /**
     * The Texture y.
     */
    final int textureY;

    /**
     * The Translate x.
     */
    final int translateX;

    /**
     * The Translate y.
     */
    final int translateY;

    /**
     * The Reticle segment length.
     */
    final double reticleSegmentLength;

    /**
     * The Center point.
     */
    final Point2D.Double centerPoint;

    /**
     * The Show compass.
     */
    final boolean showCompass;
    /**
     * The Show reticle.
     */
    final boolean showReticle;

    /**
     * LabelVars and their text suppliers
     */
    final List<Tuple<LabelVars, ThemeLabelSource>> labels = new ArrayList<>(4);

    /**
     * The Theme.
     */
    final Theme theme;
    /**
     * The Minimap frame.
     */
    final ThemeMinimapFrame minimapFrame;
    /**
     * The Minimap compass points.
     */
    final ThemeCompassPoints minimapCompassPoints;
    /**
     * The Minimap spec.
     */
    final Theme.Minimap.MinimapSpec minimapSpec;
    /**
     * The Location format keys.
     */
    final LocationFormat.LocationFormatKeys locationFormatKeys;
    /**
     * The Location format verbose.
     */
    final boolean locationFormatVerbose;
    /**
     * Whether the frame rotates when the map does.
     */
    final boolean frameRotates;
    /**
     * The Margin x.
     */
    int marginX;
    /**
     * The Margin y.
     */
    int marginY;
    /**
     * The Map type status.
     */
    MapTypeStatus mapTypeStatus;
    /**
     * The Map preset status.
     */
    MapPresetStatus mapPresetStatus;

    /**
     * Constructor.
     *
     * @param mc                Minecraft
     * @param miniMapProperties the mini map properties
     */
    DisplayVars(Minecraft mc, final MiniMapProperties miniMapProperties)
    {
        // Immutable member and local vars
        this.scaledResolution = new ScaledResolution(mc);
        this.showCompass = miniMapProperties.showCompass.get();
        this.showReticle = miniMapProperties.showReticle.get();
        this.position = miniMapProperties.position.get();
        this.orientation = miniMapProperties.orientation.get();
        this.displayWidth = mc.displayWidth;
        this.displayHeight = mc.displayHeight;
        this.terrainAlpha = Math.max(0f, Math.min(1f, miniMapProperties.terrainAlpha.get() / 100f));
        this.locationFormatKeys = new LocationFormat().getFormatKeys(miniMapProperties.locationFormat.get());
        this.locationFormatVerbose = miniMapProperties.locationFormatVerbose.get();
        this.theme = ThemeLoader.getCurrentTheme();

        // Assign shape
        switch (miniMapProperties.shape.get())
        {
            case Rectangle:
            {
                if (theme.minimap.square != null)
                {
                    this.shape = Shape.Rectangle;
                    minimapSpec = theme.minimap.square;
                    double ratio = mc.displayWidth * 1D / mc.displayHeight;
                    minimapHeight = miniMapProperties.getSize();
                    minimapWidth = (int) (minimapHeight * ratio);
                    reticleSegmentLength = minimapWidth / 1.5;
                    break;
                }
            }
            case Circle:
            {
                if (theme.minimap.circle != null)
                {
                    this.shape = Shape.Circle;
                    minimapSpec = theme.minimap.circle;
                    minimapWidth = miniMapProperties.getSize();
                    minimapHeight = miniMapProperties.getSize();
                    reticleSegmentLength = minimapHeight / 2;
                    break;
                }
            }
            case Square:
            default:
            {
                this.shape = Shape.Square;
                minimapSpec = theme.minimap.square;
                minimapWidth = miniMapProperties.getSize();
                minimapHeight = miniMapProperties.getSize();
                reticleSegmentLength = Math.sqrt((minimapHeight * minimapHeight) + (minimapWidth * minimapWidth)) / 2;
                break;
            }
        }

        this.fontScale = miniMapProperties.fontScale.get();
        FontRenderer fontRenderer = mc.fontRenderer;

        // Calculate areas reserved for info labels
        int topInfoLabelsHeight = getInfoLabelAreaHeight(fontRenderer, minimapSpec.labelTop, miniMapProperties.info1Label.get(), miniMapProperties.info2Label.get());
        int bottomInfoLabelsHeight = getInfoLabelAreaHeight(fontRenderer, minimapSpec.labelBottom, miniMapProperties.info3Label.get(), miniMapProperties.info4Label.get());

        int compassFontScale = miniMapProperties.compassFontScale.get();
        int compassLabelHeight = 0;
        if (showCompass)
        {
            compassLabelHeight = (int) (DrawUtil.getLabelHeight(fontRenderer, minimapSpec.compassLabel.shadow) * compassFontScale);
        }

        minimapFrame = new ThemeMinimapFrame(theme, minimapSpec, miniMapProperties, minimapWidth, minimapHeight);
        marginX = marginY = minimapSpec.margin;

        int halfWidth = minimapWidth / 2;
        int halfHeight = minimapHeight / 2;

        if (showCompass)
        {
            double compassPointMargin;
            boolean compassExists = minimapSpec.compassPoint != null && minimapSpec.compassPoint.width > 0;
            if (compassExists)
            {
                TextureImpl compassPointTex = this.minimapFrame.getCompassPoint();
                float compassPointScale = ThemeCompassPoints.getCompassPointScale(compassLabelHeight, minimapSpec, compassPointTex);
                compassPointMargin = compassPointTex.getWidth() / 2 * compassPointScale;
            }
            else
            {
                compassPointMargin = compassLabelHeight;
            }
            marginX = (int) Math.max(marginX, Math.ceil(compassPointMargin));
            // TODO: Why height/2?
            marginY = (int) Math.max(marginY, Math.ceil(compassPointMargin) + compassLabelHeight / 2);
        }

        // Assign position
        switch (position)
        {
            case BottomRight:
            {
                if (!minimapSpec.labelBottomInside)
                {
                    marginY += bottomInfoLabelsHeight;
                }

                textureX = mc.displayWidth - minimapWidth - marginX;
                textureY = mc.displayHeight - (minimapHeight) - marginY;
                translateX = (mc.displayWidth / 2) - halfWidth - marginX;
                translateY = (mc.displayHeight / 2) - halfHeight - marginY;
                break;
            }
            case TopLeft:
            {
                if (!minimapSpec.labelTopInside)
                {
                    marginY = Math.max(marginY, topInfoLabelsHeight + (2 * minimapSpec.margin));
                }

                textureX = marginX;
                textureY = marginY;
                translateX = -(mc.displayWidth / 2) + halfWidth + marginX;
                translateY = -(mc.displayHeight / 2) + halfHeight + marginY;
                break;
            }
            case BottomLeft:
            {
                if (!minimapSpec.labelBottomInside)
                {
                    marginY += bottomInfoLabelsHeight;
                }

                textureX = marginX;
                textureY = mc.displayHeight - (minimapHeight) - marginY;
                translateX = -(mc.displayWidth / 2) + halfWidth + marginX;
                translateY = (mc.displayHeight / 2) - halfHeight - marginY;
                break;
            }
            case TopCenter:
            {
                if (!minimapSpec.labelTopInside)
                {
                    marginY = Math.max(marginY, topInfoLabelsHeight + (2 * minimapSpec.margin));
                }
                textureX = (mc.displayWidth - minimapWidth) / 2;
                textureY = marginY;
                translateX = 0;
                translateY = -(mc.displayHeight / 2) + halfHeight + marginY;
                break;
            }
            case Center:
            {
                textureX = (mc.displayWidth - minimapWidth) / 2;
                textureY = (mc.displayHeight - minimapHeight) / 2;
                translateX = 0;
                translateY = 0;
                break;
            }
            case TopRight:
            default:
            {
                if (!minimapSpec.labelTopInside)
                {
                    marginY = Math.max(marginY, topInfoLabelsHeight + (2 * minimapSpec.margin));
                }

                textureX = mc.displayWidth - minimapWidth - marginX;
                textureY = marginY;
                translateX = (mc.displayWidth / 2) - halfWidth - marginX;
                translateY = -(mc.displayHeight / 2) + halfHeight + marginY;
                break;
            }
        }

        // Set frame position
        this.minimapFrame.setPosition(textureX, textureY);

        // Assign frame rectangle and centers
        this.centerPoint = new Point2D.Double(textureX + halfWidth, textureY + halfHeight);

        // Set up compass poionts
        this.minimapCompassPoints = new ThemeCompassPoints(textureX, textureY, halfWidth, halfHeight, minimapSpec,
                miniMapProperties, this.minimapFrame.getCompassPoint(), compassLabelHeight);

        if (shape == Shape.Circle)
        {
            this.frameRotates = ((Theme.Minimap.MinimapCircle) minimapSpec).rotates;
        }
        else
        {
            this.frameRotates = false;
        }

        // Setup top info area labels
        final int centerX = (int) Math.floor(textureX + (minimapWidth / 2));
        if (topInfoLabelsHeight > 0)
        {
            int startY = minimapSpec.labelTopInside ? (textureY + minimapSpec.margin) : textureY - minimapSpec.margin - topInfoLabelsHeight;
            positionLabels(fontRenderer, centerX, startY, minimapSpec.labelTop, miniMapProperties.info1Label.get(), miniMapProperties.info2Label.get());
        }

        // Set up bottom info labels
        if (bottomInfoLabelsHeight > 0)
        {
            int startY = textureY + minimapHeight;
            //startY += minimapSpec.labelBottom.inside ? (-minimapSpec.margin) : minimapSpec.margin;
            startY += minimapSpec.labelBottomInside ? (-minimapSpec.margin - bottomInfoLabelsHeight) : minimapSpec.margin;
            positionLabels(fontRenderer, centerX, startY, minimapSpec.labelBottom, miniMapProperties.info3Label.get(), miniMapProperties.info4Label.get());
        }

        // Reset cache timers on info sources
        ThemeLabelSource.resetCaches();
    }

    private int getInfoLabelAreaHeight(FontRenderer fontRenderer, Theme.LabelSpec labelSpec, ThemeLabelSource... themeLabelSources)
    {
        int labelHeight = getInfoLabelHeight(fontRenderer, labelSpec);
        int areaHeight = 0;
        for (ThemeLabelSource themeLabelSource : themeLabelSources)
        {
            areaHeight += themeLabelSource.isShown() ? labelHeight : 0;
        }
        return areaHeight;
    }

    private int getInfoLabelHeight(FontRenderer fontRenderer, Theme.LabelSpec labelSpec)
    {
        return (int) ((DrawUtil.getLabelHeight(fontRenderer, labelSpec.shadow) + labelSpec.margin) * this.fontScale);
    }

    private void positionLabels(FontRenderer fontRenderer, int centerX, int startY, Theme.LabelSpec labelSpec, ThemeLabelSource... themeLabelSources)
    {
        final int labelHeight = getInfoLabelHeight(fontRenderer, labelSpec);
        int labelY = startY;

        for (ThemeLabelSource themeLabelSource : themeLabelSources)
        {
            if (themeLabelSource.isShown())
            {
                LabelVars labelVars = new LabelVars(this, centerX, labelY, DrawUtil.HAlign.Center, DrawUtil.VAlign.Below, fontScale, labelSpec);
                Tuple<LabelVars, ThemeLabelSource> tuple = new Tuple<>(labelVars, themeLabelSource);
                labels.add(tuple);
                labelY += labelHeight;
            }
        }
    }

    /**
     * Draw labels and their sourced text.
     */
    public void drawInfoLabels(long currentTimeMillis)
    {
        for (Tuple<LabelVars, ThemeLabelSource> label : labels)
        {
            label.getFirst().draw(label.getSecond().getLabelText(currentTimeMillis));
        }
    }

    /**
     * Get or create a MapPresetStatus instance
     *
     * @param mapType   the map type
     * @param miniMapId the mini map id
     * @return map preset status
     */
    MapPresetStatus getMapPresetStatus(MapType mapType, int miniMapId)
    {
        if (this.mapPresetStatus == null || !mapType.equals(this.mapPresetStatus.mapType) || miniMapId != this.mapPresetStatus.miniMapId)
        {
            this.mapPresetStatus = new MapPresetStatus(mapType, miniMapId);
        }
        return mapPresetStatus;
    }

    /**
     * Gets map type status.
     *
     * @param mapType the map type
     * @return the map type status
     */
    MapTypeStatus getMapTypeStatus(MapType mapType)
    {
        if (this.mapTypeStatus == null || !mapType.equals(this.mapTypeStatus.mapType))
        {
            this.mapTypeStatus = new MapTypeStatus(mapType);
        }
        return mapTypeStatus;
    }

    /**
     * Provides a one-time calculation of vars needed to show the MapPreset ID on the minimap
     */
    class MapPresetStatus
    {
        private int miniMapId;
        private int scale = 4;
        private MapType mapType;
        private String name;
        private Integer color;

        /**
         * Instantiates a new Map preset status.
         *
         * @param mapType   the map type
         * @param miniMapId the mini map id
         */
        MapPresetStatus(MapType mapType, int miniMapId)
        {
            this.miniMapId = miniMapId;
            this.mapType = mapType;
            this.color = RGB.WHITE_RGB;
            this.name = Integer.toString(miniMapId);
        }

        /**
         * Draw.
         *
         * @param mapCenter the map center
         * @param alpha     the alpha
         * @param rotation  the rotation
         */
        void draw(Point2D.Double mapCenter, float alpha, double rotation)
        {
            DrawUtil.drawLabel(name, mapCenter.getX(), mapCenter.getY() + 8, DrawUtil.HAlign.Center, DrawUtil.VAlign.Below, RGB.BLACK_RGB, 0, color, alpha, scale, true, rotation);
        }

    }

    /**
     * Provides a one-time calculation of vars needed to show the Map Type on the minimap
     */
    class MapTypeStatus
    {
        private MapType mapType;
        private String name;
        private TextureImpl tex;
        private Integer color;
        private Integer opposite;
        private double x;
        private double y;
        private float bgScale;
        private float scaleHeightOffset;

        /**
         * Instantiates a new Map type status.
         *
         * @param mapType the map type
         */
        MapTypeStatus(MapType mapType)
        {
            this.mapType = mapType;
            name = mapType.isUnderground() ? "caves" : mapType.name();
            tex = TextureCache.getThemeTexture(theme, String.format("icon/%s.png", name));
            color = RGB.WHITE_RGB;
            opposite = RGB.DARK_GRAY_RGB;
            bgScale = 1.15f;
            scaleHeightOffset = ((tex.getHeight() * bgScale) - tex.getHeight()) / 2;
        }

        /**
         * Draw.
         *
         * @param mapCenter the map center
         * @param alpha     the alpha
         * @param rotation  the rotation
         */
        void draw(Point2D.Double mapCenter, float alpha, double rotation)
        {
            x = mapCenter.getX() - (tex.getWidth() / 2);
            y = mapCenter.getY() - tex.getHeight() - 8;
            DrawUtil.drawColoredImage(tex, opposite, alpha, mapCenter.getX() - ((tex.getWidth() * bgScale) / 2), mapCenter.getY() - (tex.getHeight() * bgScale) + scaleHeightOffset - 8, bgScale, rotation);
            DrawUtil.drawColoredImage(tex, color, alpha, x, y, 1, 0);
        }
    }

}
