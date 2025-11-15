package uk.co.techarchitect.wasmcraft.wasm;

public class WasmErrorCodes {
    public static final int SUCCESS = 0;

    // Monitor errors (1-19)
    public static final int ERR_MONITOR_NOT_FOUND = 1;
    public static final int ERR_MONITOR_DISCONNECTED = 2;
    public static final int ERR_MONITOR_OUT_OF_BOUNDS = 3;
    public static final int ERR_MONITOR_INVALID_COLOR = 4;
    public static final int ERR_MONITOR_INVALID_RESOLUTION = 5;
    public static final int ERR_MONITOR_INVALID_SCALE = 6;
    public static final int ERR_MONITOR_INVALID_CHAR = 7;

    // Peripheral errors (20-39)
    public static final int ERR_PERIPHERAL_NOT_FOUND = 20;
    public static final int ERR_PERIPHERAL_OUT_OF_RANGE = 21;
    public static final int ERR_PERIPHERAL_NOT_CONNECTED = 22;

    // Redstone errors (40-59)
    public static final int ERR_REDSTONE_INVALID_SIDE = 40;
    public static final int ERR_REDSTONE_INVALID_POWER = 41;

    // General errors (60-79)
    public static final int ERR_INVALID_PARAMETER = 60;
    public static final int ERR_INVALID_STRING = 61;
    public static final int ERR_BUFFER_TOO_SMALL = 62;

    // Movement errors (80-99)
    public static final int ERR_MOVEMENT_COLLISION = 80;
    public static final int ERR_MOVEMENT_OUT_OF_WORLD = 81;
    public static final int ERR_MOVEMENT_INVALID_DISTANCE = 82;
    public static final int ERR_MOVEMENT_NOT_SUPPORTED = 83;
    public static final int ERR_MOVEMENT_IN_PROGRESS = 84;

    public static final int ERROR_MESSAGE_PTR = 28672;
    public static final int ERROR_MESSAGE_MAX_LEN = 1024;

    public static String getErrorName(int code) {
        return switch (code) {
            case SUCCESS -> "SUCCESS";
            case ERR_MONITOR_NOT_FOUND -> "ERR_MONITOR_NOT_FOUND";
            case ERR_MONITOR_DISCONNECTED -> "ERR_MONITOR_DISCONNECTED";
            case ERR_MONITOR_OUT_OF_BOUNDS -> "ERR_MONITOR_OUT_OF_BOUNDS";
            case ERR_MONITOR_INVALID_COLOR -> "ERR_MONITOR_INVALID_COLOR";
            case ERR_MONITOR_INVALID_RESOLUTION -> "ERR_MONITOR_INVALID_RESOLUTION";
            case ERR_MONITOR_INVALID_SCALE -> "ERR_MONITOR_INVALID_SCALE";
            case ERR_MONITOR_INVALID_CHAR -> "ERR_MONITOR_INVALID_CHAR";
            case ERR_PERIPHERAL_NOT_FOUND -> "ERR_PERIPHERAL_NOT_FOUND";
            case ERR_PERIPHERAL_OUT_OF_RANGE -> "ERR_PERIPHERAL_OUT_OF_RANGE";
            case ERR_PERIPHERAL_NOT_CONNECTED -> "ERR_PERIPHERAL_NOT_CONNECTED";
            case ERR_REDSTONE_INVALID_SIDE -> "ERR_REDSTONE_INVALID_SIDE";
            case ERR_REDSTONE_INVALID_POWER -> "ERR_REDSTONE_INVALID_POWER";
            case ERR_MOVEMENT_COLLISION -> "ERR_MOVEMENT_COLLISION";
            case ERR_MOVEMENT_OUT_OF_WORLD -> "ERR_MOVEMENT_OUT_OF_WORLD";
            case ERR_MOVEMENT_INVALID_DISTANCE -> "ERR_MOVEMENT_INVALID_DISTANCE";
            case ERR_MOVEMENT_NOT_SUPPORTED -> "ERR_MOVEMENT_NOT_SUPPORTED";
            case ERR_MOVEMENT_IN_PROGRESS -> "ERR_MOVEMENT_IN_PROGRESS";
            case ERR_INVALID_PARAMETER -> "ERR_INVALID_PARAMETER";
            case ERR_INVALID_STRING -> "ERR_INVALID_STRING";
            case ERR_BUFFER_TOO_SMALL -> "ERR_BUFFER_TOO_SMALL";
            default -> "UNKNOWN_ERROR_" + code;
        };
    }
}
