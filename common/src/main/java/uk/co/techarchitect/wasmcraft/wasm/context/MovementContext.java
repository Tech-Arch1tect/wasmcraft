package uk.co.techarchitect.wasmcraft.wasm.context;

import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.wasm.types.ValueType;
import uk.co.techarchitect.wasmcraft.wasm.WasmContext;
import uk.co.techarchitect.wasmcraft.wasm.WasmErrorHelper;

import java.util.List;

import static uk.co.techarchitect.wasmcraft.wasm.WasmErrorCodes.*;

public interface MovementContext extends WasmContext {
    int MOVEMENT_RESULT_PTR = 40960;

    int moveRelative(float forward, float strafe, float vertical, float[] outActualMovement);
    int rotate(float yawDegrees, float[] outActualYaw);
    int getPosition(double[] outPosition);
    int getYaw(float[] outYaw);
    int setYaw(float yawDegrees);

    @Override
    default HostFunction[] toHostFunctions() {
        return new HostFunction[] {
            new HostFunction(
                "env",
                "move_forward",
                List.of(ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    float distance = Float.intBitsToFloat((int) args[0]);
                    float[] actual = new float[3];
                    int errorCode = moveRelative(distance, 0, 0, actual);

                    instance.memory().writeI32(MOVEMENT_RESULT_PTR, errorCode);
                    instance.memory().writeF32(MOVEMENT_RESULT_PTR + 4, actual[0]);
                    instance.memory().writeF32(MOVEMENT_RESULT_PTR + 8, actual[1]);
                    instance.memory().writeF32(MOVEMENT_RESULT_PTR + 12, actual[2]);

                    if (errorCode != SUCCESS) {
                        WasmErrorHelper.writeErrorMessage(instance, getMovementErrorMessage(errorCode, "move_forward"));
                    }
                    return new long[] { errorCode };
                }
            ),
            new HostFunction(
                "env",
                "move_backward",
                List.of(ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    float distance = Float.intBitsToFloat((int) args[0]);
                    float[] actual = new float[3];
                    int errorCode = moveRelative(-distance, 0, 0, actual);

                    instance.memory().writeI32(MOVEMENT_RESULT_PTR, errorCode);
                    instance.memory().writeF32(MOVEMENT_RESULT_PTR + 4, actual[0]);
                    instance.memory().writeF32(MOVEMENT_RESULT_PTR + 8, actual[1]);
                    instance.memory().writeF32(MOVEMENT_RESULT_PTR + 12, actual[2]);

                    if (errorCode != SUCCESS) {
                        WasmErrorHelper.writeErrorMessage(instance, getMovementErrorMessage(errorCode, "move_backward"));
                    }
                    return new long[] { errorCode };
                }
            ),
            new HostFunction(
                "env",
                "move_left",
                List.of(ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    float distance = Float.intBitsToFloat((int) args[0]);
                    float[] actual = new float[3];
                    int errorCode = moveRelative(0, -distance, 0, actual);

                    instance.memory().writeI32(MOVEMENT_RESULT_PTR, errorCode);
                    instance.memory().writeF32(MOVEMENT_RESULT_PTR + 4, actual[0]);
                    instance.memory().writeF32(MOVEMENT_RESULT_PTR + 8, actual[1]);
                    instance.memory().writeF32(MOVEMENT_RESULT_PTR + 12, actual[2]);

                    if (errorCode != SUCCESS) {
                        WasmErrorHelper.writeErrorMessage(instance, getMovementErrorMessage(errorCode, "move_left"));
                    }
                    return new long[] { errorCode };
                }
            ),
            new HostFunction(
                "env",
                "move_right",
                List.of(ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    float distance = Float.intBitsToFloat((int) args[0]);
                    float[] actual = new float[3];
                    int errorCode = moveRelative(0, distance, 0, actual);

                    instance.memory().writeI32(MOVEMENT_RESULT_PTR, errorCode);
                    instance.memory().writeF32(MOVEMENT_RESULT_PTR + 4, actual[0]);
                    instance.memory().writeF32(MOVEMENT_RESULT_PTR + 8, actual[1]);
                    instance.memory().writeF32(MOVEMENT_RESULT_PTR + 12, actual[2]);

                    if (errorCode != SUCCESS) {
                        WasmErrorHelper.writeErrorMessage(instance, getMovementErrorMessage(errorCode, "move_right"));
                    }
                    return new long[] { errorCode };
                }
            ),
            new HostFunction(
                "env",
                "move_up",
                List.of(ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    float distance = Float.intBitsToFloat((int) args[0]);
                    float[] actual = new float[3];
                    int errorCode = moveRelative(0, 0, distance, actual);

                    instance.memory().writeI32(MOVEMENT_RESULT_PTR, errorCode);
                    instance.memory().writeF32(MOVEMENT_RESULT_PTR + 4, actual[0]);
                    instance.memory().writeF32(MOVEMENT_RESULT_PTR + 8, actual[1]);
                    instance.memory().writeF32(MOVEMENT_RESULT_PTR + 12, actual[2]);

                    if (errorCode != SUCCESS) {
                        WasmErrorHelper.writeErrorMessage(instance, getMovementErrorMessage(errorCode, "move_up"));
                    }
                    return new long[] { errorCode };
                }
            ),
            new HostFunction(
                "env",
                "move_down",
                List.of(ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    float distance = Float.intBitsToFloat((int) args[0]);
                    float[] actual = new float[3];
                    int errorCode = moveRelative(0, 0, -distance, actual);

                    instance.memory().writeI32(MOVEMENT_RESULT_PTR, errorCode);
                    instance.memory().writeF32(MOVEMENT_RESULT_PTR + 4, actual[0]);
                    instance.memory().writeF32(MOVEMENT_RESULT_PTR + 8, actual[1]);
                    instance.memory().writeF32(MOVEMENT_RESULT_PTR + 12, actual[2]);

                    if (errorCode != SUCCESS) {
                        WasmErrorHelper.writeErrorMessage(instance, getMovementErrorMessage(errorCode, "move_down"));
                    }
                    return new long[] { errorCode };
                }
            ),
            new HostFunction(
                "env",
                "get_position",
                List.of(),
                List.of(ValueType.I32),
                (instance, args) -> {
                    double[] position = new double[3];
                    int errorCode = getPosition(position);

                    instance.memory().writeI32(MOVEMENT_RESULT_PTR, errorCode);
                    instance.memory().writeF64(MOVEMENT_RESULT_PTR + 4, position[0]);
                    instance.memory().writeF64(MOVEMENT_RESULT_PTR + 12, position[1]);
                    instance.memory().writeF64(MOVEMENT_RESULT_PTR + 20, position[2]);

                    if (errorCode != SUCCESS) {
                        WasmErrorHelper.writeErrorMessage(instance, getMovementErrorMessage(errorCode, "get_position"));
                    }
                    return new long[] { MOVEMENT_RESULT_PTR };
                }
            ),
            new HostFunction(
                "env",
                "rotate",
                List.of(ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    float yawDegrees = Float.intBitsToFloat((int) args[0]);
                    float[] actualYaw = new float[1];
                    int errorCode = rotate(yawDegrees, actualYaw);

                    instance.memory().writeI32(MOVEMENT_RESULT_PTR, errorCode);
                    instance.memory().writeF32(MOVEMENT_RESULT_PTR + 4, actualYaw[0]);

                    if (errorCode != SUCCESS) {
                        WasmErrorHelper.writeErrorMessage(instance, getMovementErrorMessage(errorCode, "rotate"));
                    }
                    return new long[] { errorCode };
                }
            ),
            new HostFunction(
                "env",
                "get_yaw",
                List.of(),
                List.of(ValueType.I32),
                (instance, args) -> {
                    float[] yaw = new float[1];
                    int errorCode = getYaw(yaw);

                    instance.memory().writeI32(MOVEMENT_RESULT_PTR, errorCode);
                    instance.memory().writeF32(MOVEMENT_RESULT_PTR + 4, yaw[0]);

                    if (errorCode != SUCCESS) {
                        WasmErrorHelper.writeErrorMessage(instance, getMovementErrorMessage(errorCode, "get_yaw"));
                    }
                    return new long[] { errorCode };
                }
            ),
            new HostFunction(
                "env",
                "set_yaw",
                List.of(ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    float yawDegrees = Float.intBitsToFloat((int) args[0]);
                    int errorCode = setYaw(yawDegrees);

                    if (errorCode != SUCCESS) {
                        WasmErrorHelper.writeErrorMessage(instance, getMovementErrorMessage(errorCode, "set_yaw"));
                    }
                    return new long[] { errorCode };
                }
            )
        };
    }

    default String getMovementErrorMessage(int errorCode, String function) {
        return switch (errorCode) {
            case ERR_MOVEMENT_COLLISION -> "Movement blocked by collision in " + function;
            case ERR_MOVEMENT_OUT_OF_WORLD -> "Movement would take entity out of world bounds";
            case ERR_MOVEMENT_INVALID_DISTANCE -> "Invalid movement distance (must be >= 0)";
            case ERR_MOVEMENT_NOT_SUPPORTED -> "Movement not supported by this computer type";
            case ERR_MOVEMENT_IN_PROGRESS -> "Cannot start movement while another movement is in progress";
            default -> "Unknown movement error in " + function + ": " + getErrorName(errorCode);
        };
    }
}
