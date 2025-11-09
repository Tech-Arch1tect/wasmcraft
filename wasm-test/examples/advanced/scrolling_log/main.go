package main

import (
	"fmt"
	"time"

	"github.com/wasmcraft/bindings/monitor"
	"github.com/wasmcraft/bindings/peripheral"
)

func main() {
	monitorID := "monitor_test"

	peripheral.Connect(monitorID)
	width, height := monitor.GetSize(monitorID)

	monitor.Clear(monitorID, 0, 0, 0)

	_, lineHeight := monitor.MeasureText(monitorID, "A", 1)
	maxLines := (height - 20) / lineHeight
	logY := 10

	fmt.Println("=== Scrolling Log Example ===\n")
	fmt.Println("Demonstrates CopyRegion for smooth scrolling")
	fmt.Println("Watch the monitor for animated log updates\n")

	messages := []string{
		"[INFO] System starting...",
		"[INFO] Loading configuration",
		"[OK] Database connected",
		"[INFO] Starting web server",
		"[OK] HTTP listening on :8080",
		"[INFO] Processing requests",
		"[WARN] High memory usage: 75%",
		"[INFO] Cache cleared",
		"[OK] Memory optimized",
		"[INFO] Background tasks running",
		"[INFO] Health check passed",
		"[OK] System stable",
		"[INFO] New connection from 192.168.1.100",
		"[WARN] Slow query detected (2.3s)",
		"[INFO] Query optimized",
		"[OK] Performance improved",
		"[INFO] Backup started",
		"[OK] Backup completed",
		"[INFO] All systems operational",
		"[OK] Ready for production",
	}

	currentLine := 0

	for i, msg := range messages {
		if currentLine >= maxLines {
			logAreaHeight := maxLines * lineHeight
			monitor.CopyRegion(monitorID, 10, logY+lineHeight, width-20, logAreaHeight-lineHeight, 10, logY)

			monitor.FillRect(monitorID, 10, logY+(maxLines-1)*lineHeight, width-20, lineHeight, 0, 0, 0)

			currentLine = maxLines - 1
		}

		y := logY + currentLine*lineHeight

		var color [3]int
		if msg[1] == 'O' {
			color = [3]int{0, 255, 0}
		} else if msg[1] == 'W' {
			color = [3]int{255, 255, 0}
		} else {
			color = [3]int{0, 255, 255}
		}

		monitor.DrawText(monitorID, 10, y, msg, color[0], color[1], color[2], 0, 0, 0, 1)

		fmt.Printf("[%2d/%2d] %s\n", i+1, len(messages), msg)
		currentLine++

		time.Sleep(300 * time.Millisecond)
	}

	fmt.Println("\nScrolling complete!")
	fmt.Println("CopyRegion enables smooth log scrolling without redrawing everything.")
}
