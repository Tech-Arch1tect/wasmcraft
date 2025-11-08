package uk.co.techarchitect.wasmcraft.wasm.context;

import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.wasm.types.ValueType;
import uk.co.techarchitect.wasmcraft.wasm.WasmContext;

import java.util.List;

public interface MonitorContext extends WasmContext {
    void setPixel(String monitorId, int x, int y, int r, int g, int b);

    int[] getPixel(String monitorId, int x, int y);

    void clear(String monitorId, int r, int g, int b);

    int[] getSize(String monitorId);

    void setResolution(String monitorId, int width, int height);

    @Override
    default HostFunction[] toHostFunctions() {
        return new HostFunction[] {
            new HostFunction(
                "env",
                "monitor_set_pixel",
                List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
                List.of(),
                (instance, args) -> {
                    int idPtr = (int) args[0];
                    int idLen = (int) args[1];
                    byte[] idBytes = new byte[idLen];
                    for (int i = 0; i < idLen; i++) {
                        idBytes[i] = (byte) instance.memory().read(idPtr + i);
                    }
                    String monitorId = new String(idBytes);

                    int x = (int) args[2];
                    int y = (int) args[3];
                    int r = (int) args[4];
                    int g = (int) args[5];
                    int b = (int) args[6];

                    setPixel(monitorId, x, y, r, g, b);
                    return null;
                }
            ),
            new HostFunction(
                "env",
                "monitor_get_pixel",
                List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    int idPtr = (int) args[0];
                    int idLen = (int) args[1];
                    byte[] idBytes = new byte[idLen];
                    for (int i = 0; i < idLen; i++) {
                        idBytes[i] = (byte) instance.memory().read(idPtr + i);
                    }
                    String monitorId = new String(idBytes);

                    int x = (int) args[2];
                    int y = (int) args[3];

                    int[] rgb = getPixel(monitorId, x, y);

                    int resultPtr = 16384;
                    instance.memory().writeI32(resultPtr, rgb[0]);
                    instance.memory().writeI32(resultPtr + 4, rgb[1]);
                    instance.memory().writeI32(resultPtr + 8, rgb[2]);

                    return new long[] { resultPtr };
                }
            ),
            new HostFunction(
                "env",
                "monitor_clear",
                List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
                List.of(),
                (instance, args) -> {
                    int idPtr = (int) args[0];
                    int idLen = (int) args[1];
                    byte[] idBytes = new byte[idLen];
                    for (int i = 0; i < idLen; i++) {
                        idBytes[i] = (byte) instance.memory().read(idPtr + i);
                    }
                    String monitorId = new String(idBytes);

                    int r = (int) args[2];
                    int g = (int) args[3];
                    int b = (int) args[4];

                    clear(monitorId, r, g, b);
                    return null;
                }
            ),
            new HostFunction(
                "env",
                "monitor_get_size",
                List.of(ValueType.I32, ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    int idPtr = (int) args[0];
                    int idLen = (int) args[1];
                    byte[] idBytes = new byte[idLen];
                    for (int i = 0; i < idLen; i++) {
                        idBytes[i] = (byte) instance.memory().read(idPtr + i);
                    }
                    String monitorId = new String(idBytes);

                    int[] size = getSize(monitorId);

                    int resultPtr = 20480;
                    instance.memory().writeI32(resultPtr, size[0]);
                    instance.memory().writeI32(resultPtr + 4, size[1]);

                    return new long[] { resultPtr };
                }
            ),
            new HostFunction(
                "env",
                "monitor_set_resolution",
                List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
                List.of(),
                (instance, args) -> {
                    int idPtr = (int) args[0];
                    int idLen = (int) args[1];
                    byte[] idBytes = new byte[idLen];
                    for (int i = 0; i < idLen; i++) {
                        idBytes[i] = (byte) instance.memory().read(idPtr + i);
                    }
                    String monitorId = new String(idBytes);

                    int width = (int) args[2];
                    int height = (int) args[3];

                    setResolution(monitorId, width, height);
                    return null;
                }
            )
        };
    }
}
