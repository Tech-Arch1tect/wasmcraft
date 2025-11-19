package inventory

import (
	"unsafe"

	"github.com/wasmcraft/bindings/errors"
	"github.com/wasmcraft/bindings/memory"
	"github.com/wasmcraft/bindings/sides"
)

//go:wasmimport env inventory_detect
func detectInventoryRaw(side int32) uint32

//go:wasmimport env inventory_external_size
func externalSizeRaw(side int32) uint32

//go:wasmimport env inventory_external_get_item
func externalGetItemRaw(side int32, slot int32) uint32

//go:wasmimport env inventory_push
func pushItemRaw(side int32, droneSlot int32, externalSlot int32, count int32) uint32

//go:wasmimport env inventory_pull
func pullItemRaw(side int32, externalSlot int32, droneSlot int32, count int32) uint32

func DetectInventory(side sides.Side) (bool, error) {
	resultPtr := detectInventoryRaw(int32(side))
	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	if err := errors.NewError(int(errorCode)); err != nil {
		return false, err
	}

	hasInventory := *(*int32)(unsafe.Pointer(uintptr(memory.INVENTORY_RESULT_PTR + 4)))
	return hasInventory == 1, nil
}

func GetExternalSize(side sides.Side) (int, error) {
	resultPtr := externalSizeRaw(int32(side))
	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	if err := errors.NewError(int(errorCode)); err != nil {
		return 0, err
	}

	size := *(*int32)(unsafe.Pointer(uintptr(memory.INVENTORY_RESULT_PTR + 4)))
	return int(size), nil
}

func GetExternalItem(side sides.Side, slot int) (Item, error) {
	resultPtr := externalGetItemRaw(int32(side), int32(slot))
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

func PushItem(side sides.Side, droneSlot int, externalSlot int, count int) (int, error) {
	resultPtr := pushItemRaw(int32(side), int32(droneSlot), int32(externalSlot), int32(count))
	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	if err := errors.NewError(int(errorCode)); err != nil {
		return 0, err
	}

	actualCount := *(*int32)(unsafe.Pointer(uintptr(memory.INVENTORY_RESULT_PTR + 4)))
	return int(actualCount), nil
}

func PullItem(side sides.Side, externalSlot int, droneSlot int, count int) (int, error) {
	resultPtr := pullItemRaw(int32(side), int32(externalSlot), int32(droneSlot), int32(count))
	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	if err := errors.NewError(int(errorCode)); err != nil {
		return 0, err
	}

	actualCount := *(*int32)(unsafe.Pointer(uintptr(memory.INVENTORY_RESULT_PTR + 4)))
	return int(actualCount), nil
}
