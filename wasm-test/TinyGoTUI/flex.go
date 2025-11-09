package tui

type FlexChild struct {
	Child Component
	Flex  int
}

func NewFlexChild(child Component, flex int) *FlexChild {
	return &FlexChild{
		Child: child,
		Flex:  flex,
	}
}

func (f *FlexChild) MinSize(monitorID string) (width, height int) {
	if f.Flex > 0 {
		return 0, 0
	}
	return f.Child.MinSize(monitorID)
}

func (f *FlexChild) Render(monitorID string, region Rect) {
	f.Child.Render(monitorID, region)
}
