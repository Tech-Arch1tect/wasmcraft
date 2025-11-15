package uk.co.techarchitect.wasmcraft.wasm.context;

import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.wasm.types.ValueType;
import uk.co.techarchitect.wasmcraft.wasm.WasmContext;
import uk.co.techarchitect.wasmcraft.wasm.WasmErrorHelper;

import java.util.List;

import static uk.co.techarchitect.wasmcraft.wasm.WasmErrorCodes.*;

public interface WorldContext extends WasmContext {
    int WORLD_RESULT_PTR = 49152;
    int WORLD_BLOCK_ID_PTR = 53248;
    int WORLD_BLOCK_ID_MAX_LEN = 256;
    int WORLD_PROPERTY_VALUE_PTR = 53504;
    int WORLD_PROPERTY_VALUE_MAX_LEN = 64;
    int WORLD_TAGS_PTR = 53568;
    int WORLD_TAGS_MAX_LEN = 2048;
    int WORLD_PROPERTIES_PTR = 55616;
    int WORLD_PROPERTIES_MAX_LEN = 2048;

    int BOTTOM = 0;
    int TOP = 1;
    int FRONT = 2;
    int BACK = 3;
    int LEFT = 4;
    int RIGHT = 5;

    int getBlock(int relativeSide, StringBuilder outBlockId);
    int getBlockProperty(int relativeSide, String propertyName, StringBuilder outValue);
    int hasBlockTag(int relativeSide, String tagName, int[] outHasTag);
    int getBlockTags(int relativeSide, StringBuilder outTags);
    int getBlockProperties(int relativeSide, StringBuilder outProperties);

