package tui

import (
	"fmt"

	"github.com/wasmcraft/bindings/monitor"
)

type ListStyle int

const (
	ListPlain ListStyle = iota
	ListBullet
	ListNumbered
)

type ListItem struct {
	Text  string
	Color Color
}

type List struct {
	Items       []ListItem
	Style       ListStyle
	BulletColor Color
	Spacing     int
	Scale       int
}

func NewList() *List {
	return &List{
		Items:       []ListItem{},
		Style:       ListPlain,
		BulletColor: White,
		Spacing:     1,
		Scale:       1,
	}
}

func (l *List) AddItem(text string, color Color) {
	l.Items = append(l.Items, ListItem{Text: text, Color: color})
}

func (l *List) AddPlainItem(text string) {
	l.AddItem(text, White)
}

func (l *List) SetStyle(style ListStyle) {
	l.Style = style
}

func (l *List) SetBulletColor(color Color) {
	l.BulletColor = color
}

func (l *List) SetSpacing(spacing int) {
	l.Spacing = spacing
}

func (l *List) SetScale(scale int) {
	if scale < 1 {
		scale = 1
	}
	l.Scale = scale
}

func (l *List) MinSize(monitorID string) (width, height int, err error) {
	if len(l.Items) == 0 {
		return 0, 0, nil
	}

	maxWidth := 0
	totalHeight := 0

	for i, item := range l.Items {
		prefix := l.getPrefix(i)
		fullText := prefix + item.Text

		w, h, err := monitor.MeasureText(monitorID, fullText, l.Scale)
		if err != nil {
			return 0, 0, err
		}
		if w > maxWidth {
			maxWidth = w
		}
		totalHeight += h

		if i < len(l.Items)-1 {
			totalHeight += l.Spacing
		}
	}

	return maxWidth, totalHeight, nil
}

func (l *List) getPrefix(index int) string {
	switch l.Style {
	case ListBullet:
		return "* "
	case ListNumbered:
		return fmt.Sprintf("%d. ", index+1)
	default:
		return ""
	}
}

func (l *List) Render(monitorID string, region Rect) error {
	if len(l.Items) == 0 {
		return nil
	}

	_, lineHeight, err := monitor.MeasureText(monitorID, "A", l.Scale)
	if err != nil {
		return err
	}
	lineHeight += l.Spacing

	y := region.Y

	for i, item := range l.Items {
		if y >= region.Y+region.Height {
			break
		}

		prefix := l.getPrefix(i)

		if prefix != "" {
			prefixWidth, _, err := monitor.MeasureText(monitorID, prefix, l.Scale)
			if err != nil {
				return err
			}
			_, err = monitor.DrawText(
				monitorID,
				region.X, y,
				prefix,
				l.BulletColor.R, l.BulletColor.G, l.BulletColor.B,
				0, 0, 0,
				l.Scale,
			)
			if err != nil {
				return err
			}

			_, err = monitor.DrawText(
				monitorID,
				region.X+prefixWidth, y,
				item.Text,
				item.Color.R, item.Color.G, item.Color.B,
				0, 0, 0,
				l.Scale,
			)
			if err != nil {
				return err
			}
		} else {
			_, err := monitor.DrawText(
				monitorID,
				region.X, y,
				item.Text,
				item.Color.R, item.Color.G, item.Color.B,
				0, 0, 0,
				l.Scale,
			)
			if err != nil {
				return err
			}
		}

		_, actualHeight, err := monitor.MeasureText(monitorID, item.Text, l.Scale)
		if err != nil {
			return err
		}
		y += actualHeight + l.Spacing
	}

	return nil
}
