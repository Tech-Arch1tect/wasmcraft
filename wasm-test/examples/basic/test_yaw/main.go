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

	// North (180°) = -Z
	err = movement.SetYaw(180)
	if err != nil {
		panic(err)
	}
	_, err = movement.MoveForward(3.0)
	if err != nil {
		panic(err)
	}
	err = movement.SetYaw(0)
	if err != nil {
		panic(err)
	}
	_, err = movement.MoveForward(3.0)
	if err != nil {
		panic(err)
	}

	// East (270°) = +X
	err = movement.SetYaw(270)
	if err != nil {
		panic(err)
	}
	_, err = movement.MoveForward(3.0)
	if err != nil {
		panic(err)
	}
	err = movement.SetYaw(90)
	if err != nil {
		panic(err)
	}
	_, err = movement.MoveForward(3.0)
	if err != nil {
		panic(err)
	}

	// South (0°) = +Z
	err = movement.SetYaw(0)
	if err != nil {
		panic(err)
	}
	_, err = movement.MoveForward(3.0)
	if err != nil {
		panic(err)
	}
	err = movement.SetYaw(180)
	if err != nil {
		panic(err)
	}
	_, err = movement.MoveForward(3.0)
	if err != nil {
		panic(err)
	}

	// West (90°) = -X
	err = movement.SetYaw(90)
	if err != nil {
		panic(err)
	}
	_, err = movement.MoveForward(3.0)
	if err != nil {
		panic(err)
	}
	err = movement.SetYaw(270)
	if err != nil {
		panic(err)
	}
	_, err = movement.MoveForward(3.0)
	if err != nil {
		panic(err)
	}

	endPos, err := movement.GetPosition()
	if err != nil {
		panic(err)
	}
	drift := math.Sqrt(
		math.Pow(endPos.X-startPos.X, 2) +
			math.Pow(endPos.Y-startPos.Y, 2) +
			math.Pow(endPos.Z-startPos.Z, 2))

	fmt.Printf("Drift: %.6f blocks\n", drift)
}
