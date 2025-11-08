package main

import (
	"github.com/wasmcraft/bindings/monitor"
	"github.com/wasmcraft/bindings/peripheral"
)

func main() {
	peripheral.Connect("monitor_test")

	// Clear screen to black
	monitor.Clear("monitor_test", 0, 0, 0)

	// Draw 10-pixel borders around the entire display
	// This helps identify the edges of the full 128x128 buffer

	// TOP border - RED (10 pixels tall across full width)
	for y := 0; y < 10; y++ {
		for x := 0; x < 128; x++ {
			monitor.SetPixel("monitor_test", x, y, 255, 0, 0)
		}
	}

	// BOTTOM border - GREEN (10 pixels tall across full width)
	for y := 118; y < 128; y++ {
		for x := 0; x < 128; x++ {
			monitor.SetPixel("monitor_test", x, y, 0, 255, 0)
		}
	}

	// LEFT border - BLUE (10 pixels wide across full height, excluding corners already colored)
	for y := 10; y < 118; y++ {
		for x := 0; x < 10; x++ {
			monitor.SetPixel("monitor_test", x, y, 0, 0, 255)
		}
	}

	// RIGHT border - YELLOW (10 pixels wide across full height, excluding corners already colored)
	for y := 10; y < 118; y++ {
		for x := 118; x < 128; x++ {
			monitor.SetPixel("monitor_test", x, y, 255, 255, 0)
		}
	}

	// Draw markers at exact corners (single pixel, white)
	monitor.SetPixel("monitor_test", 0, 0, 255, 255, 255)       // top-left
	monitor.SetPixel("monitor_test", 127, 0, 255, 255, 255)     // top-right
	monitor.SetPixel("monitor_test", 0, 127, 255, 255, 255)     // bottom-left
	monitor.SetPixel("monitor_test", 127, 127, 255, 255, 255)   // bottom-right

	// Draw 64x64 divider lines (where monitors meet in 2x2 grid)
	// Vertical line at x=64 - CYAN
	for y := 0; y < 128; y++ {
		monitor.SetPixel("monitor_test", 64, y, 0, 255, 255)
	}

	// Horizontal line at y=64 - MAGENTA
	for x := 0; x < 128; x++ {
		monitor.SetPixel("monitor_test", x, 64, 255, 0, 255)
	}
}
