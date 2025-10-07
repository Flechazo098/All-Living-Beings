package com.flechazo.all_living_beings.registry;

import com.flechazo.all_living_beings.AllLivingBeings;
import com.flechazo.all_living_beings.item.HeavenlyThroneItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AllLivingBeings.MODID);

    public static final RegistryObject<Item> HEAVENLY_THRONE = ITEMS.register(
            "fate",
            () -> new HeavenlyThroneItem(new Item.Properties().stacksTo(1))
    );
}