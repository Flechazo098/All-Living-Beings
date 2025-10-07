package com.flechazo.all_living_beings.client;

import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientTitleCache {
    private static final Map<UUID, Component> CACHE = new HashMap<>();

    public static Component get(UUID id) {
        return CACHE.get(id);
    }

    public static void put(UUID id, Component title) {
        CACHE.put(id, title);
    }

    public static void remove(UUID id) {
        CACHE.remove(id);
    }

    public static void clear() {
        CACHE.clear();
    }
}