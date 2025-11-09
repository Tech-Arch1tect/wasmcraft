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

func (b *Box) MinSize(monitorID string) (width, height int) {
	minWidth := 2
	minHeight := 2

	titleHeight := 0
	if b.Title != "" {
		titleWidth, titleH := monitor.MeasureText(monitorID, b.Title, b.TitleStyle.Scale)
		titleHeight = titleH
		minWidth = titleWidth + 4
	}

	if b.Child != nil {
		childW, childH := b.Child.MinSize(monitorID)

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

	return minWidth, minHeight
}

func (b *Box) Render(monitorID string, region Rect) {
	monitor.FillRect(
		monitorID,
		region.X, region.Y,
		region.Width, region.Height,
		b.Background.R, b.Background.G, b.Background.B,
	)

	if b.BorderStyle != BorderNone {
		monitor.DrawRect(
			monitorID,
			region.X, region.Y,
			region.Width, region.Height,
			b.BorderColor.R, b.BorderColor.G, b.BorderColor.B,
		)

		if b.Title != "" {
			titleWidth, titleHeight := monitor.MeasureText(monitorID, b.Title, b.TitleStyle.Scale)
			titleX := region.X + (region.Width-titleWidth)/2
			titleY := region.Y + 1

			monitor.FillRect(
				monitorID,
				titleX-1, titleY,
				titleWidth+2, titleHeight,
				b.Background.R, b.Background.G, b.Background.B,
			)

			monitor.DrawText(
				monitorID,
				titleX, titleY,
				b.Title,
				b.TitleStyle.Foreground.R, b.TitleStyle.Foreground.G, b.TitleStyle.Foreground.B,
				b.Background.R, b.Background.G, b.Background.B,
				b.TitleStyle.Scale,
			)
		}
	}

	if b.Child != nil {
		borderSize := 1
		if b.BorderStyle == BorderNone {
			borderSize = 0
		}

		titleOffset := 0
		if b.Title != "" {
			_, titleHeight := monitor.MeasureText(monitorID, b.Title, b.TitleStyle.Scale)
			titleOffset = titleHeight + 1
		}

		childRegion := Rect{
			X:      region.X + borderSize + b.Padding,
			Y:      region.Y + borderSize + b.Padding + titleOffset,
			Width:  region.Width - (borderSize+b.Padding)*2,
			Height: region.Height - (borderSize+b.Padding)*2 - titleOffset,
		}

		if childRegion.Width > 0 && childRegion.Height > 0 {
			b.Child.Render(monitorID, childRegion)
		}
	}
}
