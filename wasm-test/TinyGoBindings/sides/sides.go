package sides

type Side int

const (
	Bottom Side = 0
	Top    Side = 1
	Front  Side = 2
	Back   Side = 3
	Left   Side = 4
	Right  Side = 5
)

func (s Side) String() string {
	switch s {
	case Bottom:
		return "BOTTOM"
	case Top:
		return "TOP"
	case Front:
		return "FRONT"
	case Back:
		return "BACK"
	case Left:
		return "LEFT"
	case Right:
		return "RIGHT"
	default:
		return "UNKNOWN"
	}
}

func (s Side) ToInt() int {
	return int(s)
}

func All() []Side {
	return []Side{Bottom, Top, Front, Back, Left, Right}
}

func (s Side) IsValid() bool {
	return s >= Bottom && s <= Right
}
