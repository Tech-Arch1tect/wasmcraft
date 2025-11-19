package world

import (
	"encoding/json"
	"unsafe"

	"github.com/wasmcraft/bindings/errors"
	"github.com/wasmcraft/bindings/memory"
	"github.com/wasmcraft/bindings/sides"
)

//go:wasmimport env world_get_block
func getBlockRaw(side int32) uint32

//go:wasmimport env world_get_block_property
func getBlockPropertyRaw(side int32, propertyNamePtr int32, propertyNameLen int32) uint32

//go:wasmimport env world_has_block_tag
func hasBlockTagRaw(side int32, tagNamePtr int32, tagNameLen int32) uint32

//go:wasmimport env world_get_block_tags
func getBlockTagsRaw(side int32) uint32

//go:wasmimport env world_get_block_properties
func getBlockPropertiesRaw(side int32) uint32

//go:wasmimport env world_can_break
func canBreakRaw(side int32) uint32

//go:wasmimport env world_break_block
func breakBlockRaw(side int32) uint32

//go:wasmimport env world_place_block
func placeBlockRaw(side int32) uint32

func GetBlock(side sides.Side) (string, error) {
	resultPtr := getBlockRaw(int32(side))
	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	if err := errors.NewError(int(errorCode)); err != nil {
		return "", err
	}

	length := *(*int32)(unsafe.Pointer(uintptr(resultPtr + 4)))
	if length == 0 {
		return "", nil
	}

	blockIdBytes := make([]byte, length)
	for i := 0; i < int(length); i++ {
		blockIdBytes[i] = *(*byte)(unsafe.Pointer(uintptr(memory.WORLD_BLOCK_ID_PTR + uintptr(i))))
	}

	return string(blockIdBytes), nil
}

func GetBlockProperty(side sides.Side, propertyName string) (string, error) {
	propertyNameBytes := []byte(propertyName)
	propertyNamePtr := uintptr(unsafe.Pointer(&propertyNameBytes[0]))

	resultPtr := getBlockPropertyRaw(int32(side), int32(propertyNamePtr), int32(len(propertyNameBytes)))
	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	if err := errors.NewError(int(errorCode)); err != nil {
		return "", err
	}

	length := *(*int32)(unsafe.Pointer(uintptr(resultPtr + 4)))
	if length == 0 {
		return "", nil
	}

	valueBytes := make([]byte, length)
	for i := 0; i < int(length); i++ {
		valueBytes[i] = *(*byte)(unsafe.Pointer(uintptr(memory.WORLD_PROPERTY_VALUE_PTR + uintptr(i))))
	}

	return string(valueBytes), nil
}

func HasBlockTag(side sides.Side, tagName string) (bool, error) {
	tagNameBytes := []byte(tagName)
	tagNamePtr := uintptr(unsafe.Pointer(&tagNameBytes[0]))

	resultPtr := hasBlockTagRaw(int32(side), int32(tagNamePtr), int32(len(tagNameBytes)))
	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	if err := errors.NewError(int(errorCode)); err != nil {
		return false, err
	}

	hasTag := *(*int32)(unsafe.Pointer(uintptr(resultPtr + 4)))
	return hasTag != 0, nil
}

func GetBlockTags(side sides.Side) ([]string, error) {
	resultPtr := getBlockTagsRaw(int32(side))
	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	if err := errors.NewError(int(errorCode)); err != nil {
		return nil, err
	}

	length := *(*int32)(unsafe.Pointer(uintptr(resultPtr + 4)))
	if length == 0 {
		return []string{}, nil
	}

	jsonBytes := make([]byte, length)
	for i := 0; i < int(length); i++ {
		jsonBytes[i] = *(*byte)(unsafe.Pointer(uintptr(memory.WORLD_TAGS_PTR + uintptr(i))))
	}

	var tags []string
	err := json.Unmarshal(jsonBytes, &tags)
	if err != nil {
		return nil, err
	}

	return tags, nil
}

func GetBlockProperties(side sides.Side) (map[string]string, error) {
	resultPtr := getBlockPropertiesRaw(int32(side))
	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	if err := errors.NewError(int(errorCode)); err != nil {
		return nil, err
	}

	length := *(*int32)(unsafe.Pointer(uintptr(resultPtr + 4)))
	if length == 0 {
		return map[string]string{}, nil
	}

	jsonBytes := make([]byte, length)
	for i := 0; i < int(length); i++ {
		jsonBytes[i] = *(*byte)(unsafe.Pointer(uintptr(memory.WORLD_PROPERTIES_PTR + uintptr(i))))
	}

	var properties map[string]string
	err := json.Unmarshal(jsonBytes, &properties)
	if err != nil {
		return nil, err
	}

	return properties, nil
}

func CanBreak(side sides.Side) (bool, error) {
	resultPtr := canBreakRaw(int32(side))
	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	if err := errors.NewError(int(errorCode)); err != nil {
		return false, err
	}

	canBreak := *(*int32)(unsafe.Pointer(uintptr(resultPtr + 4)))
	return canBreak != 0, nil
}

func BreakBlock(side sides.Side) error {
	resultPtr := breakBlockRaw(int32(side))
	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	if err := errors.NewError(int(errorCode)); err != nil {
		return err
	}
	return nil
}

func PlaceBlock(side sides.Side) error {
	resultPtr := placeBlockRaw(int32(side))
	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	if err := errors.NewError(int(errorCode)); err != nil {
		return err
	}
	return nil
}
