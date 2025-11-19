package inventory

import (
	"unsafe"

	"github.com/wasmcraft/bindings/errors"
	"github.com/wasmcraft/bindings/memory"
	"github.com/wasmcraft/bindings/sides"
)

//go:wasmimport env inventory_get_selected_slot
func getSelectedSlotRaw() uint32

//go:wasmimport env inventory_set_selected_slot
func setSelectedSlotRaw(slot int32) uint32

//go:wasmimport env inventory_get_size
func getSizeRaw() uint32

//go:wasmimport env inventory_get_item
func getItemRaw(slot int32) uint32

//go:wasmimport env item_scan
func scanRaw(side int32) uint32

//go:wasmimport env item_suck
func suckRaw(side int32) uint32

//go:wasmimport env item_drop
func dropRaw(slot int32, count int32) uint32

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

func Scan(side sides.Side) ([]Item, error) {
	resultPtr := scanRaw(int32(side))
	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	if err := errors.NewError(int(errorCode)); err != nil {
		return nil, err
	}

	itemCount := *(*int32)(unsafe.Pointer(uintptr(memory.INVENTORY_RESULT_PTR + 4)))
	jsonLen := *(*int32)(unsafe.Pointer(uintptr(memory.INVENTORY_RESULT_PTR + 8)))

	if jsonLen == 0 || itemCount == 0 {
		return []Item{}, nil
	}

	jsonBytes := make([]byte, jsonLen)
	for i := 0; i < int(jsonLen); i++ {
		jsonBytes[i] = *(*byte)(unsafe.Pointer(uintptr(memory.INVENTORY_ITEM_ID_PTR + uintptr(i))))
	}

	items := parseItemJSON(string(jsonBytes))
	return items, nil
}

func Suck(side sides.Side) (int, error) {
	resultPtr := suckRaw(int32(side))
	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))

	itemsCollected := *(*int32)(unsafe.Pointer(uintptr(memory.INVENTORY_RESULT_PTR + 4)))

	if errorCode == errors.ERR_INVENTORY_NO_SPACE {
		return int(itemsCollected), &errors.WasmError{
			Code:    errors.ERR_INVENTORY_NO_SPACE,
			Message: errors.GetErrorMessage(errors.ERR_INVENTORY_NO_SPACE),
		}
	}

	if err := errors.NewError(int(errorCode)); err != nil {
		return 0, err
	}

	return int(itemsCollected), nil
}

func Drop(slot int, count int) (int, error) {
	resultPtr := dropRaw(int32(slot), int32(count))
	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	if err := errors.NewError(int(errorCode)); err != nil {
		return 0, err
	}

	actualCount := *(*int32)(unsafe.Pointer(uintptr(memory.INVENTORY_RESULT_PTR + 4)))
	return int(actualCount), nil
}

func parseItemJSON(jsonStr string) []Item {
	var items []Item

	if len(jsonStr) < 2 || jsonStr[0] != '[' {
		return items
	}

	jsonStr = jsonStr[1 : len(jsonStr)-1]

	if len(jsonStr) == 0 {
		return items
	}

	inString := false
	inID := false
	depth := 0
	currentID := ""
	currentCount := 0
	numStr := ""

	for i := 0; i < len(jsonStr); i++ {
		c := jsonStr[i]

		switch c {
		case '{':
			if !inString {
				depth++
				currentID = ""
				currentCount = 0
			}
		case '}':
			if !inString {
				depth--
				if numStr != "" {
					currentCount = parseSimpleInt(numStr)
					numStr = ""
				}
				if depth == 0 && currentID != "" {
					items = append(items, Item{ID: currentID, Count: currentCount})
				}
			}
		case '"':
			inString = !inString
			if !inString && inID {
				inID = false
			} else if inString && i > 0 && jsonStr[i-1] == ':' {
				inID = true
			}
		case ':':
			if !inString {
				if i+1 < len(jsonStr) && jsonStr[i+1] >= '0' && jsonStr[i+1] <= '9' {
					numStr = ""
				}
			}
		case ',':
			if !inString && depth == 1 {
				if numStr != "" {
					currentCount = parseSimpleInt(numStr)
					numStr = ""
				}
			}
		default:
			if inID && inString {
				currentID += string(c)
			} else if !inString && c >= '0' && c <= '9' {
				numStr += string(c)
			}
		}
	}

	return items
}

func parseSimpleInt(s string) int {
	result := 0
	for i := 0; i < len(s); i++ {
		if s[i] >= '0' && s[i] <= '9' {
			result = result*10 + int(s[i]-'0')
		}
	}
	return result
}