    @Override
    default HostFunction[] toHostFunctions() {
        return new HostFunction[] {
            new HostFunction(
                "env",
                "world_get_block",
                List.of(ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    int side = (int) args[0];
                    StringBuilder blockId = new StringBuilder();
                    int errorCode = getBlock(side, blockId);

                    instance.memory().writeI32(WORLD_RESULT_PTR, errorCode);

                    if (errorCode == SUCCESS) {
                        byte[] bytes = blockId.toString().getBytes();
                        int len = Math.min(bytes.length, WORLD_BLOCK_ID_MAX_LEN - 1);
                        for (int i = 0; i < len; i++) {
                            instance.memory().writeByte(WORLD_BLOCK_ID_PTR + i, bytes[i]);
                        }
                        instance.memory().writeByte(WORLD_BLOCK_ID_PTR + len, (byte) 0);
                        instance.memory().writeI32(WORLD_RESULT_PTR + 4, len);
                    } else {
                        WasmErrorHelper.writeErrorMessage(instance, getWorldErrorMessage(errorCode, side));
                        instance.memory().writeI32(WORLD_RESULT_PTR + 4, 0);
                    }
                    return new long[] { WORLD_RESULT_PTR };
                }
            ),
            new HostFunction(
                "env",
                "world_get_block_property",
                List.of(ValueType.I32, ValueType.I32, ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    int side = (int) args[0];
                    String propertyName = WasmErrorHelper.readString(instance, (int) args[1], (int) args[2]);
                    StringBuilder value = new StringBuilder();
                    int errorCode = getBlockProperty(side, propertyName, value);

                    instance.memory().writeI32(WORLD_RESULT_PTR, errorCode);

                    if (errorCode == SUCCESS) {
                        byte[] bytes = value.toString().getBytes();
                        int len = Math.min(bytes.length, WORLD_PROPERTY_VALUE_MAX_LEN - 1);
                        for (int i = 0; i < len; i++) {
                            instance.memory().writeByte(WORLD_PROPERTY_VALUE_PTR + i, bytes[i]);
                        }
                        instance.memory().writeByte(WORLD_PROPERTY_VALUE_PTR + len, (byte) 0);
                        instance.memory().writeI32(WORLD_RESULT_PTR + 4, len);
                    } else {
                        WasmErrorHelper.writeErrorMessage(instance, getWorldErrorMessage(errorCode, side));
                        instance.memory().writeI32(WORLD_RESULT_PTR + 4, 0);
                    }
                    return new long[] { WORLD_RESULT_PTR };
                }
            ),
            new HostFunction(
                "env",
                "world_has_block_tag",
                List.of(ValueType.I32, ValueType.I32, ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    int side = (int) args[0];
                    String tagName = WasmErrorHelper.readString(instance, (int) args[1], (int) args[2]);
                    int[] hasTag = new int[1];
                    int errorCode = hasBlockTag(side, tagName, hasTag);

                    instance.memory().writeI32(WORLD_RESULT_PTR, errorCode);
                    instance.memory().writeI32(WORLD_RESULT_PTR + 4, hasTag[0]);

                    if (errorCode != SUCCESS) {
                        WasmErrorHelper.writeErrorMessage(instance, getWorldErrorMessage(errorCode, side));
                    }
                    return new long[] { WORLD_RESULT_PTR };
                }
            ),
            new HostFunction(
                "env",
                "world_get_block_tags",
                List.of(ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    int side = (int) args[0];
                    StringBuilder tags = new StringBuilder();
                    int errorCode = getBlockTags(side, tags);

                    instance.memory().writeI32(WORLD_RESULT_PTR, errorCode);

                    if (errorCode == SUCCESS) {
                        byte[] bytes = tags.toString().getBytes();
                        int len = Math.min(bytes.length, WORLD_TAGS_MAX_LEN - 1);
                        for (int i = 0; i < len; i++) {
                            instance.memory().writeByte(WORLD_TAGS_PTR + i, bytes[i]);
                        }
                        instance.memory().writeByte(WORLD_TAGS_PTR + len, (byte) 0);
                        instance.memory().writeI32(WORLD_RESULT_PTR + 4, len);
                    } else {
                        WasmErrorHelper.writeErrorMessage(instance, getWorldErrorMessage(errorCode, side));
                        instance.memory().writeI32(WORLD_RESULT_PTR + 4, 0);
                    }
                    return new long[] { WORLD_RESULT_PTR };
                }
            ),
            new HostFunction(
                "env",
                "world_get_block_properties",
                List.of(ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    int side = (int) args[0];
                    StringBuilder properties = new StringBuilder();
                    int errorCode = getBlockProperties(side, properties);

                    instance.memory().writeI32(WORLD_RESULT_PTR, errorCode);

                    if (errorCode == SUCCESS) {
                        byte[] bytes = properties.toString().getBytes();
                        int len = Math.min(bytes.length, WORLD_PROPERTIES_MAX_LEN - 1);
                        for (int i = 0; i < len; i++) {
                            instance.memory().writeByte(WORLD_PROPERTIES_PTR + i, bytes[i]);
                        }
                        instance.memory().writeByte(WORLD_PROPERTIES_PTR + len, (byte) 0);
                        instance.memory().writeI32(WORLD_RESULT_PTR + 4, len);
                    } else {
                        WasmErrorHelper.writeErrorMessage(instance, getWorldErrorMessage(errorCode, side));
                        instance.memory().writeI32(WORLD_RESULT_PTR + 4, 0);
                    }
                    return new long[] { WORLD_RESULT_PTR };
                }
            )
        };
    }

    default String getWorldErrorMessage(int errorCode, int side) {
        String sideName = side >= 0 ? getSideName(side) : "specified position";
        return switch (errorCode) {
            case ERR_WORLD_INVALID_SIDE -> "Invalid world side: " + side + " (must be 0-5)";
            case ERR_WORLD_OUT_OF_BOUNDS -> "Position out of world bounds";
            case ERR_WORLD_CHUNK_NOT_LOADED -> "Chunk not loaded at " + sideName;
            case ERR_WORLD_PROPERTY_NOT_FOUND -> "Block property not found";
            case ERR_WORLD_INVALID_TAG -> "Invalid block tag format";
            default -> "Unknown world error: " + getErrorName(errorCode);
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
