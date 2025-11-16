package movement

import (
	"unsafe"

	"github.com/wasmcraft/bindings/errors"
	"github.com/wasmcraft/bindings/memory"
)

//go:wasmimport env move_forward
func moveForward(distance uint32) uint32

//go:wasmimport env move_backward
func moveBackward(distance uint32) uint32

//go:wasmimport env move_left
func moveLeft(distance uint32) uint32

//go:wasmimport env move_right
func moveRight(distance uint32) uint32

//go:wasmimport env move_up
func moveUp(distance uint32) uint32

//go:wasmimport env move_down
func moveDown(distance uint32) uint32

//go:wasmimport env get_position
func getPositionRaw() uint32

//go:wasmimport env rotate
func rotateRaw(yawDegrees uint32) uint32

//go:wasmimport env get_yaw
func getYawRaw() uint32

//go:wasmimport env set_yaw
func setYawRaw(yawDegrees uint32) uint32

type Position struct {
	X, Y, Z float64
}

type MovementResult struct {
	X, Y, Z float32
}

func MoveForward(distance float32) MovementResult {
	if distance < 0 {
		distance = 0
	}
	errorCode := moveForward(floatToUint32(distance))
	errors.Check(int(errorCode))
	return readMovementResult()
}

func MoveBackward(distance float32) MovementResult {
	if distance < 0 {
		distance = 0
	}
	errorCode := moveBackward(floatToUint32(distance))
	errors.Check(int(errorCode))
	return readMovementResult()
}

func MoveLeft(distance float32) MovementResult {
	if distance < 0 {
		distance = 0
	}
	errorCode := moveLeft(floatToUint32(distance))
	errors.Check(int(errorCode))
	return readMovementResult()
}

func MoveRight(distance float32) MovementResult {
	if distance < 0 {
		distance = 0
	}
	errorCode := moveRight(floatToUint32(distance))
	errors.Check(int(errorCode))
	return readMovementResult()
}

func MoveUp(distance float32) MovementResult {
	if distance < 0 {
		distance = 0
	}
	errorCode := moveUp(floatToUint32(distance))
	errors.Check(int(errorCode))
	return readMovementResult()
}

func MoveDown(distance float32) MovementResult {
	if distance < 0 {
		distance = 0
	}
	errorCode := moveDown(floatToUint32(distance))
	errors.Check(int(errorCode))
	return readMovementResult()
}

func GetPosition() Position {
	resultPtr := getPositionRaw()

	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	errors.Check(int(errorCode))

	x := *(*float64)(unsafe.Pointer(uintptr(resultPtr + 4)))
	y := *(*float64)(unsafe.Pointer(uintptr(resultPtr + 12)))
	z := *(*float64)(unsafe.Pointer(uintptr(resultPtr + 20)))

	return Position{X: x, Y: y, Z: z}
}

func Rotate(yawDegrees float32) float32 {
	errorCode := rotateRaw(floatToUint32(yawDegrees))
	errors.Check(int(errorCode))

	actualYaw := *(*float32)(unsafe.Pointer(uintptr(memory.MOVEMENT_RESULT_PTR + 4)))
	return actualYaw
}

func GetYaw() float32 {
	errorCode := getYawRaw()
	errors.Check(int(errorCode))

	yaw := *(*float32)(unsafe.Pointer(uintptr(memory.MOVEMENT_RESULT_PTR + 4)))
	return yaw
}

func SetYaw(yawDegrees float32) {
	errorCode := setYawRaw(floatToUint32(yawDegrees))
	errors.Check(int(errorCode))
}

func readMovementResult() MovementResult {
	x := *(*float32)(unsafe.Pointer(uintptr(memory.MOVEMENT_RESULT_PTR + 4)))
	y := *(*float32)(unsafe.Pointer(uintptr(memory.MOVEMENT_RESULT_PTR + 8)))
	z := *(*float32)(unsafe.Pointer(uintptr(memory.MOVEMENT_RESULT_PTR + 12)))

	return MovementResult{X: x, Y: y, Z: z}
}

func floatToUint32(f float32) uint32 {
	return *(*uint32)(unsafe.Pointer(&f))
}
