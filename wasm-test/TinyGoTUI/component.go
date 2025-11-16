package tui

type Component interface {
	Render(monitor string, region Rect) error
	MinSize(monitor string) (width, height int, err error)
}
