/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.component;

import journeymap.common.properties.config.ConfigField;

/**
 * Indicates a class wraps a ConfigField.
 *
 * @param <T> the type parameter
 */
public interface IConfigFieldHolder<T extends ConfigField>
{
    /**
     * Gets config field.
     *
     * @return the config field
     */
    public T getConfigField();
}
