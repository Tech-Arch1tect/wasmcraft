package uk.co.techarchitect.wasmcraft.wasm;

public class WasmMemoryMap {

    public static final int ERROR_MESSAGE_PTR = 28672;
    public static final int ERROR_MESSAGE_MAX_LEN = 1024;
    public static final int ERROR_MESSAGE_SIZE = 4 + ERROR_MESSAGE_MAX_LEN;

    public static final int MOVEMENT_RESULT_PTR = 40960;
    public static final int MOVEMENT_RESULT_SIZE = 16;

    public static final int WORLD_RESULT_PTR = 49152;
    public static final int WORLD_BLOCK_ID_PTR = 53248;
    public static final int WORLD_BLOCK_ID_MAX_LEN = 256;
    public static final int WORLD_PROPERTY_VALUE_PTR = 53504;
    public static final int WORLD_PROPERTY_VALUE_MAX_LEN = 64;
    public static final int WORLD_TAGS_PTR = 53568;
    public static final int WORLD_TAGS_MAX_LEN = 2048;
    public static final int WORLD_PROPERTIES_PTR = 55616;
    public static final int WORLD_PROPERTIES_MAX_LEN = 1664;

    public static final int INVENTORY_RESULT_PTR = 57344;
    public static final int INVENTORY_RESULT_SIZE = 12;
    public static final int INVENTORY_ITEM_ID_PTR = 57344 + INVENTORY_RESULT_SIZE;
    public static final int INVENTORY_ITEM_ID_MAX_LEN = 256;

    public static final int REDSTONE_RESULT_PTR = 58368;

    public static final int MONITOR_RESULT_PTR = 59392;

    public static final int PERIPHERAL_RESULT_PTR = 65536;
}
