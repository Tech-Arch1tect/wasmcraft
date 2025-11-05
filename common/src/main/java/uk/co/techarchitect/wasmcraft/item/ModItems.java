package uk.co.techarchitect.wasmcraft.item;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import uk.co.techarchitect.wasmcraft.Wasmcraft;
import uk.co.techarchitect.wasmcraft.block.ModBlocks;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Wasmcraft.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<Item> COMPUTER_BLOCK = ITEMS.register("computer_block",
            () -> new BlockItem(ModBlocks.COMPUTER_BLOCK.get(), new Item.Properties()));

    public static void register() {
        ITEMS.register();
    }
}
