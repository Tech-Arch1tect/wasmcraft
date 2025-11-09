package main

import (
	"fmt"

	"github.com/wasmcraft/bindings/monitor"
	"github.com/wasmcraft/bindings/peripheral"
)

func main() {
	monitorID := "monitor_test"

	result := peripheral.Connect(monitorID)
	fmt.Printf("Connected to monitor: %s\n", result)

	width, height := monitor.GetSize(monitorID)
	fmt.Printf("Monitor size: %dx%d pixels\n", width, height)

	monitor.Clear(monitorID, 0, 0, 0)

	message := "Hello, WasmCraft!"
	textWidth, textHeight := monitor.MeasureText(monitorID, message, 2)

	x := (width - textWidth) / 2
	y := (height - textHeight) / 2

	monitor.DrawText(monitorID, x, y, message, 0, 255, 255, 0, 0, 0, 2)

	fmt.Println("Hello world displayed on monitor!")
}
