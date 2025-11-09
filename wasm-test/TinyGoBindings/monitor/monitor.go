package monitor

import "unsafe"

//go:wasmimport env monitor_set_pixel
func monitorSetPixel(idPtr, idLen, x, y, r, g, b uint32)

//go:wasmimport env monitor_get_pixel
func monitorGetPixel(idPtr, idLen, x, y uint32) uint32

//go:wasmimport env monitor_clear
func monitorClear(idPtr, idLen, r, g, b uint32)

//go:wasmimport env monitor_get_size
func monitorGetSize(idPtr, idLen uint32) uint32

//go:wasmimport env monitor_set_resolution
func monitorSetResolution(idPtr, idLen, width, height uint32)

//go:wasmimport env monitor_fill_rect
func monitorFillRect(idPtr, idLen, x, y, width, height, r, g, b uint32)

//go:wasmimport env monitor_draw_hline
func monitorDrawHLine(idPtr, idLen, x, y, length, r, g, b uint32)

//go:wasmimport env monitor_draw_vline
func monitorDrawVLine(idPtr, idLen, x, y, length, r, g, b uint32)

//go:wasmimport env monitor_draw_rect
func monitorDrawRect(idPtr, idLen, x, y, width, height, r, g, b uint32)

//go:wasmimport env monitor_draw_char
func monitorDrawChar(idPtr, idLen, x, y, c, fgR, fgG, fgB, bgR, bgG, bgB, scale uint32)

//go:wasmimport env monitor_draw_text
func monitorDrawText(idPtr, idLen, x, y, textPtr, textLen, fgR, fgG, fgB, bgR, bgG, bgB, scale uint32) uint32

//go:wasmimport env monitor_measure_text
func monitorMeasureText(idPtr, idLen, textPtr, textLen, scale uint32) uint32

//go:wasmimport env monitor_copy_region
func monitorCopyRegion(idPtr, idLen, srcX, srcY, width, height, dstX, dstY uint32)

func SetPixel(monitorID string, x, y, r, g, b int) {
	idBytes := []byte(monitorID)
	idPtr := uint32(uintptr(unsafe.Pointer(&idBytes[0])))
	idLen := uint32(len(idBytes))

	monitorSetPixel(idPtr, idLen, uint32(x), uint32(y), uint32(r), uint32(g), uint32(b))
}

func GetPixel(monitorID string, x, y int) (r, g, b int) {
	idBytes := []byte(monitorID)
	idPtr := uint32(uintptr(unsafe.Pointer(&idBytes[0])))
	idLen := uint32(len(idBytes))

	resultPtr := monitorGetPixel(idPtr, idLen, uint32(x), uint32(y))

	rVal := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	gVal := *(*int32)(unsafe.Pointer(uintptr(resultPtr + 4)))
	bVal := *(*int32)(unsafe.Pointer(uintptr(resultPtr + 8)))

	return int(rVal), int(gVal), int(bVal)
}

func Clear(monitorID string, r, g, b int) {
	idBytes := []byte(monitorID)
	idPtr := uint32(uintptr(unsafe.Pointer(&idBytes[0])))
	idLen := uint32(len(idBytes))

	monitorClear(idPtr, idLen, uint32(r), uint32(g), uint32(b))
}

func GetSize(monitorID string) (width, height int) {
	idBytes := []byte(monitorID)
	idPtr := uint32(uintptr(unsafe.Pointer(&idBytes[0])))
	idLen := uint32(len(idBytes))

	resultPtr := monitorGetSize(idPtr, idLen)

	widthVal := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	heightVal := *(*int32)(unsafe.Pointer(uintptr(resultPtr + 4)))

	return int(widthVal), int(heightVal)
}

func SetResolution(monitorID string, resolution int) {
	idBytes := []byte(monitorID)
	idPtr := uint32(uintptr(unsafe.Pointer(&idBytes[0])))
	idLen := uint32(len(idBytes))

	monitorSetResolution(idPtr, idLen, uint32(resolution), uint32(resolution))
}

func FillRect(monitorID string, x, y, width, height, r, g, b int) {
	idBytes := []byte(monitorID)
	idPtr := uint32(uintptr(unsafe.Pointer(&idBytes[0])))
	idLen := uint32(len(idBytes))

	monitorFillRect(idPtr, idLen, uint32(x), uint32(y), uint32(width), uint32(height), uint32(r), uint32(g), uint32(b))
}

