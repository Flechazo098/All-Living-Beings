package com.flechazo.all_living_beings.client;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.List;

public class ClientCache {
    public static volatile List<ResourceLocation> DIMENSIONS = Collections.emptyList();
}