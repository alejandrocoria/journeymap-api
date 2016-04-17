/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
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
