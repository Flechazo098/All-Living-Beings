package com.flechazo.sky_accessories;

import com.flechazo.sky_accessories.config.Config;
import com.flechazo.sky_accessories.network.ClientSkyNet;
import com.flechazo.sky_accessories.network.SkyNet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SkyAccessories.MODID)
public class SkyAccessories {
    public static final String MODID = "sky_accessories";

    public SkyAccessories() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(modBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
        SkyNet.init();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientSkyNet::init);
    }
}
