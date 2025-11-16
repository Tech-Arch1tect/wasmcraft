package main

import (
	"fmt"
	"math"

	"github.com/wasmcraft/bindings/movement"
)

func main() {
	startPos, err := movement.GetPosition()
	if err != nil {
		panic(err)
	}
	squareSize := float32(5.0)
	numLaps := 3

	for lap := 1; lap <= numLaps; lap++ {
		lapStartPos, err := movement.GetPosition()
		if err != nil {
			panic(err)
		}

		for side := 1; side <= 4; side++ {
			_, err = movement.MoveForward(squareSize)
			if err != nil {
				panic(err)
			}
			_, err = movement.Rotate(90)
			if err != nil {
				panic(err)
			}
		}

		currentPos, err := movement.GetPosition()
		if err != nil {
			panic(err)
		}
		drift := calculateDistance(lapStartPos, currentPos)
		fmt.Printf("Lap %d: drift %.6f\n", lap, drift)
	}

	finalPos, err := movement.GetPosition()
	if err != nil {
		panic(err)
	}
	totalDrift := calculateDistance(startPos, finalPos)
	fmt.Printf("Total drift: %.6f blocks\n", totalDrift)
}

func calculateDistance(pos1, pos2 movement.Position) float64 {
	dx := pos2.X - pos1.X
	dy := pos2.Y - pos1.Y
	dz := pos2.Z - pos1.Z
	return math.Sqrt(dx*dx + dy*dy + dz*dz)
}
