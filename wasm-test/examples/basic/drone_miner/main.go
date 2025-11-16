package main

import (
	"fmt"

	"github.com/wasmcraft/bindings/world"
)

func main() {
	fmt.Println("=== Drone Mining Example ===")
	fmt.Println()

	blockInFront, err := world.GetBlock(world.FRONT)
	if err != nil {
		panic(err)
	}
	fmt.Printf("Block in front: %s\n", blockInFront)

	if blockInFront == "minecraft:air" {
		fmt.Println("No block to mine!")
		return
	}

	fmt.Println()
	fmt.Println("=== Checking Tool Compatibility ===")
	canBreak, err := world.CanBreak(world.FRONT)
	if err != nil {
		panic(err)
	}

	if !canBreak {
		fmt.Println("ERROR: Cannot break this block with current tool/hand!")
		return
	}

	fmt.Println("Block can be broken with current selection!")
	fmt.Println()

	fmt.Println("=== Breaking Block ===")
	fmt.Printf("Mining: %s\n", blockInFront)

	err = world.BreakBlock(world.FRONT)
	if err != nil {
		panic(err)
	}

	fmt.Println("SUCCESS! Block broken")

	blockAfter, err := world.GetBlock(world.FRONT)
	if err != nil {
		panic(err)
	}
	fmt.Printf("Block in front now: %s\n", blockAfter)

	fmt.Println("=== Mining Complete ===")
}
