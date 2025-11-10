package uk.co.techarchitect.wasmcraft.wasm.context;

import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.types.ValueType;
import uk.co.techarchitect.wasmcraft.wasm.WasmContext;
import uk.co.techarchitect.wasmcraft.wasm.WasmErrorHelper;

import java.util.List;

import static uk.co.techarchitect.wasmcraft.wasm.WasmErrorCodes.*;

public interface MonitorContext extends WasmContext {
    int setPixel(String monitorId, int x, int y, int r, int g, int b);
    int getPixel(String monitorId, int x, int y, int[] outRgb);
    int clear(String monitorId, int r, int g, int b);
    int getSize(String monitorId, int[] outSize);
    int setResolution(String monitorId, int width, int height);
    int fillRect(String monitorId, int x, int y, int width, int height, int r, int g, int b);
    int drawHLine(String monitorId, int x, int y, int length, int r, int g, int b);
    int drawVLine(String monitorId, int x, int y, int length, int r, int g, int b);
    int drawRect(String monitorId, int x, int y, int width, int height, int r, int g, int b);
    int drawChar(String monitorId, int x, int y, char c, int fgR, int fgG, int fgB, int bgR, int bgG, int bgB, int scale);
    int drawText(String monitorId, int x, int y, String text, int fgR, int fgG, int fgB, int bgR, int bgG, int bgB, int scale, int[] outWidth);
    int measureText(String monitorId, String text, int scale, int[] outDimensions);
    int copyRegion(String monitorId, int srcX, int srcY, int width, int height, int dstX, int dstY);

    @Override
    default HostFunction[] toHostFunctions() {
        return new HostFunction[] {
            createSetPixelFunction(),
            createGetPixelFunction(),
            createClearFunction(),
            createGetSizeFunction(),
            createSetResolutionFunction(),
            createFillRectFunction(),
            createDrawHLineFunction(),
            createDrawVLineFunction(),
            createDrawRectFunction(),
            createDrawCharFunction(),
            createDrawTextFunction(),
            createMeasureTextFunction(),
            createCopyRegionFunction()
        };
    }

    default HostFunction createSetPixelFunction() {
        return new HostFunction(
            "env",
            "monitor_set_pixel",
            List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
            List.of(ValueType.I32),
            (instance, args) -> {
                String monitorId = WasmErrorHelper.readString(instance, (int) args[0], (int) args[1]);
                int errorCode = setPixel(monitorId, (int) args[2], (int) args[3], (int) args[4], (int) args[5], (int) args[6]);
                if (errorCode != SUCCESS) {
                    WasmErrorHelper.writeErrorMessage(instance, getMonitorErrorMessage(errorCode, monitorId));
                }
                return new long[] { errorCode };
            }
        );
    }

    default HostFunction createGetPixelFunction() {
        return new HostFunction(
            "env",
            "monitor_get_pixel",
            List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
            List.of(ValueType.I32),
            (instance, args) -> {
                String monitorId = WasmErrorHelper.readString(instance, (int) args[0], (int) args[1]);
                int[] rgb = new int[3];
                int errorCode = getPixel(monitorId, (int) args[2], (int) args[3], rgb);

                int resultPtr = 16384;
                instance.memory().writeI32(resultPtr, errorCode);
                instance.memory().writeI32(resultPtr + 4, rgb[0]);
                instance.memory().writeI32(resultPtr + 8, rgb[1]);
                instance.memory().writeI32(resultPtr + 12, rgb[2]);

                if (errorCode != SUCCESS) {
                    WasmErrorHelper.writeErrorMessage(instance, getMonitorErrorMessage(errorCode, monitorId));
                }
                return new long[] { resultPtr };
            }
        );
    }

    default HostFunction createClearFunction() {
        return new HostFunction(
            "env",
            "monitor_clear",
            List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
            List.of(ValueType.I32),
            (instance, args) -> {
                String monitorId = WasmErrorHelper.readString(instance, (int) args[0], (int) args[1]);
                int errorCode = clear(monitorId, (int) args[2], (int) args[3], (int) args[4]);
                if (errorCode != SUCCESS) {
                    WasmErrorHelper.writeErrorMessage(instance, getMonitorErrorMessage(errorCode, monitorId));
                }
                return new long[] { errorCode };
            }
        );
    }

