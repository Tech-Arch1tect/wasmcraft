package uk.co.techarchitect.wasmcraft.wasm.context;

import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.wasm.types.ValueType;
import net.minecraft.world.item.ItemStack;
import uk.co.techarchitect.wasmcraft.wasm.WasmContext;
import uk.co.techarchitect.wasmcraft.wasm.WasmErrorHelper;
import uk.co.techarchitect.wasmcraft.wasm.WasmMemoryMap;

import java.util.List;

import static uk.co.techarchitect.wasmcraft.wasm.WasmErrorCodes.*;
import static uk.co.techarchitect.wasmcraft.wasm.WasmMemoryMap.*;

public interface InventoryContext extends WasmContext {

    int getSelectedSlot(int[] outSlot);
    int setSelectedSlot(int slot);
    int getInventorySize(int[] outSize);
    int getItem(int slot, StringBuilder outItemId, int[] outCount);
    int scanItems(int relativeSide, StringBuilder outJson, int[] outItemCount);
    int suckItems(int relativeSide, int[] outItemsCollected);
    int dropItems(int slot, int count, int[] outActualCount);

    @Override
    default HostFunction[] toHostFunctions() {
        return new HostFunction[] {
            new HostFunction(
                "env",
                "inventory_get_selected_slot",
                List.of(),
                List.of(ValueType.I32),
                (instance, args) -> {
                    int[] slot = new int[1];
                    int errorCode = getSelectedSlot(slot);

                    instance.memory().writeI32(INVENTORY_RESULT_PTR, errorCode);
                    if (errorCode == SUCCESS) {
                        instance.memory().writeI32(INVENTORY_RESULT_PTR + 4, slot[0]);
                    } else {
                        WasmErrorHelper.writeErrorMessage(instance, getInventoryErrorMessage(errorCode));
                    }
                    return new long[] { INVENTORY_RESULT_PTR };
                }
            ),
            new HostFunction(
                "env",
                "inventory_set_selected_slot",
                List.of(ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    int slot = (int) args[0];
                    int errorCode = setSelectedSlot(slot);

                    instance.memory().writeI32(INVENTORY_RESULT_PTR, errorCode);
                    if (errorCode != SUCCESS) {
                        WasmErrorHelper.writeErrorMessage(instance, getInventoryErrorMessage(errorCode));
                    }
                    return new long[] { INVENTORY_RESULT_PTR };
                }
            ),
            new HostFunction(
                "env",
                "inventory_get_size",
                List.of(),
                List.of(ValueType.I32),
                (instance, args) -> {
                    int[] size = new int[1];
                    int errorCode = getInventorySize(size);

                    instance.memory().writeI32(INVENTORY_RESULT_PTR, errorCode);
                    if (errorCode == SUCCESS) {
                        instance.memory().writeI32(INVENTORY_RESULT_PTR + 4, size[0]);
                    } else {
                        WasmErrorHelper.writeErrorMessage(instance, getInventoryErrorMessage(errorCode));
                    }
                    return new long[] { INVENTORY_RESULT_PTR };
                }
            ),
            new HostFunction(
                "env",
                "inventory_get_item",
                List.of(ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    int slot = (int) args[0];
                    StringBuilder itemId = new StringBuilder();
                    int[] count = new int[1];
                    int errorCode = getItem(slot, itemId, count);

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
                        WasmErrorHelper.writeErrorMessage(instance, getInventoryErrorMessage(errorCode));
                        instance.memory().writeI32(INVENTORY_RESULT_PTR + 4, 0);
                        instance.memory().writeI32(INVENTORY_RESULT_PTR + 8, 0);
                    }
                    return new long[] { INVENTORY_RESULT_PTR };
                }
            ),
            new HostFunction(
                "env",
                "item_scan",
                List.of(ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    int side = (int) args[0];
                    StringBuilder json = new StringBuilder();
                    int[] itemCount = new int[1];
                    int errorCode = scanItems(side, json, itemCount);

                    instance.memory().writeI32(INVENTORY_RESULT_PTR, errorCode);

                    if (errorCode == SUCCESS) {
                        byte[] bytes = json.toString().getBytes();
                        int len = Math.min(bytes.length, INVENTORY_ITEM_ID_MAX_LEN - 1);
                        for (int i = 0; i < len; i++) {
                            instance.memory().writeByte(INVENTORY_ITEM_ID_PTR + i, bytes[i]);
                        }
                        instance.memory().writeByte(INVENTORY_ITEM_ID_PTR + len, (byte) 0);
                        instance.memory().writeI32(INVENTORY_RESULT_PTR + 4, itemCount[0]);
                        instance.memory().writeI32(INVENTORY_RESULT_PTR + 8, len);
                    } else {
                        WasmErrorHelper.writeErrorMessage(instance, getInventoryErrorMessage(errorCode));
                        instance.memory().writeI32(INVENTORY_RESULT_PTR + 4, 0);
                        instance.memory().writeI32(INVENTORY_RESULT_PTR + 8, 0);
                    }
                    return new long[] { INVENTORY_RESULT_PTR };
                }
            ),
            new HostFunction(
                "env",
                "item_suck",
                List.of(ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    int side = (int) args[0];
                    int[] itemsCollected = new int[1];
                    int errorCode = suckItems(side, itemsCollected);

                    instance.memory().writeI32(INVENTORY_RESULT_PTR, errorCode);
                    if (errorCode == SUCCESS || errorCode == ERR_INVENTORY_NO_SPACE) {
                        instance.memory().writeI32(INVENTORY_RESULT_PTR + 4, itemsCollected[0]);
                    } else {
                        WasmErrorHelper.writeErrorMessage(instance, getInventoryErrorMessage(errorCode));
                        instance.memory().writeI32(INVENTORY_RESULT_PTR + 4, 0);
                    }
                    return new long[] { INVENTORY_RESULT_PTR };
                }
            ),
            new HostFunction(
                "env",
                "item_drop",
                List.of(ValueType.I32, ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    int slot = (int) args[0];
                    int count = (int) args[1];
                    int[] actualCount = new int[1];
                    int errorCode = dropItems(slot, count, actualCount);

                    instance.memory().writeI32(INVENTORY_RESULT_PTR, errorCode);
                    if (errorCode == SUCCESS) {
                        instance.memory().writeI32(INVENTORY_RESULT_PTR + 4, actualCount[0]);
                    } else {
                        WasmErrorHelper.writeErrorMessage(instance, getInventoryErrorMessage(errorCode));
                        instance.memory().writeI32(INVENTORY_RESULT_PTR + 4, 0);
                    }
                    return new long[] { INVENTORY_RESULT_PTR };
                }
            )
        };
    }

    default String getInventoryErrorMessage(int errorCode) {
        return switch (errorCode) {
            case ERR_INVALID_PARAMETER -> "Invalid inventory slot (must be 0-" + (getInventorySizeInternal() - 1) + ")";
            default -> "Unknown inventory error: " + getErrorName(errorCode);
        };
    }

    default int getInventorySizeInternal() {
        int[] size = new int[1];
        getInventorySize(size);
        return size[0];
    }
}
