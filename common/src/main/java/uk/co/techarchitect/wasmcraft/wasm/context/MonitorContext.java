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

    void fillRect(String monitorId, int x, int y, int width, int height, int r, int g, int b);

    void drawHLine(String monitorId, int x, int y, int length, int r, int g, int b);

    void drawVLine(String monitorId, int x, int y, int length, int r, int g, int b);

    void drawRect(String monitorId, int x, int y, int width, int height, int r, int g, int b);

    void drawChar(String monitorId, int x, int y, char c, int fgR, int fgG, int fgB, int bgR, int bgG, int bgB, int scale);

    int drawText(String monitorId, int x, int y, String text, int fgR, int fgG, int fgB, int bgR, int bgG, int bgB, int scale);

    int[] measureText(String monitorId, String text, int scale);

    void copyRegion(String monitorId, int srcX, int srcY, int width, int height, int dstX, int dstY);

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
            ),
            new HostFunction(
                "env",
                "monitor_fill_rect",
                List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
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
                    int width = (int) args[4];
                    int height = (int) args[5];
                    int r = (int) args[6];
                    int g = (int) args[7];
                    int b = (int) args[8];

                    fillRect(monitorId, x, y, width, height, r, g, b);
                    return null;
                }
            ),
            new HostFunction(
                "env",
                "monitor_draw_hline",
                List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
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
                    int length = (int) args[4];
                    int r = (int) args[5];
                    int g = (int) args[6];
                    int b = (int) args[7];

                    drawHLine(monitorId, x, y, length, r, g, b);
                    return null;
                }
            ),
            new HostFunction(
                "env",
                "monitor_draw_vline",
                List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
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
                    int length = (int) args[4];
                    int r = (int) args[5];
                    int g = (int) args[6];
                    int b = (int) args[7];

                    drawVLine(monitorId, x, y, length, r, g, b);
                    return null;
                }
            ),
            new HostFunction(
                "env",
                "monitor_draw_rect",
                List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
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
                    int width = (int) args[4];
                    int height = (int) args[5];
                    int r = (int) args[6];
                    int g = (int) args[7];
                    int b = (int) args[8];

                    drawRect(monitorId, x, y, width, height, r, g, b);
                    return null;
                }
            ),
            new HostFunction(
                "env",
                "monitor_draw_char",
                List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
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
                    char c = (char) args[4];
                    int fgR = (int) args[5];
                    int fgG = (int) args[6];
                    int fgB = (int) args[7];
                    int bgR = (int) args[8];
                    int bgG = (int) args[9];
                    int bgB = (int) args[10];
                    int scale = (int) args[11];

                    drawChar(monitorId, x, y, c, fgR, fgG, fgB, bgR, bgG, bgB, scale);
                    return null;
                }
            ),
            new HostFunction(
                "env",
                "monitor_draw_text",
                List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
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
                    int textPtr = (int) args[4];
                    int textLen = (int) args[5];
                    byte[] textBytes = new byte[textLen];
                    for (int i = 0; i < textLen; i++) {
                        textBytes[i] = (byte) instance.memory().read(textPtr + i);
                    }
                    String text = new String(textBytes);

                    int fgR = (int) args[6];
                    int fgG = (int) args[7];
                    int fgB = (int) args[8];
                    int bgR = (int) args[9];
                    int bgG = (int) args[10];
                    int bgB = (int) args[11];
                    int scale = (int) args[12];

                    int width = drawText(monitorId, x, y, text, fgR, fgG, fgB, bgR, bgG, bgB, scale);
                    return new long[] { width };
                }
            ),
            new HostFunction(
                "env",
                "monitor_measure_text",
                List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
                List.of(ValueType.I32),
                (instance, args) -> {
                    int idPtr = (int) args[0];
                    int idLen = (int) args[1];
                    byte[] idBytes = new byte[idLen];
                    for (int i = 0; i < idLen; i++) {
                        idBytes[i] = (byte) instance.memory().read(idPtr + i);
                    }
                    String monitorId = new String(idBytes);

                    int textPtr = (int) args[2];
                    int textLen = (int) args[3];
                    byte[] textBytes = new byte[textLen];
                    for (int i = 0; i < textLen; i++) {
                        textBytes[i] = (byte) instance.memory().read(textPtr + i);
                    }
                    String text = new String(textBytes);

                    int scale = (int) args[4];

                    int[] dimensions = measureText(monitorId, text, scale);

                    int resultPtr = 24576;
                    instance.memory().writeI32(resultPtr, dimensions[0]);
                    instance.memory().writeI32(resultPtr + 4, dimensions[1]);

                    return new long[] { resultPtr };
                }
            ),
            new HostFunction(
                "env",
                "monitor_copy_region",
                List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
                List.of(),
                (instance, args) -> {
                    int idPtr = (int) args[0];
                    int idLen = (int) args[1];
                    byte[] idBytes = new byte[idLen];
                    for (int i = 0; i < idLen; i++) {
                        idBytes[i] = (byte) instance.memory().read(idPtr + i);
                    }
                    String monitorId = new String(idBytes);

                    int srcX = (int) args[2];
                    int srcY = (int) args[3];
                    int width = (int) args[4];
                    int height = (int) args[5];
                    int dstX = (int) args[6];
                    int dstY = (int) args[7];

                    copyRegion(monitorId, srcX, srcY, width, height, dstX, dstY);
                    return null;
                }
            )
        };
    }
}
