package uk.co.techarchitect.wasmcraft.neoforge.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import uk.co.techarchitect.wasmcraft.Wasmcraft;
import uk.co.techarchitect.wasmcraft.blockentity.ModBlockEntities;
import uk.co.techarchitect.wasmcraft.client.renderer.MonitorRenderer;
import uk.co.techarchitect.wasmcraft.client.screen.ComputerScreen;
import uk.co.techarchitect.wasmcraft.menu.ModMenuTypes;

@EventBusSubscriber(modid = Wasmcraft.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class WasmcraftClientNeoForge {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.COMPUTER_MENU.get(), ComputerScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.MONITOR_BLOCK_ENTITY.get(), MonitorRenderer::new);
    }
}
