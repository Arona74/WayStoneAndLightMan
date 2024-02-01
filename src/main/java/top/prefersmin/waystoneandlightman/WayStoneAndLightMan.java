package top.prefersmin.waystoneandlightman;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import top.prefersmin.waystoneandlightman.config.ModConfig;
import top.prefersmin.waystoneandlightman.handler.TeleportHandler;

@Mod(WayStoneAndLightMan.MODID)
public class WayStoneAndLightMan
{
    public static final String MODID = "waystoneandlightman";

    public WayStoneAndLightMan()
    {
        MinecraftForge.EVENT_BUS.register(this);

        MinecraftForge.EVENT_BUS.register(new TeleportHandler());

        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, ModConfig.SPEC);
    }

}
