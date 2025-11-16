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

func MoveForward(distance float32) (MovementResult, error) {
	if distance < 0 {
		distance = 0
	}
	errorCode := moveForward(floatToUint32(distance))
	if err := errors.NewError(int(errorCode)); err != nil {
		return MovementResult{}, err
	}
	return readMovementResult(), nil
}

func MoveBackward(distance float32) (MovementResult, error) {
	if distance < 0 {
		distance = 0
	}
	errorCode := moveBackward(floatToUint32(distance))
	if err := errors.NewError(int(errorCode)); err != nil {
		return MovementResult{}, err
	}
	return readMovementResult(), nil
}

func MoveLeft(distance float32) (MovementResult, error) {
	if distance < 0 {
		distance = 0
	}
	errorCode := moveLeft(floatToUint32(distance))
	if err := errors.NewError(int(errorCode)); err != nil {
		return MovementResult{}, err
	}
	return readMovementResult(), nil
}

func MoveRight(distance float32) (MovementResult, error) {
	if distance < 0 {
		distance = 0
	}
	errorCode := moveRight(floatToUint32(distance))
	if err := errors.NewError(int(errorCode)); err != nil {
		return MovementResult{}, err
	}
	return readMovementResult(), nil
}

func MoveUp(distance float32) (MovementResult, error) {
	if distance < 0 {
		distance = 0
	}
	errorCode := moveUp(floatToUint32(distance))
	if err := errors.NewError(int(errorCode)); err != nil {
		return MovementResult{}, err
	}
	return readMovementResult(), nil
}

func MoveDown(distance float32) (MovementResult, error) {
	if distance < 0 {
		distance = 0
	}
	errorCode := moveDown(floatToUint32(distance))
	if err := errors.NewError(int(errorCode)); err != nil {
		return MovementResult{}, err
	}
	return readMovementResult(), nil
}

func GetPosition() (Position, error) {
	resultPtr := getPositionRaw()

	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	if err := errors.NewError(int(errorCode)); err != nil {
		return Position{}, err
	}

	x := *(*float64)(unsafe.Pointer(uintptr(resultPtr + 4)))
	y := *(*float64)(unsafe.Pointer(uintptr(resultPtr + 12)))
	z := *(*float64)(unsafe.Pointer(uintptr(resultPtr + 20)))

	return Position{X: x, Y: y, Z: z}, nil
}

func Rotate(yawDegrees float32) (float32, error) {
	errorCode := rotateRaw(floatToUint32(yawDegrees))
	if err := errors.NewError(int(errorCode)); err != nil {
		return 0, err
	}

	actualYaw := *(*float32)(unsafe.Pointer(uintptr(memory.MOVEMENT_RESULT_PTR + 4)))
	return actualYaw, nil
}

func GetYaw() (float32, error) {
	errorCode := getYawRaw()
	if err := errors.NewError(int(errorCode)); err != nil {
		return 0, err
	}

	yaw := *(*float32)(unsafe.Pointer(uintptr(memory.MOVEMENT_RESULT_PTR + 4)))
	return yaw, nil
}

func SetYaw(yawDegrees float32) error {
	errorCode := setYawRaw(floatToUint32(yawDegrees))
	if err := errors.NewError(int(errorCode)); err != nil {
		return err
	}
	return nil
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
