package com.flechazo.sky_accessories;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SkyAccessories.MODID);

    public static final RegistryObject<Item> HEAVENLY_THRONE = ITEMS.register(
            "fate",
            () -> new HeavenlyThroneItem(new Item.Properties().stacksTo(1))
    );
}