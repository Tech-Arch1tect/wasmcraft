package uk.co.techarchitect.wasmcraft.wasm;

import com.dylibso.chicory.runtime.HostFunction;

public interface WasmContext {
    HostFunction[] toHostFunctions();
}
