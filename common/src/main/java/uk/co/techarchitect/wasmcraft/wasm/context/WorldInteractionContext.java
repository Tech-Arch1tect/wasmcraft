package uk.co.techarchitect.wasmcraft.wasm.context;

import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.wasm.types.ValueType;
import uk.co.techarchitect.wasmcraft.wasm.WasmContext;
import uk.co.techarchitect.wasmcraft.wasm.WasmErrorHelper;

import java.util.List;

import static uk.co.techarchitect.wasmcraft.wasm.WasmErrorCodes.*;
import static uk.co.techarchitect.wasmcraft.wasm.WasmMemoryMap.*;

public interface WorldInteractionContext extends WasmContext {

    int canBreak(int relativeSide, int[] outCanBreak);

    int breakBlock(int relativeSide);

    @Override
    default HostFunction[] toHostFunctions() {
        return new HostFunction[] {
            new HostFunction(
                "env",
                "world_can_break",
                List.of(ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    int relativeSide = (int) args[0];
                    int[] canBreak = new int[1];
                    int errorCode = canBreak(relativeSide, canBreak);

                    instance.memory().writeI32(WORLD_INTERACTION_RESULT_PTR, errorCode);
                    instance.memory().writeI32(WORLD_INTERACTION_RESULT_PTR + 4, canBreak[0]);

                    if (errorCode != SUCCESS) {
                        WasmErrorHelper.writeErrorMessage(instance, getBreakBlockErrorMessage(errorCode));
                    }

                    return new long[] { WORLD_INTERACTION_RESULT_PTR };
                }
            ),
            new HostFunction(
                "env",
                "world_break_block",
                List.of(ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    int relativeSide = (int) args[0];
                    int errorCode = breakBlock(relativeSide);

                    instance.memory().writeI32(WORLD_INTERACTION_RESULT_PTR, errorCode);

                    if (errorCode != SUCCESS) {
                        WasmErrorHelper.writeErrorMessage(instance, getBreakBlockErrorMessage(errorCode));
                    }

                    return new long[] { WORLD_INTERACTION_RESULT_PTR };
                }
            )
        };
    }

    default String getBreakBlockErrorMessage(int errorCode) {
        return switch (errorCode) {
            case ERR_WORLD_INVALID_SIDE -> "Invalid side (must be 0=BOTTOM, 1=TOP, or 2=FRONT)";
            case ERR_WORLD_WRONG_TOOL -> "Wrong tool or insufficient tier for this block";
            case ERR_WORLD_UNBREAKABLE -> "Block cannot be broken (bedrock, barrier, etc.)";
            case ERR_WORLD_PROTECTED -> "Block is protected (spawn protection, claims, etc.)";
            case ERR_INVALID_PARAMETER -> "Invalid parameter";
            default -> "Unknown world interaction error: " + getErrorName(errorCode);
        };
    }
}
