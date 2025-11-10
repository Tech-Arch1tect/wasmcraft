package redstone

import (
	"unsafe"

	"github.com/wasmcraft/bindings/errors"
)

//go:wasmimport env redstone_set
func redstoneSet(side, power uint32) uint32

//go:wasmimport env redstone_get
func redstoneGet(side uint32) uint32

const (
	BOTTOM = 0
	TOP    = 1
	FRONT  = 2
	BACK   = 3
	LEFT   = 4
	RIGHT  = 5
)

type Side uint32

const (
	SideBottom Side = BOTTOM
	SideTop    Side = TOP
	SideFront  Side = FRONT
	SideBack   Side = BACK
	SideLeft   Side = LEFT
	SideRight  Side = RIGHT
)

func (s Side) String() string {
	switch s {
	case SideBottom:
		return "BOTTOM"
	case SideTop:
		return "TOP"
	case SideFront:
		return "FRONT"
	case SideBack:
		return "BACK"
	case SideLeft:
		return "LEFT"
	case SideRight:
		return "RIGHT"
	default:
		return "UNKNOWN"
	}
}

func SetRedstone(side Side, power int) {
	if power < 0 {
		power = 0
	}
	if power > 15 {
		power = 15
	}
	errorCode := redstoneSet(uint32(side), uint32(power))
	errors.Check(int(errorCode))
}

func GetRedstone(side Side) int {
	resultPtr := redstoneGet(uint32(side))

	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	power := *(*int32)(unsafe.Pointer(uintptr(resultPtr + 4)))

	errors.Check(int(errorCode))
	return int(power)
}
