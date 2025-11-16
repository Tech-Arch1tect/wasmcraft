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

func GetSelectedSlot() (int, error) {
	resultPtr := getSelectedSlotRaw()
	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	if err := errors.NewError(int(errorCode)); err != nil {
		return 0, err
	}

	slot := *(*int32)(unsafe.Pointer(uintptr(memory.INVENTORY_RESULT_PTR + 4)))
	return int(slot), nil
}

func SetSelectedSlot(slot int) error {
	resultPtr := setSelectedSlotRaw(int32(slot))
	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	if err := errors.NewError(int(errorCode)); err != nil {
		return err
	}
	return nil
}

func GetSize() (int, error) {
	resultPtr := getSizeRaw()
	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	if err := errors.NewError(int(errorCode)); err != nil {
		return 0, err
	}

	size := *(*int32)(unsafe.Pointer(uintptr(memory.INVENTORY_RESULT_PTR + 4)))
	return int(size), nil
}

type Item struct {
	ID    string
	Count int
}

func GetItem(slot int) (Item, error) {
	resultPtr := getItemRaw(int32(slot))
	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	if err := errors.NewError(int(errorCode)); err != nil {
		return Item{}, err
	}

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
	}, nil
}

func IsEmpty(slot int) (bool, error) {
	item, err := GetItem(slot)
	if err != nil {
		return false, err
	}
	return item.ID == "minecraft:air" || item.Count == 0, nil
}
