package com.flechazo.all_living_beings;

import com.flechazo.all_living_beings.config.Config;
import com.flechazo.all_living_beings.network.NetworkHandler;
import com.flechazo.all_living_beings.registry.ModItems;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AllLivingBeings.MODID)
public class AllLivingBeings {
    public static final String MODID = "all_living_beings";

    @SuppressWarnings("removal")
    public AllLivingBeings() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(modBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
        NetworkHandler.init();
    }
}
