package peripheral

import "unsafe"

//go:wasmimport env peripheral_list
func peripheralList(bufPtr uint32) uint32

//go:wasmimport env peripheral_connect
func peripheralConnect(labelPtr, labelLen uint32) uint32

//go:wasmimport env peripheral_disconnect
func peripheralDisconnect(idPtr, idLen uint32)

func List() string {
	buf := make([]byte, 4096)
	length := peripheralList(uint32(uintptr(unsafe.Pointer(&buf[0]))))
	if length > 4096 {
		length = 4096
	}
	return string(buf[:length])
}

func Connect(label string) string {
	labelBytes := []byte(label)
	labelPtr := uint32(uintptr(unsafe.Pointer(&labelBytes[0])))
	labelLen := uint32(len(labelBytes))

	resultLen := peripheralConnect(labelPtr, labelLen)

	resultBuf := make([]byte, resultLen)
	resultPtr := uint32(8192)
	for i := uint32(0); i < resultLen; i++ {
		resultBuf[i] = *(*byte)(unsafe.Pointer(uintptr(resultPtr + i)))
	}

	return string(resultBuf)
}

func Disconnect(id string) {
	idBytes := []byte(id)
	idPtr := uint32(uintptr(unsafe.Pointer(&idBytes[0])))
	idLen := uint32(len(idBytes))
	peripheralDisconnect(idPtr, idLen)
}
