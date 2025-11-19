package redstone

import (
	"unsafe"

	"github.com/wasmcraft/bindings/errors"
	"github.com/wasmcraft/bindings/sides"
)

//go:wasmimport env redstone_set
func redstoneSet(side, power uint32) uint32

//go:wasmimport env redstone_get
func redstoneGet(side uint32) uint32

func SetRedstone(side sides.Side, power int) error {
	if power < 0 {
		power = 0
	}
	if power > 15 {
		power = 15
	}
	errorCode := redstoneSet(uint32(side), uint32(power))
	if err := errors.NewError(int(errorCode)); err != nil {
		return err
	}
	return nil
}

func GetRedstone(side sides.Side) (int, error) {
	resultPtr := redstoneGet(uint32(side))

	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	power := *(*int32)(unsafe.Pointer(uintptr(resultPtr + 4)))

	if err := errors.NewError(int(errorCode)); err != nil {
		return 0, err
	}
	return int(power), nil
}
