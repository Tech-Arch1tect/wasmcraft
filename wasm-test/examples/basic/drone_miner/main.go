package main

import (
	"fmt"

	"github.com/wasmcraft/bindings/world"
)

func main() {
	fmt.Println("=== Drone Mining Example ===")
	fmt.Println()

	blockInFront := world.GetBlock(world.FRONT)
	fmt.Printf("Block in front: %s\n", blockInFront)

	if blockInFront == "minecraft:air" {
		fmt.Println("No block to mine!")
		return
	}

	fmt.Println()
	fmt.Println("=== Checking Tool Compatibility ===")
	canBreak := world.CanBreak(world.FRONT)

	if !canBreak {
		fmt.Println("ERROR: Cannot break this block with current tool/hand!")
		return
	}

	fmt.Println("Block can be broken with current selection!")
	fmt.Println()

	fmt.Println("=== Breaking Block ===")
	fmt.Printf("Mining: %s\n", blockInFront)

	world.BreakBlock(world.FRONT)

	fmt.Println("SUCCESS! Block broken")

	blockAfter := world.GetBlock(world.FRONT)
	fmt.Printf("Block in front now: %s\n", blockAfter)

	fmt.Println("=== Mining Complete ===")
}
