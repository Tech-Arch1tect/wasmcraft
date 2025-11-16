package inventory

import (
	"unsafe"

	"github.com/wasmcraft/bindings/errors"
	"github.com/wasmcraft/bindings/memory"
)

//go:wasmimport env inventory_get_selected_slot
func getSelectedSlotRaw() uint32

//go:wasmimport env inventory_set_selected_slot
func setSelectedSlotRaw(slot int32) uint32

//go:wasmimport env inventory_get_size
func getSizeRaw() uint32

//go:wasmimport env inventory_get_item
func getItemRaw(slot int32) uint32

func GetSelectedSlot() int {
	resultPtr := getSelectedSlotRaw()
	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	errors.Check(int(errorCode))

	slot := *(*int32)(unsafe.Pointer(uintptr(memory.INVENTORY_RESULT_PTR + 4)))
	return int(slot)
}

func SetSelectedSlot(slot int) {
	resultPtr := setSelectedSlotRaw(int32(slot))
	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	errors.Check(int(errorCode))
}

func GetSize() int {
	resultPtr := getSizeRaw()
	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	errors.Check(int(errorCode))

	size := *(*int32)(unsafe.Pointer(uintptr(memory.INVENTORY_RESULT_PTR + 4)))
	return int(size)
}

type Item struct {
	ID    string
	Count int
}

func GetItem(slot int) Item {
	resultPtr := getItemRaw(int32(slot))
	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	errors.Check(int(errorCode))

	length := *(*int32)(unsafe.Pointer(uintptr(memory.INVENTORY_RESULT_PTR + 4)))
	count := *(*int32)(unsafe.Pointer(uintptr(memory.INVENTORY_RESULT_PTR + 8)))

	var itemID string
	if length > 0 {
		itemIDBytes := make([]byte, length)
		for i := 0; i < int(length); i++ {
			itemIDBytes[i] = *(*byte)(unsafe.Pointer(uintptr(memory.INVENTORY_ITEM_ID_PTR + uintptr(i))))
		}
		itemID = string(itemIDBytes)
	} else {
		itemID = "minecraft:air"
	}

	return Item{
		ID:    itemID,
		Count: int(count),
	}
}

func IsEmpty(slot int) bool {
	item := GetItem(slot)
	return item.ID == "minecraft:air" || item.Count == 0
}
