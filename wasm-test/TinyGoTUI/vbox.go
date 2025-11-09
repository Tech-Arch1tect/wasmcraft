package tui

type VBox struct {
	Children []Component
	Spacing  int
}

func NewVBox() *VBox {
	return &VBox{
		Children: []Component{},
		Spacing:  2,
	}
}

func (v *VBox) AddChild(child Component) {
	v.Children = append(v.Children, child)
}

func (v *VBox) SetSpacing(spacing int) {
	v.Spacing = spacing
}

func (v *VBox) MinSize(monitorID string) (width, height int) {
	if len(v.Children) == 0 {
		return 0, 0
	}

	maxWidth := 0
	totalHeight := 0

	for i, child := range v.Children {
		w, h := child.MinSize(monitorID)
		if w > maxWidth {
			maxWidth = w
		}
		totalHeight += h
		if i < len(v.Children)-1 {
			totalHeight += v.Spacing
		}
	}

	return maxWidth, totalHeight
}

func (v *VBox) Render(monitorID string, region Rect) {
	if len(v.Children) == 0 {
		return
	}

	y := region.Y

	for _, child := range v.Children {
		_, h := child.MinSize(monitorID)

		if y+h > region.Y+region.Height {
			break
		}

		childRegion := Rect{
			X:      region.X,
			Y:      y,
			Width:  region.Width,
			Height: h,
		}

		child.Render(monitorID, childRegion)
		y += h + v.Spacing
	}
}
