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

	fmt.Println("=== TUI Layouts Example ===\n")

	panel1 := tui.NewPanel(tui.NewText("Left\n1/3 width"))
	panel1.SetTitle("Panel 1")
	panel1.SetTitleBarColor(tui.Color{R: 128, G: 0, B: 0})
	panel1.SetTitleTextColor(tui.White)
	panel1.SetBorderColor(tui.Red)
	panel1.SetPadding(2)

	panel2 := tui.NewPanel(tui.NewText("Center\n1/3 width"))
	panel2.SetTitle("Panel 2")
	panel2.SetTitleBarColor(tui.Color{R: 0, G: 128, B: 0})
	panel2.SetTitleTextColor(tui.White)
	panel2.SetBorderColor(tui.Green)
	panel2.SetPadding(2)

	panel3 := tui.NewPanel(tui.NewText("Right\n1/3 width"))
	panel3.SetTitle("Panel 3")
	panel3.SetTitleBarColor(tui.Color{R: 0, G: 0, B: 128})
	panel3.SetTitleTextColor(tui.White)
	panel3.SetBorderColor(tui.Blue)
	panel3.SetPadding(2)

	topRow := tui.NewHBox()
	topRow.SetSpacing(3)
	topRow.AddChild(tui.NewFlexChild(panel1, 1))
	topRow.AddChild(tui.NewFlexChild(panel2, 1))
	topRow.AddChild(tui.NewFlexChild(panel3, 1))

	bottomPanel := tui.NewPanel(tui.NewText("Full width, 1/2 height"))
	bottomPanel.SetTitle("Bottom")
	bottomPanel.SetTitleBarColor(tui.Color{R: 64, G: 64, B: 64})
	bottomPanel.SetTitleTextColor(tui.White)
	bottomPanel.SetBorderColor(tui.Gray)
	bottomPanel.SetPadding(2)

	separator := tui.NewHSeparator()
	separator.SetColor(tui.Cyan)

	mainLayout := tui.NewVBox()
	mainLayout.SetSpacing(3)
	mainLayout.AddChild(tui.NewFlexChild(topRow, 1))
	mainLayout.AddChild(separator)
	mainLayout.AddChild(tui.NewFlexChild(bottomPanel, 1))

	mainLayout.Render(monitorID, tui.Rect{
		X:      0,
		Y:      0,
		Width:  width,
		Height: height,
	})

	fmt.Println("Layout features demonstrated:")
	fmt.Println("- HBox: horizontal layout (top row, 3 panels)")
	fmt.Println("- VBox: vertical layout (main structure)")
	fmt.Println("- FlexChild: proportional sizing (1/3, 1/3, 1/3)")
	fmt.Println("- Separator: visual divider between sections")
}
