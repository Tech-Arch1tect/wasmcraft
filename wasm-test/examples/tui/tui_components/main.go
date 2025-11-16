package main

import (
	"fmt"

	"github.com/wasmcraft/bindings/monitor"
	"github.com/wasmcraft/bindings/peripheral"
	"github.com/wasmcraft/tui"
)

func main() {
	monitorID := "monitor_test"

	if _, err := peripheral.Connect(monitorID); err != nil {
		panic(err)
	}
	width, height, err := monitor.GetSize(monitorID)
	if err != nil {
		panic(err)
	}

	if err := monitor.Clear(monitorID, 16, 16, 16); err != nil {
		panic(err)
	}

	fmt.Println("=== TUI Components Example ===\n")

	taskList := tui.NewList()
	taskList.SetStyle(tui.ListBullet)
	taskList.SetBulletColor(tui.Yellow)
	taskList.AddItem("Initialize system", tui.White)
	taskList.AddItem("Load configuration", tui.White)
	taskList.AddItem("Start services", tui.White)

	listPanel := tui.NewPanel(taskList)
	listPanel.SetTitle("Task List")
	listPanel.SetTitleBarColor(tui.Color{R: 64, G: 128, B: 0})
	listPanel.SetTitleTextColor(tui.White)
	listPanel.SetBorderColor(tui.Green)
	listPanel.SetPadding(2)

	systemLog := tui.NewLog()
	systemLog.Info("System started")
	systemLog.Success("Config loaded")
	systemLog.Warning("Cache miss")
	systemLog.Info("Services ready")

	logPanel := tui.NewPanel(systemLog)
	logPanel.SetTitle("System Log")
	logPanel.SetTitleBarColor(tui.Color{R: 32, G: 32, B: 32})
	logPanel.SetTitleTextColor(tui.White)
	logPanel.SetBorderColor(tui.Gray)
	logPanel.SetPadding(2)

	healthBar := tui.NewProgressBar()
	healthBar.SetValue(0.75)
	healthBar.SetForeground(tui.Green)
	healthBar.SetBackground(tui.DarkGray)
	healthBar.SetShowText(true)

	healthPanel := tui.NewPanel(healthBar)
	healthPanel.SetTitle("Health")
	healthPanel.SetTitleBarColor(tui.Color{R: 0, G: 64, B: 0})
	healthPanel.SetTitleTextColor(tui.White)
	healthPanel.SetBorderColor(tui.Green)
	healthPanel.SetPadding(2)

	layout := tui.NewVBox()
	layout.SetSpacing(4)
	layout.AddChild(listPanel)
	layout.AddChild(logPanel)
	layout.AddChild(healthPanel)

	if err := layout.Render(monitorID, tui.Rect{
		X:      10,
		Y:      10,
		Width:  width - 20,
		Height: height - 20,
	}); err != nil {
		panic(err)
	}

	fmt.Println("Components demonstrated:")
	fmt.Println("- List: bullet style with colored items")
	fmt.Println("- Log: color-coded severity levels")
	fmt.Println("- ProgressBar: visual indicator with percentage")
}
