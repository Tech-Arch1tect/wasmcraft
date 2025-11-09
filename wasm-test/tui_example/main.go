package main

import (
	"fmt"

	"github.com/wasmcraft/bindings/monitor"
	"github.com/wasmcraft/bindings/peripheral"
	"github.com/wasmcraft/tui"
)

func main() {
	monitorID := "monitor_test"

	result := peripheral.Connect(monitorID)
	fmt.Printf("Connected: %s\n", result)

	monitor.SetResolution(monitorID, 128)
	width, height := monitor.GetSize(monitorID)
	fmt.Printf("Monitor size: %dx%d\n", width, height)

	monitor.Clear(monitorID, 16, 16, 16)

	fmt.Println("\nTUI Framework Demo")
	fmt.Println("==================")

	fmt.Println("\n1. Creating text components...")

	text1 := tui.NewText("Hello TUI!")
	text1.SetAlign(tui.AlignCenter)
	text1.Render(monitorID, tui.Rect{X: 5, Y: 3, Width: 118, Height: 10})

	styledText := tui.NewTextStyled("Styled", tui.Style{
		Foreground: tui.Yellow,
		Background: tui.DarkGray,
		Scale:      1,
	})
	styledText.SetAlign(tui.AlignCenter)
	styledText.Render(monitorID, tui.Rect{X: 5, Y: 15, Width: 118, Height: 10})

	fmt.Println("2. Creating boxes...")

	innerText := tui.NewTextStyled("Welcome!", tui.Style{
		Foreground: tui.White,
		Background: tui.Black,
		Scale:      1,
	})
	innerText.SetAlign(tui.AlignCenter)

	box1 := tui.NewBox(innerText)
	box1.SetTitle("Info")
	box1.SetBackground(tui.Color{R: 0, G: 32, B: 64})
	box1.SetBorderColor(tui.Cyan)
	box1.SetPadding(2)

	box1Width, box1Height := box1.MinSize(monitorID)
	box1.Render(monitorID, tui.Rect{X: 4, Y: 28, Width: box1Width + 4, Height: box1Height + 4})

	warningText := tui.NewTextStyled("Alert!", tui.Style{
		Foreground: tui.Black,
		Background: tui.Color{R: 64, G: 64, B: 0},
		Scale:      1,
	})
	warningText.SetAlign(tui.AlignCenter)

	warningBox := tui.NewBox(warningText)
	warningBox.SetTitle("Warning")
	warningBox.SetBackground(tui.Color{R: 64, G: 64, B: 0})
	warningBox.SetBorderColor(tui.Yellow)
	warningBox.SetPadding(2)

	warningWidth, warningHeight := warningBox.MinSize(monitorID)
	warningBox.Render(monitorID, tui.Rect{X: 4 + box1Width + 8, Y: 28, Width: warningWidth + 4, Height: warningHeight + 4})

	fmt.Println("3. Creating themed boxes...")

	successText := tui.NewTextStyled("OK", tui.Style{
		Foreground: tui.White,
		Background: tui.Color{R: 0, G: 64, B: 0},
		Scale:      1,
	})
	successText.SetAlign(tui.AlignCenter)

	successBox := tui.NewBox(successText)
	successBox.SetTitle("Success")
	successBox.SetBackground(tui.Color{R: 0, G: 64, B: 0})
	successBox.SetBorderColor(tui.Green)
	successBox.SetPadding(2)

	successWidth, successHeight := successBox.MinSize(monitorID)
	successBox.Render(monitorID, tui.Rect{X: 4, Y: 54, Width: successWidth + 4, Height: successHeight + 4})

	errorText := tui.NewTextStyled("Error!", tui.Style{
		Foreground: tui.White,
		Background: tui.Color{R: 64, G: 0, B: 0},
		Scale:      1,
	})
	errorText.SetAlign(tui.AlignCenter)

	errorBox := tui.NewBox(errorText)
	errorBox.SetTitle("Failed")
	errorBox.SetBackground(tui.Color{R: 64, G: 0, B: 0})
	errorBox.SetBorderColor(tui.Red)
	errorBox.SetPadding(2)

	errorWidth, errorHeight := errorBox.MinSize(monitorID)
	errorBox.Render(monitorID, tui.Rect{X: 4 + successWidth + 8, Y: 54, Width: errorWidth + 4, Height: errorHeight + 4})

	statusText := tui.NewTextStyled("Ready", tui.Style{
		Foreground: tui.White,
		Background: tui.Color{R: 32, G: 32, B: 32},
		Scale:      1,
	})
	statusText.SetAlign(tui.AlignCenter)

	statusBox := tui.NewBox(statusText)
	statusBox.SetTitle("Status")
	statusBox.SetBackground(tui.Color{R: 32, G: 32, B: 32})
	statusBox.SetBorderColor(tui.LightGray)
	statusBox.SetPadding(2)

	statusWidth, statusHeight := statusBox.MinSize(monitorID)
	statusBox.Render(monitorID, tui.Rect{X: 4 + successWidth + errorWidth + 16, Y: 54, Width: statusWidth + 4, Height: statusHeight + 4})

	fmt.Println("\nDemo complete!")
}
