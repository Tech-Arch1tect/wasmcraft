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
	hasFlexChildren := false

	for i, child := range v.Children {
		w, h := child.MinSize(monitorID)

		if _, ok := child.(*FlexChild); ok {
			hasFlexChildren = true
		}

		if w > maxWidth {
			maxWidth = w
		}
		totalHeight += h
		if i < len(v.Children)-1 {
			totalHeight += v.Spacing
		}
	}

	if hasFlexChildren {
		return maxWidth, 0
	}

	return maxWidth, totalHeight
}

func (v *VBox) Render(monitorID string, region Rect) {
	if len(v.Children) == 0 {
		return
	}

	totalMinHeight := 0
	totalFlex := 0
	childSizes := make([]struct{ w, h, flex int }, len(v.Children))

	for i, child := range v.Children {
		w, h := child.MinSize(monitorID)
		childSizes[i].w = w
		childSizes[i].h = h
		childSizes[i].flex = 0

		if flexChild, ok := child.(*FlexChild); ok {
			childSizes[i].flex = flexChild.Flex
			totalFlex += flexChild.Flex
		}

		totalMinHeight += h
		if i < len(v.Children)-1 {
			totalMinHeight += v.Spacing
		}
	}

	remainingHeight := region.Height - totalMinHeight

	y := region.Y

	for i, child := range v.Children {
		h := childSizes[i].h
		flex := childSizes[i].flex

		if flex > 0 && totalFlex > 0 && remainingHeight > 0 {
			extraHeight := (remainingHeight * flex) / totalFlex
			h += extraHeight
		}

		if h <= 0 {
			continue
		}

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
