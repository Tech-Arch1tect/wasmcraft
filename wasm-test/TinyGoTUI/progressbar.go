package tui

import (
	"github.com/wasmcraft/bindings/monitor"
)

type ProgressBar struct {
	Value      float64
	Background Color
	Foreground Color
	ShowText   bool
}

func NewProgressBar() *ProgressBar {
	return &ProgressBar{
		Value:      0.0,
		Background: DarkGray,
		Foreground: Green,
		ShowText:   true,
	}
}

func (p *ProgressBar) SetValue(value float64) {
	if value < 0.0 {
		value = 0.0
	}
	if value > 1.0 {
		value = 1.0
	}
	p.Value = value
}

func (p *ProgressBar) SetBackground(color Color) {
	p.Background = color
}

func (p *ProgressBar) SetForeground(color Color) {
	p.Foreground = color
}

func (p *ProgressBar) SetShowText(show bool) {
	p.ShowText = show
}

func (p *ProgressBar) MinSize(monitorID string) (width, height int, err error) {
	return 20, 8, nil
}

func (p *ProgressBar) Render(monitorID string, region Rect) error {
	if err := monitor.FillRect(
		monitorID,
		region.X, region.Y,
		region.Width, region.Height,
		p.Background.R, p.Background.G, p.Background.B,
	); err != nil {
		return err
	}

	filledWidth := int(float64(region.Width) * p.Value)
	if filledWidth > 0 {
		if err := monitor.FillRect(
			monitorID,
			region.X, region.Y,
			filledWidth, region.Height,
			p.Foreground.R, p.Foreground.G, p.Foreground.B,
		); err != nil {
			return err
		}
	}

	if p.ShowText {
		percentage := int(p.Value * 100)
		text := ""
		if percentage < 10 {
			text = " " + string(rune('0'+percentage)) + "%"
		} else if percentage == 100 {
			text = "100%"
		} else {
			tens := percentage / 10
			ones := percentage % 10
			text = string(rune('0'+tens)) + string(rune('0'+ones)) + "%"
		}

		textWidth, textHeight, err := monitor.MeasureText(monitorID, text, 1)
		if err != nil {
			return err
		}
		textX := region.X + (region.Width-textWidth)/2
		textY := region.Y + (region.Height-textHeight)/2

		fgBrightness := p.Foreground.R + p.Foreground.G + p.Foreground.B
		bgBrightness := p.Background.R + p.Background.G + p.Background.B

		textColor := Black
		if fgBrightness > bgBrightness {
			textColor = White
		}

		_, err = monitor.DrawText(
			monitorID,
			textX, textY,
			text,
			textColor.R, textColor.G, textColor.B,
			0, 0, 0,
			1,
		)
		if err != nil {
			return err
		}
	}
	return nil
}
