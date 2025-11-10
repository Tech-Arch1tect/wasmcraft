package peripheral

import (
	"unsafe"

	"github.com/wasmcraft/bindings/errors"
)

//go:wasmimport env peripheral_list
func peripheralList(bufPtr uint32) uint32

//go:wasmimport env peripheral_connect
func peripheralConnect(labelPtr, labelLen uint32) uint32

//go:wasmimport env peripheral_disconnect
func peripheralDisconnect(idPtr, idLen uint32) uint32

func List() string {
	buf := make([]byte, 4096)
	resultPtr := peripheralList(uint32(uintptr(unsafe.Pointer(&buf[0]))))

	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	length := *(*int32)(unsafe.Pointer(uintptr(resultPtr + 4)))

	errors.Check(int(errorCode))

	if length > 4096 {
		length = 4096
	}
	return string(buf[:length])
}

func Connect(label string) string {
	labelBytes := []byte(label)
	labelPtr := uint32(uintptr(unsafe.Pointer(&labelBytes[0])))
	labelLen := uint32(len(labelBytes))

	resultPtr := peripheralConnect(labelPtr, labelLen)

	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	errors.Check(int(errorCode))

	var resultBytes []byte
	for i := 0; i < 4096; i++ {
		b := *(*byte)(unsafe.Pointer(uintptr(resultPtr + 4 + uint32(i))))
		if b == 0 {
			break
		}
		resultBytes = append(resultBytes, b)
	}

	return string(resultBytes)
}

func Disconnect(id string) {
	idBytes := []byte(id)
	idPtr := uint32(uintptr(unsafe.Pointer(&idBytes[0])))
	idLen := uint32(len(idBytes))

	errorCode := peripheralDisconnect(idPtr, idLen)
	errors.Check(int(errorCode))
}
