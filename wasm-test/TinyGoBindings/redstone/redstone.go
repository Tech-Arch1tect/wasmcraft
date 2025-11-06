package redstone

//go:wasmimport env redstone_set
func redstoneSet(side, power uint32)

//go:wasmimport env redstone_get
func redstoneGet(side uint32) uint32

const (
	BOTTOM = 0
	TOP    = 1
	FRONT  = 2
	BACK   = 3
	LEFT   = 4
	RIGHT  = 5
)

type Side uint32

const (
	SideBottom Side = BOTTOM
	SideTop    Side = TOP
	SideFront  Side = FRONT
	SideBack   Side = BACK
	SideLeft   Side = LEFT
	SideRight  Side = RIGHT
)

func (s Side) String() string {
	switch s {
	case SideBottom:
		return "BOTTOM"
	case SideTop:
		return "TOP"
	case SideFront:
		return "FRONT"
	case SideBack:
		return "BACK"
	case SideLeft:
		return "LEFT"
	case SideRight:
		return "RIGHT"
	default:
		return "UNKNOWN"
	}
}

func SetRedstone(side Side, power int) {
	if power < 0 {
		power = 0
	}
	if power > 15 {
		power = 15
	}
	redstoneSet(uint32(side), uint32(power))
}

func GetRedstone(side Side) int {
	return int(redstoneGet(uint32(side)))
}
