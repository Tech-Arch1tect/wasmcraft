package uk.co.techarchitect.wasmcraft.item;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import uk.co.techarchitect.wasmcraft.Wasmcraft;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Wasmcraft.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> WASMCRAFT_TAB = TABS.register("wasmcraft",
            () -> CreativeTabRegistry.create(builder -> builder
                    .title(Component.translatable("itemGroup." + Wasmcraft.MOD_ID))
                    .icon(() -> new ItemStack(ModItems.COMPUTER_BLOCK.get()))
                    .displayItems((context, entries) -> {
                        entries.accept(ModItems.COMPUTER_BLOCK.get());
                        entries.accept(ModItems.MONITOR_BLOCK.get());
                        entries.accept(ModItems.DRONE_SPAWN_EGG.get());
                    })
            ));

    public static void register() {
        TABS.register();
    }
}
