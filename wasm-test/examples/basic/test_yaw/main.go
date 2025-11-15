package main

import (
	"fmt"
	"math"

	"github.com/wasmcraft/bindings/movement"
)

func main() {
	startPos := movement.GetPosition()

	// North (180°) = -Z
	movement.SetYaw(180)
	movement.MoveForward(3.0)
	movement.SetYaw(0)
	movement.MoveForward(3.0)

	// East (270°) = +X
	movement.SetYaw(270)
	movement.MoveForward(3.0)
	movement.SetYaw(90)
	movement.MoveForward(3.0)

	// South (0°) = +Z
	movement.SetYaw(0)
	movement.MoveForward(3.0)
	movement.SetYaw(180)
	movement.MoveForward(3.0)

	// West (90°) = -X
	movement.SetYaw(90)
	movement.MoveForward(3.0)
	movement.SetYaw(270)
	movement.MoveForward(3.0)

	endPos := movement.GetPosition()
	drift := math.Sqrt(
		math.Pow(endPos.X-startPos.X, 2) +
			math.Pow(endPos.Y-startPos.Y, 2) +
			math.Pow(endPos.Z-startPos.Z, 2))

	fmt.Printf("Drift: %.6f blocks\n", drift)
}
