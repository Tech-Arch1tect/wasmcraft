package main

import (
	"fmt"
	"math"

	"github.com/wasmcraft/bindings/movement"
)

func main() {
	startPos := movement.GetPosition()
	squareSize := float32(5.0)
	numLaps := 3

	for lap := 1; lap <= numLaps; lap++ {
		lapStartPos := movement.GetPosition()

		for side := 1; side <= 4; side++ {
			movement.MoveForward(squareSize)
			movement.Rotate(90)
		}

		drift := calculateDistance(lapStartPos, movement.GetPosition())
		fmt.Printf("Lap %d: drift %.6f\n", lap, drift)
	}

	totalDrift := calculateDistance(startPos, movement.GetPosition())
	fmt.Printf("Total drift: %.6f blocks\n", totalDrift)
}

func calculateDistance(pos1, pos2 movement.Position) float64 {
	dx := pos2.X - pos1.X
	dy := pos2.Y - pos1.Y
	dz := pos2.Z - pos1.Z
	return math.Sqrt(dx*dx + dy*dy + dz*dz)
}
