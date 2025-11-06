package uk.co.techarchitect.wasmcraft.block;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import uk.co.techarchitect.wasmcraft.Wasmcraft;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Wasmcraft.MOD_ID, Registries.BLOCK);

    public static final RegistrySupplier<Block> COMPUTER_BLOCK = BLOCKS.register("computer_block", ComputerBlock::new);
    public static final RegistrySupplier<Block> MONITOR_BLOCK = BLOCKS.register("monitor_block", MonitorBlock::new);

    public static void register() {
        BLOCKS.register();
    }
}
