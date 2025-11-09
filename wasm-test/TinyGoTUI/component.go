package tui

type Component interface {
	Render(monitor string, region Rect)
	MinSize(monitor string) (width, height int)
}
