package uk.co.techarchitect.wasmcraft.wasm.context;

import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.wasm.types.ValueType;
import uk.co.techarchitect.wasmcraft.wasm.WasmContext;

import java.util.List;

public interface PeripheralContext extends WasmContext {
    String listPeripherals();

    String connectPeripheral(String label);

    void disconnectPeripheral(String peripheralId);

    @Override
    default HostFunction[] toHostFunctions() {
        return new HostFunction[] {
            new HostFunction(
                "env",
                "peripheral_list",
                List.of(ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    String json = listPeripherals();
                    int ptr = (int) args[0];
                    byte[] bytes = json.getBytes();
                    for (int i = 0; i < bytes.length && i < 4096; i++) {
                        instance.memory().writeByte(ptr + i, bytes[i]);
                    }
                    return new long[] { bytes.length };
                }
            ),
            new HostFunction(
                "env",
                "peripheral_connect",
                List.of(ValueType.I32, ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    int labelPtr = (int) args[0];
                    int labelLen = (int) args[1];
                    byte[] labelBytes = new byte[labelLen];
                    for (int i = 0; i < labelLen; i++) {
                        labelBytes[i] = (byte) instance.memory().read(labelPtr + i);
                    }
                    String label = new String(labelBytes);
                    String result = connectPeripheral(label);

                    int resultPtr = 8192;
                    byte[] resultBytes = result.getBytes();
                    for (int i = 0; i < resultBytes.length && i < 4096; i++) {
                        instance.memory().writeByte(resultPtr + i, resultBytes[i]);
                    }
                    return new long[] { resultBytes.length };
                }
            ),
            new HostFunction(
                "env",
                "peripheral_disconnect",
                List.of(ValueType.I32, ValueType.I32),
                List.of(),
                (instance, args) -> {
                    int idPtr = (int) args[0];
                    int idLen = (int) args[1];
                    byte[] idBytes = new byte[idLen];
                    for (int i = 0; i < idLen; i++) {
                        idBytes[i] = (byte) instance.memory().read(idPtr + i);
                    }
                    String peripheralId = new String(idBytes);
                    disconnectPeripheral(peripheralId);
                    return null;
                }
            )
        };
    }
}
