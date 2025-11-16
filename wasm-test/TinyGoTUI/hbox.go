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

func (hb *HBox) MinSize(monitorID string) (width, height int, err error) {
	if len(hb.Children) == 0 {
		return 0, 0, nil
	}

	totalWidth := 0
	maxHeight := 0
	hasFlexChildren := false

	for i, child := range hb.Children {
		w, h, err := child.MinSize(monitorID)
		if err != nil {
			return 0, 0, err
		}

		if _, ok := child.(*FlexChild); ok {
			hasFlexChildren = true
		}

		totalWidth += w
		if h > maxHeight {
			maxHeight = h
		}
		if i < len(hb.Children)-1 {
			totalWidth += hb.Spacing
		}
	}

	if hasFlexChildren {
		return 0, maxHeight, nil
	}

	return totalWidth, maxHeight, nil
}

func (hb *HBox) Render(monitorID string, region Rect) error {
	if len(hb.Children) == 0 {
		return nil
	}

	totalMinWidth := 0
	totalFlex := 0
	childSizes := make([]struct{ w, h, flex int }, len(hb.Children))

	for i, child := range hb.Children {
		w, h, err := child.MinSize(monitorID)
		if err != nil {
			return err
		}
		childSizes[i].w = w
		childSizes[i].h = h
		childSizes[i].flex = 0

		if flexChild, ok := child.(*FlexChild); ok {
			childSizes[i].flex = flexChild.Flex
			totalFlex += flexChild.Flex
		}

		totalMinWidth += w
		if i < len(hb.Children)-1 {
			totalMinWidth += hb.Spacing
		}
	}

	remainingWidth := region.Width - totalMinWidth

	x := region.X

	for i, child := range hb.Children {
		w := childSizes[i].w
		flex := childSizes[i].flex

		if flex > 0 && totalFlex > 0 && remainingWidth > 0 {
			extraWidth := (remainingWidth * flex) / totalFlex
			w += extraWidth
		}

		if x+w > region.X+region.Width {
			break
		}

		childRegion := Rect{
			X:      x,
			Y:      region.Y,
			Width:  w,
			Height: region.Height,
		}

		if err := child.Render(monitorID, childRegion); err != nil {
			return err
		}
		x += w + hb.Spacing
	}

	return nil
}
