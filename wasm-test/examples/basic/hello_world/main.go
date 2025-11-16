package main

import (
	"fmt"

	"github.com/wasmcraft/bindings/monitor"
	"github.com/wasmcraft/bindings/peripheral"
)

func main() {
	monitorID := "monitor_test"

	result, err := peripheral.Connect(monitorID)
	if err != nil {
		panic(err)
	}
	fmt.Printf("Connected to monitor: %s\n", result)

	width, height, err := monitor.GetSize(monitorID)
	if err != nil {
		panic(err)
	}
	fmt.Printf("Monitor size: %dx%d pixels\n", width, height)

	err = monitor.Clear(monitorID, 0, 0, 0)
	if err != nil {
		panic(err)
	}

	message := "Hello, WasmCraft!"
	textWidth, textHeight, err := monitor.MeasureText(monitorID, message, 2)
	if err != nil {
		panic(err)
	}

	x := (width - textWidth) / 2
	y := (height - textHeight) / 2

	_, err = monitor.DrawText(monitorID, x, y, message, 0, 255, 255, 0, 0, 0, 2)
	if err != nil {
		panic(err)
	}

	fmt.Println("Hello world displayed on monitor!")
}
