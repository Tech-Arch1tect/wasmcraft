package uk.co.techarchitect.wasmcraft.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import uk.co.techarchitect.wasmcraft.client.screen.ComputerScreen;
import uk.co.techarchitect.wasmcraft.menu.ModMenuTypes;

public final class WasmcraftFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModMenuTypes.COMPUTER_MENU.get(), ComputerScreen::new);
    }
}
