package world

import (
	"encoding/json"
	"unsafe"

	"github.com/wasmcraft/bindings/errors"
	"github.com/wasmcraft/bindings/memory"
)

const (
	BOTTOM = 0
	TOP    = 1
	FRONT  = 2
	BACK   = 3
	LEFT   = 4
	RIGHT  = 5
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
		blockIdBytes[i] = *(*byte)(unsafe.Pointer(uintptr(memory.WORLD_BLOCK_ID_PTR + uintptr(i))))
	}

	return string(blockIdBytes)
}

func GetBlockProperty(side int, propertyName string) string {
	propertyNameBytes := []byte(propertyName)
	propertyNamePtr := uintptr(unsafe.Pointer(&propertyNameBytes[0]))

	resultPtr := getBlockPropertyRaw(int32(side), int32(propertyNamePtr), int32(len(propertyNameBytes)))
	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	errors.Check(int(errorCode))

	length := *(*int32)(unsafe.Pointer(uintptr(resultPtr + 4)))
	if length == 0 {
		return ""
	}

	valueBytes := make([]byte, length)
	for i := 0; i < int(length); i++ {
		valueBytes[i] = *(*byte)(unsafe.Pointer(uintptr(memory.WORLD_PROPERTY_VALUE_PTR + uintptr(i))))
	}

	return string(valueBytes)
}

func HasBlockTag(side int, tagName string) bool {
	tagNameBytes := []byte(tagName)
	tagNamePtr := uintptr(unsafe.Pointer(&tagNameBytes[0]))

	resultPtr := hasBlockTagRaw(int32(side), int32(tagNamePtr), int32(len(tagNameBytes)))
	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	errors.Check(int(errorCode))

	hasTag := *(*int32)(unsafe.Pointer(uintptr(resultPtr + 4)))
	return hasTag != 0
}

func GetBlockTags(side int) []string {
	resultPtr := getBlockTagsRaw(int32(side))
	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	errors.Check(int(errorCode))

	length := *(*int32)(unsafe.Pointer(uintptr(resultPtr + 4)))
	if length == 0 {
		return []string{}
	}

	jsonBytes := make([]byte, length)
	for i := 0; i < int(length); i++ {
		jsonBytes[i] = *(*byte)(unsafe.Pointer(uintptr(memory.WORLD_TAGS_PTR + uintptr(i))))
	}

	var tags []string
	err := json.Unmarshal(jsonBytes, &tags)
	if err != nil {
		return []string{}
	}

	return tags
}

func GetBlockProperties(side int) map[string]string {
	resultPtr := getBlockPropertiesRaw(int32(side))
	errorCode := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	errors.Check(int(errorCode))

	length := *(*int32)(unsafe.Pointer(uintptr(resultPtr + 4)))
	if length == 0 {
		return map[string]string{}
	}

	jsonBytes := make([]byte, length)
	for i := 0; i < int(length); i++ {
		jsonBytes[i] = *(*byte)(unsafe.Pointer(uintptr(memory.WORLD_PROPERTIES_PTR + uintptr(i))))
	}

	var properties map[string]string
	err := json.Unmarshal(jsonBytes, &properties)
	if err != nil {
		return map[string]string{}
	}

	return properties
}