func DrawHLine(monitorID string, x, y, length, r, g, b int) {
	idBytes := []byte(monitorID)
	idPtr := uint32(uintptr(unsafe.Pointer(&idBytes[0])))
	idLen := uint32(len(idBytes))

	monitorDrawHLine(idPtr, idLen, uint32(x), uint32(y), uint32(length), uint32(r), uint32(g), uint32(b))
}

func DrawVLine(monitorID string, x, y, length, r, g, b int) {
	idBytes := []byte(monitorID)
	idPtr := uint32(uintptr(unsafe.Pointer(&idBytes[0])))
	idLen := uint32(len(idBytes))

	monitorDrawVLine(idPtr, idLen, uint32(x), uint32(y), uint32(length), uint32(r), uint32(g), uint32(b))
}

func DrawRect(monitorID string, x, y, width, height, r, g, b int) {
	idBytes := []byte(monitorID)
	idPtr := uint32(uintptr(unsafe.Pointer(&idBytes[0])))
	idLen := uint32(len(idBytes))

	monitorDrawRect(idPtr, idLen, uint32(x), uint32(y), uint32(width), uint32(height), uint32(r), uint32(g), uint32(b))
}

func DrawChar(monitorID string, x, y int, c rune, fgR, fgG, fgB, bgR, bgG, bgB, scale int) {
	idBytes := []byte(monitorID)
	idPtr := uint32(uintptr(unsafe.Pointer(&idBytes[0])))
	idLen := uint32(len(idBytes))

	monitorDrawChar(idPtr, idLen, uint32(x), uint32(y), uint32(c), uint32(fgR), uint32(fgG), uint32(fgB), uint32(bgR), uint32(bgG), uint32(bgB), uint32(scale))
}

func DrawText(monitorID string, x, y int, text string, fgR, fgG, fgB, bgR, bgG, bgB, scale int) int {
	idBytes := []byte(monitorID)
	idPtr := uint32(uintptr(unsafe.Pointer(&idBytes[0])))
	idLen := uint32(len(idBytes))

	textBytes := []byte(text)
	textPtr := uint32(uintptr(unsafe.Pointer(&textBytes[0])))
	textLen := uint32(len(textBytes))

	width := monitorDrawText(idPtr, idLen, uint32(x), uint32(y), textPtr, textLen, uint32(fgR), uint32(fgG), uint32(fgB), uint32(bgR), uint32(bgG), uint32(bgB), uint32(scale))
	return int(width)
}

func MeasureText(monitorID string, text string, scale int) (width, height int) {
	idBytes := []byte(monitorID)
	idPtr := uint32(uintptr(unsafe.Pointer(&idBytes[0])))
	idLen := uint32(len(idBytes))

	textBytes := []byte(text)
	textPtr := uint32(uintptr(unsafe.Pointer(&textBytes[0])))
	textLen := uint32(len(textBytes))

	resultPtr := monitorMeasureText(idPtr, idLen, textPtr, textLen, uint32(scale))

	widthVal := *(*int32)(unsafe.Pointer(uintptr(resultPtr)))
	heightVal := *(*int32)(unsafe.Pointer(uintptr(resultPtr + 4)))

	return int(widthVal), int(heightVal)
}

func CopyRegion(monitorID string, srcX, srcY, width, height, dstX, dstY int) {
	idBytes := []byte(monitorID)
	idPtr := uint32(uintptr(unsafe.Pointer(&idBytes[0])))
	idLen := uint32(len(idBytes))

	monitorCopyRegion(idPtr, idLen, uint32(srcX), uint32(srcY), uint32(width), uint32(height), uint32(dstX), uint32(dstY))
}

type Color struct {
	R, G, B int
}

func RGB(r, g, b int) Color {
	return Color{R: r, G: g, B: b}
}

var (
	Black   = RGB(0, 0, 0)
	White   = RGB(255, 255, 255)
	Red     = RGB(255, 0, 0)
	Green   = RGB(0, 255, 0)
	Blue    = RGB(0, 0, 255)
	Yellow  = RGB(255, 255, 0)
	Cyan    = RGB(0, 255, 255)
	Magenta = RGB(255, 0, 255)
)
