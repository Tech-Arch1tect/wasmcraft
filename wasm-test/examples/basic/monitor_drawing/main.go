package main

import (
	"fmt"
	"math/rand"

	"github.com/wasmcraft/bindings/monitor"
	"github.com/wasmcraft/bindings/peripheral"
)

func main() {
	monitorID := "monitor_test"

	_, err := peripheral.Connect(monitorID)
	if err != nil {
		panic(err)
	}
	width, height, err := monitor.GetSize(monitorID)
	if err != nil {
		panic(err)
	}

	err = monitor.Clear(monitorID, 20, 20, 20)
	if err != nil {
		panic(err)
	}

	fmt.Println("=== Monitor Drawing Primitives ===\n")

	fmt.Println("1. SetPixel / GetPixel")
	for i := 0; i < 50; i++ {
		x := rand.Intn(width/4) + 10
		y := rand.Intn(height/4) + 10
		r := rand.Intn(256)
		g := rand.Intn(256)
		b := rand.Intn(256)
		err = monitor.SetPixel(monitorID, x, y, r, g, b)
		if err != nil {
			panic(err)
		}
	}
	testR, testG, testB, err := monitor.GetPixel(monitorID, 15, 15)
	if err != nil {
		panic(err)
	}
	fmt.Printf("   Pixel at (15,15): RGB(%d, %d, %d)\n", testR, testG, testB)

	fmt.Println("\n2. FillRect")
	err = monitor.FillRect(monitorID, width/4+10, 10, 80, 40, 100, 0, 0)
	if err != nil {
		panic(err)
	}

	fmt.Println("\n3. DrawRect (outline)")
	err = monitor.DrawRect(monitorID, width/4+15, 15, 70, 30, 255, 0, 0)
	if err != nil {
		panic(err)
	}

	fmt.Println("\n4. DrawHLine / DrawVLine")
	err = monitor.DrawHLine(monitorID, 10, height/2, width/2-20, 0, 255, 0)
	if err != nil {
		panic(err)
	}
	err = monitor.DrawVLine(monitorID, width/2, 10, height/2, 0, 0, 255)
	if err != nil {
		panic(err)
	}

	fmt.Println("\n5. DrawChar")
	err = monitor.DrawChar(monitorID, 10, height/2+20, 'A', 255, 255, 0, 0, 0, 0, 3)
	if err != nil {
		panic(err)
	}
	err = monitor.DrawChar(monitorID, 34, height/2+20, 'B', 0, 255, 255, 0, 0, 0, 3)
	if err != nil {
		panic(err)
	}
	err = monitor.DrawChar(monitorID, 58, height/2+20, 'C', 255, 0, 255, 0, 0, 0, 3)
	if err != nil {
		panic(err)
	}

	fmt.Println("\n6. DrawText / MeasureText")
	text := "Hello!"
	textW, textH, err := monitor.MeasureText(monitorID, text, 2)
	if err != nil {
		panic(err)
	}
	fmt.Printf("   Text '%s' size: %dx%d\n", text, textW, textH)
	_, err = monitor.DrawText(monitorID, width/2+10, height/2+20, text, 255, 255, 255, 50, 50, 50, 2)
	if err != nil {
		panic(err)
	}

	fmt.Println("\n7. CopyRegion")
	err = monitor.FillRect(monitorID, width/2+10, height-60, 50, 40, 200, 100, 0)
	if err != nil {
		panic(err)
	}
	_, err = monitor.DrawText(monitorID, width/2+15, height-50, "Copy", 255, 255, 255, 0, 0, 0, 1)
	if err != nil {
		panic(err)
	}
	err = monitor.CopyRegion(monitorID, width/2+10, height-60, 50, 40, width/2+70, height-60)
	if err != nil {
		panic(err)
	}

	fmt.Println("\nAll drawing primitives demonstrated!")
}
