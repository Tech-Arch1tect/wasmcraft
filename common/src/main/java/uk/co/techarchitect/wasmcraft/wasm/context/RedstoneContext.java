package uk.co.techarchitect.wasmcraft.wasm.context;

import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.wasm.types.ValueType;
import uk.co.techarchitect.wasmcraft.wasm.WasmContext;
import uk.co.techarchitect.wasmcraft.wasm.WasmErrorHelper;

import java.util.List;

import static uk.co.techarchitect.wasmcraft.wasm.WasmErrorCodes.*;

public interface RedstoneContext extends WasmContext {
    int BOTTOM = 0;
    int TOP = 1;
    int FRONT = 2;
    int BACK = 3;
    int LEFT = 4;
    int RIGHT = 5;

    int getRedstoneInput(int relativeSide, int[] outPower);
    int setRedstoneOutput(int relativeSide, int power);

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
                    int[] power = new int[1];
                    int errorCode = getRedstoneInput(side, power);

                    int resultPtr = 8192;
                    instance.memory().writeI32(resultPtr, errorCode);
                    instance.memory().writeI32(resultPtr + 4, power[0]);

                    if (errorCode != SUCCESS) {
                        WasmErrorHelper.writeErrorMessage(instance, getRedstoneErrorMessage(errorCode, side));
                    }
                    return new long[] { resultPtr };
                }
            ),
            new HostFunction(
                "env",
                "redstone_set",
                List.of(ValueType.I32, ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    int side = (int) args[0];
                    int power = (int) args[1];
                    int errorCode = setRedstoneOutput(side, power);
                    if (errorCode != SUCCESS) {
                        WasmErrorHelper.writeErrorMessage(instance, getRedstoneErrorMessage(errorCode, side));
                    }
                    return new long[] { errorCode };
                }
            )
        };
    }

    default String getRedstoneErrorMessage(int errorCode, int side) {
        String sideName = getSideName(side);
        return switch (errorCode) {
            case ERR_REDSTONE_INVALID_SIDE -> "Invalid redstone side: " + side + " (must be 0-5)";
            case ERR_REDSTONE_INVALID_POWER -> "Invalid redstone power (must be 0-15)";
            default -> "Unknown redstone error on side " + sideName + ": " + getErrorName(errorCode);
        };
    }

    default String getSideName(int side) {
        return switch (side) {
            case BOTTOM -> "BOTTOM";
            case TOP -> "TOP";
            case FRONT -> "FRONT";
            case BACK -> "BACK";
            case LEFT -> "LEFT";
            case RIGHT -> "RIGHT";
            default -> "UNKNOWN(" + side + ")";
        };
    }
}
