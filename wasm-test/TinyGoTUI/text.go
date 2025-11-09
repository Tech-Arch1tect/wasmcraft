package tui

import (
	"github.com/wasmcraft/bindings/monitor"
)

type Text struct {
	Content string
	Style   Style
	Align   Alignment
}

func NewText(content string) *Text {
	return &Text{
		Content: content,
		Style:   DefaultStyle(),
		Align:   AlignLeft,
	}
}

func NewTextStyled(content string, style Style) *Text {
	return &Text{
		Content: content,
		Style:   style,
		Align:   AlignLeft,
	}
}

func (t *Text) SetContent(content string) {
	t.Content = content
}

func (t *Text) SetStyle(style Style) {
	t.Style = style
}

func (t *Text) SetAlign(align Alignment) {
	t.Align = align
}

func (t *Text) MinSize(monitorID string) (width, height int) {
	return monitor.MeasureText(monitorID, t.Content, t.Style.Scale)
}

func (t *Text) Render(monitorID string, region Rect) {
	if t.Content == "" {
		return
	}

	textWidth, textHeight := monitor.MeasureText(monitorID, t.Content, t.Style.Scale)

	var x int
	switch t.Align {
	case AlignLeft:
		x = region.X
	case AlignCenter:
		x = region.X + (region.Width-textWidth)/2
	case AlignRight:
		x = region.X + region.Width - textWidth
	}

	y := region.Y + (region.Height-textHeight)/2

	if t.Style.Background != Black {
		monitor.FillRect(
			monitorID,
			x, y,
			textWidth, textHeight,
			t.Style.Background.R, t.Style.Background.G, t.Style.Background.B,
		)
	}

	monitor.DrawText(
		monitorID,
		x, y,
		t.Content,
		t.Style.Foreground.R, t.Style.Foreground.G, t.Style.Foreground.B,
		0, 0, 0,
		t.Style.Scale,
	)
}
