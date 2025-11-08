package main

import (
	"github.com/wasmcraft/bindings/monitor"
	"github.com/wasmcraft/bindings/peripheral"
)

func main() {
	peripheral.Connect("monitor_test")
	
	// Draw a big 32x32 red square in the center
	for y := 16; y < 48; y++ {
		for x := 16; x < 48; x++ {
			monitor.SetPixel("monitor_test", x, y, 255, 0, 0)
		}
	}
}
