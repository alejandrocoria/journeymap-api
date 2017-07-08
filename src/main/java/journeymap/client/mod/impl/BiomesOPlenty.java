/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.mod.impl;

import journeymap.client.mod.IModBlockHandler;
import journeymap.client.model.BlockMD;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static journeymap.client.model.BlockFlag.Crop;
import static journeymap.client.model.BlockFlag.Plant;

/**
 * Special handling required to flag BoP plants and crops.
 */
public class BiomesOPlenty implements IModBlockHandler {
    private List<String> plants = Arrays.asList("flower", "mushroom", "sapling", "plant", "ivy", "waterlily", "moss");
    private List<String> crops = Collections.singletonList("turnip");

    public BiomesOPlenty() {
    }

    @Override
    public void initialize(BlockMD blockMD) {
        String name = blockMD.getBlockId().toLowerCase();
        for (String plant : plants) {
            if (name.contains(plant)) {
                blockMD.addFlags(Plant);
                break;
            }
        }

        for (String crop : crops) {
            if (name.contains(crop)) {
                blockMD.addFlags(Crop);
                break;
            }
        }
    }
}
