package uk.co.techarchitect.wasmcraft.wasm.context;

import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.wasm.types.ValueType;
import uk.co.techarchitect.wasmcraft.wasm.WasmContext;
import uk.co.techarchitect.wasmcraft.wasm.WasmErrorHelper;

import java.util.List;

import static uk.co.techarchitect.wasmcraft.wasm.WasmErrorCodes.*;

public interface PeripheralContext extends WasmContext {
    int listPeripherals(StringBuilder outJson);
    int connectPeripheral(String label, StringBuilder outId);
    int disconnectPeripheral(String peripheralId);

    @Override
    default HostFunction[] toHostFunctions() {
        return new HostFunction[] {
            new HostFunction(
                "env",
                "peripheral_list",
                List.of(ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    StringBuilder json = new StringBuilder();
                    int errorCode = listPeripherals(json);

                    int ptr = (int) args[0];
                    int resultPtr = 12288;

                    if (errorCode == SUCCESS) {
                        byte[] bytes = json.toString().getBytes();
                        int len = Math.min(bytes.length, 4096);
                        for (int i = 0; i < len; i++) {
                            instance.memory().writeByte(ptr + i, bytes[i]);
                        }
                        instance.memory().writeI32(resultPtr, SUCCESS);
                        instance.memory().writeI32(resultPtr + 4, len);
                    } else {
                        WasmErrorHelper.writeErrorMessage(instance, getPeripheralErrorMessage(errorCode, ""));
                        instance.memory().writeI32(resultPtr, errorCode);
                        instance.memory().writeI32(resultPtr + 4, 0);
                    }
                    return new long[] { resultPtr };
                }
            ),
            new HostFunction(
                "env",
                "peripheral_connect",
                List.of(ValueType.I32, ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    String label = WasmErrorHelper.readString(instance, (int) args[0], (int) args[1]);
                    StringBuilder id = new StringBuilder();
                    int errorCode = connectPeripheral(label, id);

                    int resultPtr = 8192;

                    if (errorCode == SUCCESS) {
                        byte[] resultBytes = id.toString().getBytes();
                        int len = Math.min(resultBytes.length, 4096);
                        for (int i = 0; i < len; i++) {
                            instance.memory().writeByte(resultPtr + 4, resultBytes[i]);
                        }
                        instance.memory().writeI32(resultPtr, SUCCESS);
                        instance.memory().writeI32(resultPtr + 4 + len, 0);
                    } else {
                        WasmErrorHelper.writeErrorMessage(instance, getPeripheralErrorMessage(errorCode, label));
                        instance.memory().writeI32(resultPtr, errorCode);
                    }
                    return new long[] { resultPtr };
                }
            ),
            new HostFunction(
                "env",
                "peripheral_disconnect",
                List.of(ValueType.I32, ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    String peripheralId = WasmErrorHelper.readString(instance, (int) args[0], (int) args[1]);
                    int errorCode = disconnectPeripheral(peripheralId);
                    if (errorCode != SUCCESS) {
                        WasmErrorHelper.writeErrorMessage(instance, getPeripheralErrorMessage(errorCode, peripheralId));
                    }
                    return new long[] { errorCode };
                }
            )
        };
    }

    default String getPeripheralErrorMessage(int errorCode, String identifier) {
        return switch (errorCode) {
            case ERR_PERIPHERAL_NOT_FOUND -> "Peripheral '" + identifier + "' not found";
            case ERR_PERIPHERAL_OUT_OF_RANGE -> "Peripheral '" + identifier + "' is out of range (max 16 blocks)";
            case ERR_PERIPHERAL_NOT_CONNECTED -> "Peripheral '" + identifier + "' is not connected";
            default -> "Unknown peripheral error: " + getErrorName(errorCode);
        };
    }
}
