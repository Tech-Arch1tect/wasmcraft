package main

import (
	"fmt"

	"github.com/wasmcraft/bindings/monitor"
	"github.com/wasmcraft/bindings/peripheral"
)

func main() {
	fmt.Println("=== Peripheral Discovery ===\n")

	fmt.Println("Listing all peripherals:")
	peripherals, err := peripheral.List()
	if err != nil {
		panic(err)
	}
	fmt.Println(peripherals)

	monitorLabel := "monitor_test"
	fmt.Printf("\nConnecting to '%s'...\n", monitorLabel)
	monitorID, err := peripheral.Connect(monitorLabel)
	if err != nil {
		panic(err)
	}
	fmt.Printf("Connected! Monitor ID: %s\n", monitorID)

	width, height, err := monitor.GetSize(monitorID)
	if err != nil {
		panic(err)
	}
	fmt.Printf("Monitor dimensions: %dx%d\n", width, height)

	err = monitor.Clear(monitorID, 0, 50, 0)
	if err != nil {
		panic(err)
	}
	_, err = monitor.DrawText(monitorID, 10, 10, "Connected!", 255, 255, 255, 0, 0, 0, 2)
	if err != nil {
		panic(err)
	}

	fmt.Println("\nPeripheral operations:")
	fmt.Println("1. peripheral.List() - Lists available peripherals")
	fmt.Println("2. peripheral.Connect(label) - Connects to peripheral by label")
	fmt.Println("3. peripheral.Disconnect(id) - Disconnects from peripheral")

	fmt.Printf("\nDisconnecting from monitor '%s'...\n", monitorID)
	err = peripheral.Disconnect(monitorID)
	if err != nil {
		panic(err)
	}
	fmt.Println("Disconnected successfully!")
}
