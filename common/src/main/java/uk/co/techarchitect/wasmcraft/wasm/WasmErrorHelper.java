package uk.co.techarchitect.wasmcraft.wasm;

import com.dylibso.chicory.runtime.Instance;

public class WasmErrorHelper {
    public static void writeErrorMessage(Instance instance, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }

        byte[] bytes = message.getBytes();
        int maxLen = Math.min(bytes.length, WasmErrorCodes.ERROR_MESSAGE_MAX_LEN - 1);

        instance.memory().writeI32(WasmErrorCodes.ERROR_MESSAGE_PTR, maxLen);

        for (int i = 0; i < maxLen; i++) {
            instance.memory().writeByte(WasmErrorCodes.ERROR_MESSAGE_PTR + 4 + i, bytes[i]);
        }

        instance.memory().writeByte(WasmErrorCodes.ERROR_MESSAGE_PTR + 4 + maxLen, (byte) 0);
    }

    public static String readString(Instance instance, int ptr, int len) {
        if (len <= 0 || len > 65536) {
            return "";
        }
        byte[] bytes = new byte[len];
        for (int i = 0; i < len; i++) {
            bytes[i] = (byte) instance.memory().read(ptr + i);
        }
        return new String(bytes);
    }

    public static boolean isValidColor(int c) {
        return c >= 0 && c <= 255;
    }

    public static boolean isValidScale(int scale) {
        return scale >= 1 && scale <= 8;
    }

    public static boolean isValidChar(int c) {
        return c >= 32 && c <= 126;
    }
}