    default HostFunction createGetSizeFunction() {
        return new HostFunction(
            "env",
            "monitor_get_size",
            List.of(ValueType.I32, ValueType.I32),
            List.of(ValueType.I32),
            (instance, args) -> {
                String monitorId = WasmErrorHelper.readString(instance, (int) args[0], (int) args[1]);
                int[] size = new int[2];
                int errorCode = getSize(monitorId, size);

                int resultPtr = 20480;
                instance.memory().writeI32(resultPtr, errorCode);
                instance.memory().writeI32(resultPtr + 4, size[0]);
                instance.memory().writeI32(resultPtr + 8, size[1]);

                if (errorCode != SUCCESS) {
                    WasmErrorHelper.writeErrorMessage(instance, getMonitorErrorMessage(errorCode, monitorId));
                }
                return new long[] { resultPtr };
            }
        );
    }

    default HostFunction createSetResolutionFunction() {
        return new HostFunction(
            "env",
            "monitor_set_resolution",
            List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
            List.of(ValueType.I32),
            (instance, args) -> {
                String monitorId = WasmErrorHelper.readString(instance, (int) args[0], (int) args[1]);
                int errorCode = setResolution(monitorId, (int) args[2], (int) args[3]);
                if (errorCode != SUCCESS) {
                    WasmErrorHelper.writeErrorMessage(instance, getMonitorErrorMessage(errorCode, monitorId));
                }
                return new long[] { errorCode };
            }
        );
    }

    default HostFunction createFillRectFunction() {
        return new HostFunction(
            "env",
            "monitor_fill_rect",
            List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
            List.of(ValueType.I32),
            (instance, args) -> {
                String monitorId = WasmErrorHelper.readString(instance, (int) args[0], (int) args[1]);
                int errorCode = fillRect(monitorId, (int) args[2], (int) args[3], (int) args[4], (int) args[5], (int) args[6], (int) args[7], (int) args[8]);
                if (errorCode != SUCCESS) {
                    WasmErrorHelper.writeErrorMessage(instance, getMonitorErrorMessage(errorCode, monitorId));
                }
                return new long[] { errorCode };
            }
        );
    }

    default HostFunction createDrawHLineFunction() {
        return new HostFunction(
            "env",
            "monitor_draw_hline",
            List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
            List.of(ValueType.I32),
            (instance, args) -> {
                String monitorId = WasmErrorHelper.readString(instance, (int) args[0], (int) args[1]);
                int errorCode = drawHLine(monitorId, (int) args[2], (int) args[3], (int) args[4], (int) args[5], (int) args[6], (int) args[7]);
                if (errorCode != SUCCESS) {
                    WasmErrorHelper.writeErrorMessage(instance, getMonitorErrorMessage(errorCode, monitorId));
                }
                return new long[] { errorCode };
            }
        );
    }

    default HostFunction createDrawVLineFunction() {
        return new HostFunction(
            "env",
            "monitor_draw_vline",
            List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
            List.of(ValueType.I32),
            (instance, args) -> {
                String monitorId = WasmErrorHelper.readString(instance, (int) args[0], (int) args[1]);
                int errorCode = drawVLine(monitorId, (int) args[2], (int) args[3], (int) args[4], (int) args[5], (int) args[6], (int) args[7]);
                if (errorCode != SUCCESS) {
                    WasmErrorHelper.writeErrorMessage(instance, getMonitorErrorMessage(errorCode, monitorId));
                }
                return new long[] { errorCode };
            }
        );
    }

    default HostFunction createDrawRectFunction() {
        return new HostFunction(
            "env",
            "monitor_draw_rect",
            List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
            List.of(ValueType.I32),
            (instance, args) -> {
                String monitorId = WasmErrorHelper.readString(instance, (int) args[0], (int) args[1]);
                int errorCode = drawRect(monitorId, (int) args[2], (int) args[3], (int) args[4], (int) args[5], (int) args[6], (int) args[7], (int) args[8]);
                if (errorCode != SUCCESS) {
                    WasmErrorHelper.writeErrorMessage(instance, getMonitorErrorMessage(errorCode, monitorId));
                }
                return new long[] { errorCode };
            }
        );
    }

