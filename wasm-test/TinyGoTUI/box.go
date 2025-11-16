package tui

import (
	"github.com/wasmcraft/bindings/monitor"
)

type Box struct {
	Child       Component
	Title       string
	TitleStyle  Style
	Background  Color
	BorderColor Color
	BorderStyle BorderStyle
	Padding     int
}

func NewBox(child Component) *Box {
	return &Box{
		Child:       child,
		Background:  Black,
		BorderColor: White,
		BorderStyle: BorderPixel,
		Padding:     1,
		TitleStyle:  DefaultStyle(),
	}
}

func (b *Box) SetTitle(title string) {
	b.Title = title
}

func (b *Box) SetBackground(color Color) {
	b.Background = color
}

func (b *Box) SetBorderColor(color Color) {
	b.BorderColor = color
}

func (b *Box) SetPadding(padding int) {
	b.Padding = padding
}

func (b *Box) MinSize(monitorID string) (width, height int, err error) {
	minWidth := 2
	minHeight := 2

	titleHeight := 0
	if b.Title != "" {
		titleWidth, titleH, err := monitor.MeasureText(monitorID, b.Title, b.TitleStyle.Scale)
		if err != nil {
			return 0, 0, err
		}
		titleHeight = titleH
		minWidth = titleWidth + 4
	}

	if b.Child != nil {
		childW, childH, err := b.Child.MinSize(monitorID)
		if err != nil {
			return 0, 0, err
		}

		borderSize := 2
		if b.BorderStyle == BorderNone {
			borderSize = 0
		}

		totalPadding := b.Padding * 2
		contentWidth := childW + borderSize + totalPadding
		contentHeight := childH + borderSize + totalPadding

		if titleHeight > 0 {
			contentHeight += titleHeight + 1
		}

		if contentWidth > minWidth {
			minWidth = contentWidth
		}
		minHeight = contentHeight
	}

	return minWidth, minHeight, nil
}

func (b *Box) Render(monitorID string, region Rect) error {
	if err := monitor.FillRect(
		monitorID,
		region.X, region.Y,
		region.Width, region.Height,
		b.Background.R, b.Background.G, b.Background.B,
	); err != nil {
		return err
	}

	if b.BorderStyle != BorderNone {
		if err := monitor.DrawRect(
			monitorID,
			region.X, region.Y,
			region.Width, region.Height,
			b.BorderColor.R, b.BorderColor.G, b.BorderColor.B,
		); err != nil {
			return err
		}

		if b.Title != "" {
			titleWidth, titleHeight, err := monitor.MeasureText(monitorID, b.Title, b.TitleStyle.Scale)
			if err != nil {
				return err
			}
			titleX := region.X + (region.Width-titleWidth)/2
			titleY := region.Y + 1

			if err := monitor.FillRect(
				monitorID,
				titleX-1, titleY,
				titleWidth+2, titleHeight,
				b.Background.R, b.Background.G, b.Background.B,
			); err != nil {
				return err
			}

			_, err = monitor.DrawText(
				monitorID,
				titleX, titleY,
				b.Title,
				b.TitleStyle.Foreground.R, b.TitleStyle.Foreground.G, b.TitleStyle.Foreground.B,
				b.Background.R, b.Background.G, b.Background.B,
				b.TitleStyle.Scale,
			)
			if err != nil {
				return err
			}
		}
	}

	if b.Child != nil {
		borderSize := 1
		if b.BorderStyle == BorderNone {
			borderSize = 0
		}

		titleOffset := 0
		if b.Title != "" {
			_, titleHeight, err := monitor.MeasureText(monitorID, b.Title, b.TitleStyle.Scale)
			if err != nil {
				return err
			}
			titleOffset = titleHeight + 1
		}

		childRegion := Rect{
			X:      region.X + borderSize + b.Padding,
			Y:      region.Y + borderSize + b.Padding + titleOffset,
			Width:  region.Width - (borderSize+b.Padding)*2,
			Height: region.Height - (borderSize+b.Padding)*2 - titleOffset,
		}

		if childRegion.Width > 0 && childRegion.Height > 0 {
			if err := b.Child.Render(monitorID, childRegion); err != nil {
				return err
			}
		}
	}

	return nil
}
