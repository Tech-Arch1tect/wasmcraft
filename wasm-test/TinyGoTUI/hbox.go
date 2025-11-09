package tui

type HBox struct {
	Children []Component
	Spacing  int
}

func NewHBox() *HBox {
	return &HBox{
		Children: []Component{},
		Spacing:  2,
	}
}

func (hb *HBox) AddChild(child Component) {
	hb.Children = append(hb.Children, child)
}

func (hb *HBox) SetSpacing(spacing int) {
	hb.Spacing = spacing
}

func (hb *HBox) MinSize(monitorID string) (width, height int) {
	if len(hb.Children) == 0 {
		return 0, 0
	}

	totalWidth := 0
	maxHeight := 0

	for i, child := range hb.Children {
		w, h := child.MinSize(monitorID)
		totalWidth += w
		if h > maxHeight {
			maxHeight = h
		}
		if i < len(hb.Children)-1 {
			totalWidth += hb.Spacing
		}
	}

	return totalWidth, maxHeight
}

func (hb *HBox) Render(monitorID string, region Rect) {
	if len(hb.Children) == 0 {
		return
	}

	x := region.X

	for _, child := range hb.Children {
		w, h := child.MinSize(monitorID)

		if x+w > region.X+region.Width {
			break
		}

		childRegion := Rect{
			X:      x,
			Y:      region.Y,
			Width:  w,
			Height: h,
		}

		child.Render(monitorID, childRegion)
		x += w + hb.Spacing
	}
}
