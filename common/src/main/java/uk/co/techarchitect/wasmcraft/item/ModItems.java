package uk.co.techarchitect.wasmcraft.item;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import uk.co.techarchitect.wasmcraft.Wasmcraft;
import uk.co.techarchitect.wasmcraft.block.ModBlocks;
import dev.architectury.registry.registries.Registrar;
import net.minecraft.world.item.SpawnEggItem;
import uk.co.techarchitect.wasmcraft.entity.ModEntities;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Wasmcraft.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<Item> COMPUTER_BLOCK = ITEMS.register("computer_block",
            () -> new BlockItem(ModBlocks.COMPUTER_BLOCK.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> MONITOR_BLOCK = ITEMS.register("monitor_block",
            () -> new BlockItem(ModBlocks.MONITOR_BLOCK.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> DRONE_SPAWN_EGG = ITEMS.register("drone_spawn_egg",
            () -> new SpawnEggItem(ModEntities.DRONE.get(), 0x404040, 0x00AAFF, new Item.Properties()));

    public static void register() {
        ITEMS.register();
    }
}
