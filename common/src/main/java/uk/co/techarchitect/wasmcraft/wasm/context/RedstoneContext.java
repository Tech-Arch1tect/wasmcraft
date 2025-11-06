package uk.co.techarchitect.wasmcraft.wasm.context;

import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.wasm.types.ValueType;
import uk.co.techarchitect.wasmcraft.wasm.WasmContext;

import java.util.List;

public interface RedstoneContext extends WasmContext {
    int BOTTOM = 0;
    int TOP = 1;
    int FRONT = 2;
    int BACK = 3;
    int LEFT = 4;
    int RIGHT = 5;

    int getRedstoneInput(int relativeSide);
    void setRedstoneOutput(int relativeSide, int power);

    @Override
    default HostFunction[] toHostFunctions() {
        return new HostFunction[] {
            new HostFunction(
                "env",
                "redstone_get",
                List.of(ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    int side = (int) args[0];
                    return new long[] { getRedstoneInput(side) };
                }
            ),
            new HostFunction(
                "env",
                "redstone_set",
                List.of(ValueType.I32, ValueType.I32),
                List.of(),
                (instance, args) -> {
                    int side = (int) args[0];
                    int power = (int) args[1];
                    setRedstoneOutput(side, power);
                    return null;
                }
            )
        };
    }
}
