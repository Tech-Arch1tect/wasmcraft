package uk.co.techarchitect.wasmcraft.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import uk.co.techarchitect.wasmcraft.blockentity.ModBlockEntities;
import uk.co.techarchitect.wasmcraft.client.model.DroneModel;
import uk.co.techarchitect.wasmcraft.client.renderer.DroneRenderer;
import uk.co.techarchitect.wasmcraft.client.renderer.ModModelLayers;
import uk.co.techarchitect.wasmcraft.client.renderer.MonitorRenderer;
import uk.co.techarchitect.wasmcraft.client.screen.ComputerScreen;
import uk.co.techarchitect.wasmcraft.entity.ModEntities;
import uk.co.techarchitect.wasmcraft.menu.ModMenuTypes;

public final class WasmcraftFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModMenuTypes.COMPUTER_MENU.get(), ComputerScreen::new);
        BlockEntityRenderers.register(ModBlockEntities.MONITOR_BLOCK_ENTITY.get(), MonitorRenderer::new);

        EntityRendererRegistry.register(ModEntities.DRONE.get(), DroneRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.DRONE, DroneModel::createBodyLayer);
    }
}
