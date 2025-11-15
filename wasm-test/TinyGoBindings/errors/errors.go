package errors

import "unsafe"

const (
	SUCCESS = 0

	// Monitor errors (1-19)
	ERR_MONITOR_NOT_FOUND          = 1
	ERR_MONITOR_DISCONNECTED       = 2
	ERR_MONITOR_OUT_OF_BOUNDS      = 3
	ERR_MONITOR_INVALID_COLOR      = 4
	ERR_MONITOR_INVALID_RESOLUTION = 5
	ERR_MONITOR_INVALID_SCALE      = 6
	ERR_MONITOR_INVALID_CHAR       = 7

	// Peripheral errors (20-39)
	ERR_PERIPHERAL_NOT_FOUND     = 20
	ERR_PERIPHERAL_OUT_OF_RANGE  = 21
	ERR_PERIPHERAL_NOT_CONNECTED = 22

	// Redstone errors (40-59)
	ERR_REDSTONE_INVALID_SIDE  = 40
	ERR_REDSTONE_INVALID_POWER = 41

	// General errors (60-79)
	ERR_INVALID_PARAMETER = 60
	ERR_INVALID_STRING    = 61
	ERR_BUFFER_TOO_SMALL  = 62

	// Movement errors (80-99)
	ERR_MOVEMENT_COLLISION        = 80
	ERR_MOVEMENT_OUT_OF_WORLD     = 81
	ERR_MOVEMENT_INVALID_DISTANCE = 82
	ERR_MOVEMENT_NOT_SUPPORTED    = 83
	ERR_MOVEMENT_IN_PROGRESS      = 84

	// Memory address for error messages
	ERROR_MESSAGE_PTR     = 28672
	ERROR_MESSAGE_MAX_LEN = 1024
)

type WasmError struct {
	Code    int
	Message string
}

func (e *WasmError) Error() string {
	return e.Message
}

func NewError(errorCode int) error {
	if errorCode == SUCCESS {
		return nil
	}
	return &WasmError{
		Code:    errorCode,
		Message: GetErrorMessage(errorCode),
	}
}

func Check(errorCode int) {
	if errorCode != SUCCESS {
		panic(GetErrorMessage(errorCode))
	}
}

func GetErrorMessage(errorCode int) string {
	if errorCode == SUCCESS {
		return ""
	}

	msgLen := *(*int32)(unsafe.Pointer(uintptr(ERROR_MESSAGE_PTR)))
	if msgLen <= 0 || msgLen > ERROR_MESSAGE_MAX_LEN {
		return getDefaultErrorMessage(errorCode)
	}

	msgBytes := make([]byte, msgLen)
	for i := int32(0); i < msgLen; i++ {
		msgBytes[i] = *(*byte)(unsafe.Pointer(uintptr(ERROR_MESSAGE_PTR + 4 + int(i))))
	}

	return string(msgBytes)
}

func getDefaultErrorMessage(errorCode int) string {
	switch errorCode {
	case ERR_MONITOR_NOT_FOUND:
		return "Monitor not found or not connected"
	case ERR_MONITOR_DISCONNECTED:
		return "Monitor is disconnected"
	case ERR_MONITOR_OUT_OF_BOUNDS:
		return "Coordinates out of bounds"
	case ERR_MONITOR_INVALID_COLOR:
		return "Invalid color value (must be 0-255)"
	case ERR_MONITOR_INVALID_RESOLUTION:
		return "Invalid resolution value"
	case ERR_MONITOR_INVALID_SCALE:
		return "Invalid scale value (must be 1-8)"
	case ERR_MONITOR_INVALID_CHAR:
		return "Invalid character (must be ASCII 32-126)"
	case ERR_PERIPHERAL_NOT_FOUND:
		return "Peripheral not found"
	case ERR_PERIPHERAL_OUT_OF_RANGE:
		return "Peripheral out of range (max 16 blocks)"
	case ERR_PERIPHERAL_NOT_CONNECTED:
		return "Peripheral not connected"
	case ERR_REDSTONE_INVALID_SIDE:
		return "Invalid redstone side (must be 0-5)"
	case ERR_REDSTONE_INVALID_POWER:
		return "Invalid redstone power (must be 0-15)"
	case ERR_MOVEMENT_COLLISION:
		return "Movement blocked by collision"
	case ERR_MOVEMENT_OUT_OF_WORLD:
		return "Movement would take entity out of world bounds"
	case ERR_MOVEMENT_INVALID_DISTANCE:
		return "Invalid movement distance (must be >= 0)"
	case ERR_MOVEMENT_NOT_SUPPORTED:
		return "Movement not supported by this computer type"
	case ERR_MOVEMENT_IN_PROGRESS:
		return "Cannot start movement while another movement is in progress"
	case ERR_INVALID_PARAMETER:
		return "Invalid parameter"
	case ERR_INVALID_STRING:
		return "Invalid string"
	case ERR_BUFFER_TOO_SMALL:
		return "Buffer too small"
	default:
		return "Unknown error"
	}
}
