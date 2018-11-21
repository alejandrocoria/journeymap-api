/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.component;

import journeymap.common.properties.config.ConfigField;

/**
 * Indicates a class wraps a ConfigField.
 */
public interface IConfigFieldHolder<T extends ConfigField>
{
    public T getConfigField();
}
