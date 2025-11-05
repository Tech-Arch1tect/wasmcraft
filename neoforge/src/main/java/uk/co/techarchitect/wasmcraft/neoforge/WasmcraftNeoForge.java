package uk.co.techarchitect.wasmcraft.neoforge;

import uk.co.techarchitect.wasmcraft.Wasmcraft;
import net.neoforged.fml.common.Mod;

@Mod(Wasmcraft.MOD_ID)
public final class WasmcraftNeoForge {
    public WasmcraftNeoForge() {
        // Run our common setup.
        Wasmcraft.init();
    }
}
