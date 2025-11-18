package uk.co.techarchitect.wasmcraft.wasm.context;

import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.wasm.types.ValueType;
import uk.co.techarchitect.wasmcraft.wasm.WasmContext;
import uk.co.techarchitect.wasmcraft.wasm.WasmErrorHelper;

import java.util.List;

import static uk.co.techarchitect.wasmcraft.wasm.WasmErrorCodes.*;
import static uk.co.techarchitect.wasmcraft.wasm.WasmMemoryMap.*;

public interface ExternalInventoryContext extends WasmContext {

    int detectInventory(int relativeSide, int[] outHasInventory);
    int getExternalInventorySize(int relativeSide, int[] outSize);
    int getExternalItem(int relativeSide, int slot, StringBuilder outItemId, int[] outCount);
    int pushItem(int relativeSide, int droneSlot, int externalSlot, int count, int[] outActualCount);
    int pullItem(int relativeSide, int externalSlot, int droneSlot, int count, int[] outActualCount);

    @Override
    default HostFunction[] toHostFunctions() {
        return new HostFunction[] {
            new HostFunction(
                "env",
                "inventory_detect",
                List.of(ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    int relativeSide = (int) args[0];
                    int[] hasInventory = new int[1];
                    int errorCode = detectInventory(relativeSide, hasInventory);

                    instance.memory().writeI32(INVENTORY_RESULT_PTR, errorCode);
                    if (errorCode == SUCCESS) {
                        instance.memory().writeI32(INVENTORY_RESULT_PTR + 4, hasInventory[0]);
                    } else {
                        WasmErrorHelper.writeErrorMessage(instance, getExternalInventoryErrorMessage(errorCode));
                    }
                    return new long[] { INVENTORY_RESULT_PTR };
                }
            ),
            new HostFunction(
                "env",
                "inventory_external_size",
                List.of(ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    int relativeSide = (int) args[0];
                    int[] size = new int[1];
                    int errorCode = getExternalInventorySize(relativeSide, size);

                    instance.memory().writeI32(INVENTORY_RESULT_PTR, errorCode);
                    if (errorCode == SUCCESS) {
                        instance.memory().writeI32(INVENTORY_RESULT_PTR + 4, size[0]);
                    } else {
                        WasmErrorHelper.writeErrorMessage(instance, getExternalInventoryErrorMessage(errorCode));
                    }
                    return new long[] { INVENTORY_RESULT_PTR };
                }
            ),
            new HostFunction(
                "env",
                "inventory_external_get_item",
                List.of(ValueType.I32, ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    int relativeSide = (int) args[0];
                    int slot = (int) args[1];
                    StringBuilder itemId = new StringBuilder();
                    int[] count = new int[1];
                    int errorCode = getExternalItem(relativeSide, slot, itemId, count);

                    instance.memory().writeI32(INVENTORY_RESULT_PTR, errorCode);

                    if (errorCode == SUCCESS) {
                        byte[] bytes = itemId.toString().getBytes();
                        int len = Math.min(bytes.length, INVENTORY_ITEM_ID_MAX_LEN - 1);
                        for (int i = 0; i < len; i++) {
                            instance.memory().writeByte(INVENTORY_ITEM_ID_PTR + i, bytes[i]);
                        }
                        instance.memory().writeByte(INVENTORY_ITEM_ID_PTR + len, (byte) 0);
                        instance.memory().writeI32(INVENTORY_RESULT_PTR + 4, len);
                        instance.memory().writeI32(INVENTORY_RESULT_PTR + 8, count[0]);
                    } else {
                        WasmErrorHelper.writeErrorMessage(instance, getExternalInventoryErrorMessage(errorCode));
                        instance.memory().writeI32(INVENTORY_RESULT_PTR + 4, 0);
                        instance.memory().writeI32(INVENTORY_RESULT_PTR + 8, 0);
                    }
                    return new long[] { INVENTORY_RESULT_PTR };
                }
            ),
            new HostFunction(
                "env",
                "inventory_push",
                List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    int relativeSide = (int) args[0];
                    int droneSlot = (int) args[1];
                    int externalSlot = (int) args[2];
                    int count = (int) args[3];
                    int[] actualCount = new int[1];
                    int errorCode = pushItem(relativeSide, droneSlot, externalSlot, count, actualCount);

                    instance.memory().writeI32(INVENTORY_RESULT_PTR, errorCode);
                    instance.memory().writeI32(INVENTORY_RESULT_PTR + 4, actualCount[0]);

                    if (errorCode != SUCCESS) {
                        WasmErrorHelper.writeErrorMessage(instance, getExternalInventoryErrorMessage(errorCode));
                    }
                    return new long[] { INVENTORY_RESULT_PTR };
                }
            ),
            new HostFunction(
                "env",
                "inventory_pull",
                List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    int relativeSide = (int) args[0];
                    int externalSlot = (int) args[1];
                    int droneSlot = (int) args[2];
                    int count = (int) args[3];
                    int[] actualCount = new int[1];
                    int errorCode = pullItem(relativeSide, externalSlot, droneSlot, count, actualCount);

                    instance.memory().writeI32(INVENTORY_RESULT_PTR, errorCode);
                    instance.memory().writeI32(INVENTORY_RESULT_PTR + 4, actualCount[0]);

                    if (errorCode != SUCCESS) {
                        WasmErrorHelper.writeErrorMessage(instance, getExternalInventoryErrorMessage(errorCode));
                    }
                    return new long[] { INVENTORY_RESULT_PTR };
                }
            )
        };
    }

    default String getExternalInventoryErrorMessage(int errorCode) {
        return switch (errorCode) {
            case ERR_INVENTORY_NO_CONTAINER -> "No inventory found in this direction";
            case ERR_INVENTORY_SLOT_OUT_OF_RANGE -> "Inventory slot out of range";
            case ERR_INVENTORY_NO_SPACE -> "No space in target inventory";
            case ERR_INVENTORY_INCOMPATIBLE_ITEM -> "Item cannot be inserted into this inventory";
            case ERR_INVENTORY_PROTECTED -> "Inventory is protected (spawn protection, claims, etc.)";
            case ERR_INVENTORY_NO_ITEMS_FOUND -> "No items found to pick up";
            case ERR_INVALID_PARAMETER -> "Invalid parameter";
            default -> "Unknown external inventory error: " + getErrorName(errorCode);
        };
    }
}