    default HostFunction createDrawCharFunction() {
        return new HostFunction(
            "env",
            "monitor_draw_char",
            List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
            List.of(ValueType.I32),
            (instance, args) -> {
                String monitorId = WasmErrorHelper.readString(instance, (int) args[0], (int) args[1]);
                int errorCode = drawChar(monitorId, (int) args[2], (int) args[3], (char) args[4], (int) args[5], (int) args[6], (int) args[7], (int) args[8], (int) args[9], (int) args[10], (int) args[11]);
                if (errorCode != SUCCESS) {
                    WasmErrorHelper.writeErrorMessage(instance, getMonitorErrorMessage(errorCode, monitorId));
                }
                return new long[] { errorCode };
            }
        );
    }

    default HostFunction createDrawTextFunction() {
        return new HostFunction(
            "env",
            "monitor_draw_text",
            List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
            List.of(ValueType.I32),
            (instance, args) -> {
                String monitorId = WasmErrorHelper.readString(instance, (int) args[0], (int) args[1]);
                String text = WasmErrorHelper.readString(instance, (int) args[4], (int) args[5]);
                int[] outWidth = new int[1];
                int errorCode = drawText(monitorId, (int) args[2], (int) args[3], text, (int) args[6], (int) args[7], (int) args[8], (int) args[9], (int) args[10], (int) args[11], (int) args[12], outWidth);

                int resultPtr = 32768;
                instance.memory().writeI32(resultPtr, errorCode);
                instance.memory().writeI32(resultPtr + 4, outWidth[0]);

                if (errorCode != SUCCESS) {
                    WasmErrorHelper.writeErrorMessage(instance, getMonitorErrorMessage(errorCode, monitorId));
                }
                return new long[] { resultPtr };
            }
        );
    }

    default HostFunction createMeasureTextFunction() {
        return new HostFunction(
            "env",
            "monitor_measure_text",
            List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
            List.of(ValueType.I32),
            (instance, args) -> {
                String monitorId = WasmErrorHelper.readString(instance, (int) args[0], (int) args[1]);
                String text = WasmErrorHelper.readString(instance, (int) args[2], (int) args[3]);
                int[] dimensions = new int[2];
                int errorCode = measureText(monitorId, text, (int) args[4], dimensions);

                int resultPtr = 24576;
                instance.memory().writeI32(resultPtr, errorCode);
                instance.memory().writeI32(resultPtr + 4, dimensions[0]);
                instance.memory().writeI32(resultPtr + 8, dimensions[1]);

                if (errorCode != SUCCESS) {
                    WasmErrorHelper.writeErrorMessage(instance, getMonitorErrorMessage(errorCode, monitorId));
                }
                return new long[] { resultPtr };
            }
        );
    }

    default HostFunction createCopyRegionFunction() {
        return new HostFunction(
            "env",
            "monitor_copy_region",
            List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
            List.of(ValueType.I32),
            (instance, args) -> {
                String monitorId = WasmErrorHelper.readString(instance, (int) args[0], (int) args[1]);
                int errorCode = copyRegion(monitorId, (int) args[2], (int) args[3], (int) args[4], (int) args[5], (int) args[6], (int) args[7]);
                if (errorCode != SUCCESS) {
                    WasmErrorHelper.writeErrorMessage(instance, getMonitorErrorMessage(errorCode, monitorId));
                }
                return new long[] { errorCode };
            }
        );
    }

    default String getMonitorErrorMessage(int errorCode, String monitorId) {
        return switch (errorCode) {
            case ERR_MONITOR_NOT_FOUND -> "Monitor '" + monitorId + "' not found or not connected";
            case ERR_MONITOR_DISCONNECTED -> "Monitor '" + monitorId + "' is disconnected";
            case ERR_MONITOR_OUT_OF_BOUNDS -> "Coordinates out of bounds for monitor '" + monitorId + "'";
            case ERR_MONITOR_INVALID_COLOR -> "Invalid color value (must be 0-255)";
            case ERR_MONITOR_INVALID_RESOLUTION -> "Invalid resolution value";
            case ERR_MONITOR_INVALID_SCALE -> "Invalid scale value (must be 1-8)";
            case ERR_MONITOR_INVALID_CHAR -> "Invalid character (must be ASCII 32-126)";
            default -> "Unknown monitor error: " + getErrorName(errorCode);
        };
    }
}
