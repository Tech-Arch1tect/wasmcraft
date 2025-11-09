package main

import (
	"fmt"
	"math/rand"

	"github.com/wasmcraft/bindings/monitor"
	"github.com/wasmcraft/bindings/peripheral"
)

func main() {
	monitorID := "monitor_test"

	peripheral.Connect(monitorID)
	width, height := monitor.GetSize(monitorID)

	monitor.Clear(monitorID, 20, 20, 20)

	fmt.Println("=== Monitor Drawing Primitives ===\n")

	fmt.Println("1. SetPixel / GetPixel")
	for i := 0; i < 50; i++ {
		x := rand.Intn(width/4) + 10
		y := rand.Intn(height/4) + 10
		r := rand.Intn(256)
		g := rand.Intn(256)
		b := rand.Intn(256)
		monitor.SetPixel(monitorID, x, y, r, g, b)
	}
	testR, testG, testB := monitor.GetPixel(monitorID, 15, 15)
	fmt.Printf("   Pixel at (15,15): RGB(%d, %d, %d)\n", testR, testG, testB)

	fmt.Println("\n2. FillRect")
	monitor.FillRect(monitorID, width/4+10, 10, 80, 40, 100, 0, 0)

	fmt.Println("\n3. DrawRect (outline)")
	monitor.DrawRect(monitorID, width/4+15, 15, 70, 30, 255, 0, 0)

	fmt.Println("\n4. DrawHLine / DrawVLine")
	monitor.DrawHLine(monitorID, 10, height/2, width/2-20, 0, 255, 0)
	monitor.DrawVLine(monitorID, width/2, 10, height/2, 0, 0, 255)

	fmt.Println("\n5. DrawChar")
	monitor.DrawChar(monitorID, 10, height/2+20, 'A', 255, 255, 0, 0, 0, 0, 3)
	monitor.DrawChar(monitorID, 34, height/2+20, 'B', 0, 255, 255, 0, 0, 0, 3)
	monitor.DrawChar(monitorID, 58, height/2+20, 'C', 255, 0, 255, 0, 0, 0, 3)

	fmt.Println("\n6. DrawText / MeasureText")
	text := "Hello!"
	textW, textH := monitor.MeasureText(monitorID, text, 2)
	fmt.Printf("   Text '%s' size: %dx%d\n", text, textW, textH)
	monitor.DrawText(monitorID, width/2+10, height/2+20, text, 255, 255, 255, 50, 50, 50, 2)

	fmt.Println("\n7. CopyRegion")
	monitor.FillRect(monitorID, width/2+10, height-60, 50, 40, 200, 100, 0)
	monitor.DrawText(monitorID, width/2+15, height-50, "Copy", 255, 255, 255, 0, 0, 0, 1)
	monitor.CopyRegion(monitorID, width/2+10, height-60, 50, 40, width/2+70, height-60)

	fmt.Println("\nAll drawing primitives demonstrated!")
}
