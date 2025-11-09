package tui

type Color struct {
	R, G, B int
}

var (
	Black     = Color{0, 0, 0}
	White     = Color{255, 255, 255}
	Red       = Color{255, 0, 0}
	Green     = Color{0, 255, 0}
	Blue      = Color{0, 0, 255}
	Yellow    = Color{255, 255, 0}
	Cyan      = Color{0, 255, 255}
	Magenta   = Color{255, 0, 255}
	Gray      = Color{128, 128, 128}
	DarkGray  = Color{64, 64, 64}
	LightGray = Color{192, 192, 192}
)

type Rect struct {
	X, Y, Width, Height int
}

type Style struct {
	Foreground Color
	Background Color
	Scale      int
}

func DefaultStyle() Style {
	return Style{
		Foreground: White,
		Background: Black,
		Scale:      1,
	}
}

type Alignment int

const (
	AlignLeft Alignment = iota
	AlignCenter
	AlignRight
)

type BorderStyle int

const (
	BorderNone BorderStyle = iota
	BorderPixel
)
