package uk.co.techarchitect.wasmcraft.menu;

import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import uk.co.techarchitect.wasmcraft.Wasmcraft;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Wasmcraft.MOD_ID, Registries.MENU);

    public static final RegistrySupplier<MenuType<ComputerMenu>> COMPUTER_MENU =
            MENUS.register("computer_menu", () ->
                    MenuRegistry.ofExtended((syncId, inventory, buf) -> {
                        boolean hasPos = buf.readBoolean();
                        if (hasPos) {
                            BlockPos pos = buf.readBlockPos();
                            return new ComputerMenu(syncId, inventory, pos);
                        } else {
                            int entityId = buf.readInt();
                            return new ComputerMenu(syncId, inventory, entityId);
                        }
                    }));

    public static void register() {
        MENUS.register();
    }
}
