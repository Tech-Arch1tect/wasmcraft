package world

import (
	"unsafe"

	"github.com/wasmcraft/bindings/errors"
)

const (
	WORLD_RESULT_PTR     = 49152
	WORLD_BLOCK_ID_PTR   = 53248
	WORLD_BLOCK_ID_MAX_LEN = 256

	BOTTOM = 0
	TOP    = 1
	FRONT  = 2
	BACK   = 3
	LEFT   = 4
	RIGHT  = 5
)

//go:wasmimport env world_get_block
func getBlockRaw(side int32) uint32

func GetBlock(side int) string {
	resultPtr := getBlockRaw(int32(side))
	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	errors.Check(int(errorCode))

	length := *(*int32)(unsafe.Pointer(uintptr(resultPtr + 4)))
	if length == 0 {
		return ""
	}

	blockIdBytes := make([]byte, length)
	for i := 0; i < int(length); i++ {
		blockIdBytes[i] = *(*byte)(unsafe.Pointer(uintptr(WORLD_BLOCK_ID_PTR + uintptr(i))))
	}

	return string(blockIdBytes)
}
