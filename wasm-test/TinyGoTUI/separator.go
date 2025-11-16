package tui

import (
	"github.com/wasmcraft/bindings/monitor"
)

type Separator struct {
	Orientation int
	Color       Color
	Thickness   int
}

const (
	Horizontal = 0
	Vertical   = 1
)

func NewSeparator(orientation int) *Separator {
	return &Separator{
		Orientation: orientation,
		Color:       Gray,
		Thickness:   1,
	}
}

func NewHSeparator() *Separator {
	return NewSeparator(Horizontal)
}

func NewVSeparator() *Separator {
	return NewSeparator(Vertical)
}

func (s *Separator) SetColor(color Color) {
	s.Color = color
}

func (s *Separator) SetThickness(thickness int) {
	if thickness < 1 {
		thickness = 1
	}
	s.Thickness = thickness
}

func (s *Separator) MinSize(monitorID string) (width, height int, err error) {
	if s.Orientation == Horizontal {
		return 1, s.Thickness, nil
	}
	return s.Thickness, 1, nil
}

func (s *Separator) Render(monitorID string, region Rect) error {
	if s.Orientation == Horizontal {
		for i := 0; i < s.Thickness && i < region.Height; i++ {
			if err := monitor.DrawHLine(
				monitorID,
				region.X, region.Y+i,
				region.Width,
				s.Color.R, s.Color.G, s.Color.B,
			); err != nil {
				return err
			}
		}
	} else {
		for i := 0; i < s.Thickness && i < region.Width; i++ {
			if err := monitor.DrawVLine(
				monitorID,
				region.X+i, region.Y,
				region.Height,
				s.Color.R, s.Color.G, s.Color.B,
			); err != nil {
				return err
			}
		}
	}
	return nil
}
