package main

import (
	"fmt"

	"github.com/wasmcraft/bindings/monitor"
	"github.com/wasmcraft/bindings/peripheral"
	"github.com/wasmcraft/tui"
)

func main() {
	monitorID := "monitor_test"

	peripheral.Connect(monitorID)
	width, height := monitor.GetSize(monitorID)

	monitor.Clear(monitorID, 16, 16, 16)

	fmt.Println("=== TUI Basics Example ===\n")

	title := tui.NewText("TUI Framework")
	title.SetAlign(tui.AlignCenter)
	title.SetStyle(tui.Style{
		Foreground: tui.Cyan,
		Background: tui.Black,
		Scale:      2,
	})

	textBox := tui.NewBox(tui.NewText("Box with border and title"))
	textBox.SetTitle("Info")
	textBox.SetBorderColor(tui.Blue)
	textBox.SetPadding(2)

	styledPanel := tui.NewPanel(tui.NewText("Panel with colored title bar"))
	styledPanel.SetTitle("Status")
	styledPanel.SetTitleBarColor(tui.Color{R: 0, G: 128, B: 0})
	styledPanel.SetTitleTextColor(tui.White)
	styledPanel.SetBorderColor(tui.Green)
	styledPanel.SetPadding(2)

	warningBox := tui.NewBox(tui.NewText("Important message"))
	warningBox.SetTitle("Warning")
	warningBox.SetBorderColor(tui.Yellow)
	warningBox.SetBackground(tui.Color{R: 50, G: 50, B: 0})
	warningBox.SetPadding(2)

	layout := tui.NewVBox()
	layout.SetSpacing(4)
	layout.AddChild(title)
	layout.AddChild(textBox)
	layout.AddChild(styledPanel)
	layout.AddChild(warningBox)

	layout.Render(monitorID, tui.Rect{
		X:      10,
		Y:      10,
		Width:  width - 20,
		Height: height - 20,
	})

	fmt.Println("Components demonstrated:")
	fmt.Println("- Text with alignment and styling")
	fmt.Println("- Box with border and title")
	fmt.Println("- Panel with colored title bar")
}
