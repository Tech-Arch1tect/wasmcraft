package tui

import (
	"github.com/wasmcraft/bindings/monitor"
)

type Panel struct {
	Child          Component
	Title          string
	TitleBarColor  Color
	TitleTextColor Color
	Background     Color
	BorderColor    Color
	BorderStyle    BorderStyle
	Padding        int
}

func NewPanel(child Component) *Panel {
	return &Panel{
		Child:          child,
		TitleBarColor:  DarkGray,
		TitleTextColor: White,
		Background:     Black,
		BorderColor:    White,
		BorderStyle:    BorderPixel,
		Padding:        1,
	}
}

func (p *Panel) SetTitle(title string) {
	p.Title = title
}

func (p *Panel) SetTitleBarColor(color Color) {
	p.TitleBarColor = color
}

func (p *Panel) SetTitleTextColor(color Color) {
	p.TitleTextColor = color
}

func (p *Panel) SetBackground(color Color) {
	p.Background = color
}

func (p *Panel) SetBorderColor(color Color) {
	p.BorderColor = color
}

func (p *Panel) SetPadding(padding int) {
	p.Padding = padding
}

func (p *Panel) MinSize(monitorID string) (width, height int, err error) {
	minWidth := 2
	minHeight := 2

	titleBarHeight := 0
	if p.Title != "" {
		titleWidth, titleH, err := monitor.MeasureText(monitorID, p.Title, 1)
		if err != nil {
			return 0, 0, err
		}
		titleBarHeight = titleH + 2
		if titleWidth+4 > minWidth {
			minWidth = titleWidth + 4
		}
	}

	if p.Child != nil {
		childW, childH, err := p.Child.MinSize(monitorID)
		if err != nil {
			return 0, 0, err
		}

		borderSize := 2
		if p.BorderStyle == BorderNone {
			borderSize = 0
		}

		totalPadding := p.Padding * 2
		contentWidth := childW + borderSize + totalPadding
		contentHeight := childH + borderSize + totalPadding + titleBarHeight

		if contentWidth > minWidth {
			minWidth = contentWidth
		}
		minHeight = contentHeight
	}

	return minWidth, minHeight, nil
}

func (p *Panel) Render(monitorID string, region Rect) error {
	if err := monitor.FillRect(
		monitorID,
		region.X, region.Y,
		region.Width, region.Height,
		p.Background.R, p.Background.G, p.Background.B,
	); err != nil {
		return err
	}

	if p.BorderStyle != BorderNone {
		if err := monitor.DrawRect(
			monitorID,
			region.X, region.Y,
			region.Width, region.Height,
			p.BorderColor.R, p.BorderColor.G, p.BorderColor.B,
		); err != nil {
			return err
		}
	}

	titleBarHeight := 0
	if p.Title != "" {
		_, titleH, err := monitor.MeasureText(monitorID, p.Title, 1)
		if err != nil {
			return err
		}
		titleBarHeight = titleH + 2

		if err := monitor.FillRect(
			monitorID,
			region.X+1, region.Y+1,
			region.Width-2, titleBarHeight,
			p.TitleBarColor.R, p.TitleBarColor.G, p.TitleBarColor.B,
		); err != nil {
			return err
		}

		titleWidth, _, err := monitor.MeasureText(monitorID, p.Title, 1)
		if err != nil {
			return err
		}
		titleX := region.X + 1 + (region.Width-2-titleWidth)/2
		titleY := region.Y + 2

		_, err = monitor.DrawText(
			monitorID,
			titleX, titleY,
			p.Title,
			p.TitleTextColor.R, p.TitleTextColor.G, p.TitleTextColor.B,
			p.TitleBarColor.R, p.TitleBarColor.G, p.TitleBarColor.B,
			1,
		)
		if err != nil {
			return err
		}
	}

	if p.Child != nil {
		borderSize := 1
		if p.BorderStyle == BorderNone {
			borderSize = 0
		}

		childRegion := Rect{
			X:      region.X + borderSize + p.Padding,
			Y:      region.Y + borderSize + p.Padding + titleBarHeight,
			Width:  region.Width - (borderSize+p.Padding)*2,
			Height: region.Height - (borderSize+p.Padding)*2 - titleBarHeight,
		}

		if childRegion.Width > 0 && childRegion.Height > 0 {
			if err := p.Child.Render(monitorID, childRegion); err != nil {
				return err
			}
		}
	}

	return nil
}
