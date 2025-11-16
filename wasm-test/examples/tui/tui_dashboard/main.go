package main

import (
	"fmt"

	"github.com/wasmcraft/bindings/monitor"
	"github.com/wasmcraft/bindings/peripheral"
	"github.com/wasmcraft/tui"
)

func main() {
	monitorID := "monitor_test"

	result, err := peripheral.Connect(monitorID)
	if err != nil {
		panic(err)
	}
	fmt.Printf("Connected: %s\n", result)

	if err := monitor.SetResolution(monitorID, 128); err != nil {
		panic(err)
	}
	if err := monitor.Clear(monitorID, 16, 16, 16); err != nil {
		panic(err)
	}

	width, height, err := monitor.GetSize(monitorID)
	if err != nil {
		panic(err)
	}

	fmt.Println("\nDashboard Example")
	fmt.Println("=================")
	fmt.Println("Combining Separator, List, and Log components")
	fmt.Printf("Monitor size: %d x %d\n", width, height)

	title := tui.NewText("System Dashboard")
	title.SetAlign(tui.AlignCenter)
	title.SetStyle(tui.Style{
		Foreground: tui.Cyan,
		Background: tui.Black,
		Scale:      1,
	})

	statusList := tui.NewList()
	statusList.SetStyle(tui.ListBullet)
	statusList.SetBulletColor(tui.Green)
	statusList.AddItem("CPU: 45%", tui.Green)
	statusList.AddItem("Memory: 2GB", tui.Yellow)
	statusList.AddItem("Network: OK", tui.Green)

	statusPanel := tui.NewPanel(statusList)
	statusPanel.SetTitle("System Status")
	statusPanel.SetTitleBarColor(tui.Color{R: 0, G: 64, B: 128})
	statusPanel.SetTitleTextColor(tui.White)
	statusPanel.SetBorderColor(tui.Cyan)
	statusPanel.SetPadding(2)

	taskList := tui.NewList()
	taskList.SetStyle(tui.ListNumbered)
	taskList.SetBulletColor(tui.Yellow)
	taskList.AddItem("Init database", tui.White)
	taskList.AddItem("Start server", tui.White)
	taskList.AddItem("Load plugins", tui.White)

	taskPanel := tui.NewPanel(taskList)
	taskPanel.SetTitle("Startup Tasks")
	taskPanel.SetTitleBarColor(tui.Color{R: 64, G: 128, B: 0})
	taskPanel.SetTitleTextColor(tui.White)
	taskPanel.SetBorderColor(tui.Green)
	taskPanel.SetPadding(2)

	systemLog := tui.NewLog()
	systemLog.SetShowLineNumbers(false)
	systemLog.Info("System boot OK")
	systemLog.Success("Database connected")
	systemLog.Warning("Using safe mode")
	systemLog.Info("Server started")
	systemLog.Success("System ready")

	logPanel := tui.NewPanel(systemLog)
	logPanel.SetTitle("System Log")
	logPanel.SetTitleBarColor(tui.Color{R: 32, G: 32, B: 32})
	logPanel.SetTitleTextColor(tui.White)
	logPanel.SetBorderColor(tui.Gray)
	logPanel.SetPadding(2)

	separator1 := tui.NewHSeparator()
	separator1.SetColor(tui.Cyan)

	separator2 := tui.NewHSeparator()
	separator2.SetColor(tui.Green)

	separator3 := tui.NewHSeparator()
	separator3.SetColor(tui.Gray)

	leftColumn := tui.NewVBox()
	leftColumn.AddChild(statusPanel)
	leftColumn.AddChild(tui.NewVSpacer(2))
	leftColumn.AddChild(separator2)
	leftColumn.AddChild(tui.NewVSpacer(2))
	leftColumn.AddChild(taskPanel)

	rightColumn := tui.NewVBox()
	rightColumn.AddChild(logPanel)

	columns := tui.NewHBox()
	columns.SetSpacing(3)
	columns.AddChild(leftColumn)
	columns.AddChild(tui.NewVSeparator())
	columns.AddChild(rightColumn)

	mainLayout := tui.NewVBox()
	mainLayout.AddChild(title)
	mainLayout.AddChild(tui.NewVSpacer(2))
	mainLayout.AddChild(separator1)
	mainLayout.AddChild(tui.NewVSpacer(2))
	mainLayout.AddChild(columns)
	mainLayout.AddChild(tui.NewVSpacer(2))
	mainLayout.AddChild(separator3)

	if err := mainLayout.Render(monitorID, tui.Rect{
		X:      2,
		Y:      2,
		Width:  width - 4,
		Height: height - 4,
	}); err != nil {
		panic(err)
	}

	fmt.Println("\nDashboard rendered!")
	fmt.Println("Components used:")
	fmt.Println("- Separator (horizontal & vertical)")
	fmt.Println("- List (bullet & numbered styles)")
	fmt.Println("- Log (with line numbers & colors)")
}
