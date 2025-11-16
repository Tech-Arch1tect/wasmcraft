package tui

type Spacer struct {
	Width  int
	Height int
}

func NewSpacer(width, height int) *Spacer {
	return &Spacer{
		Width:  width,
		Height: height,
	}
}

func NewHSpacer(width int) *Spacer {
	return &Spacer{
		Width:  width,
		Height: 0,
	}
}

func NewVSpacer(height int) *Spacer {
	return &Spacer{
		Width:  0,
		Height: height,
	}
}

func (s *Spacer) MinSize(monitorID string) (width, height int, err error) {
	return s.Width, s.Height, nil
}

func (s *Spacer) Render(monitorID string, region Rect) error {
	return nil
}
