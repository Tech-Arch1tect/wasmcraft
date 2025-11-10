package uk.co.techarchitect.wasmcraft.wasm;

import com.dylibso.chicory.runtime.HostFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WasmContextComposer implements WasmContext {
    private final List<WasmContext> contexts = new ArrayList<>();

    public void add(WasmContext context) {
        contexts.add(context);
    }

    @Override
    public HostFunction[] toHostFunctions() {
        return contexts.stream()
            .flatMap(c -> Arrays.stream(c.toHostFunctions()))
            .toArray(HostFunction[]::new);
    }
}
