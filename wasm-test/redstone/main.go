package main

import (
	"fmt"

	"github.com/wasmcraft/bindings/redstone"
)

func main() {
	fmt.Println("=== Redstone Test ===")
	fmt.Println()

	fmt.Println("Reading inputs from all sides:")
	sides := []redstone.Side{
		redstone.SideBottom,
		redstone.SideTop,
		redstone.SideFront,
		redstone.SideBack,
		redstone.SideLeft,
		redstone.SideRight,
	}

	for _, side := range sides {
		power := redstone.GetRedstone(side)
		fmt.Printf("  %s input: %d\n", side, power)
	}

	fmt.Println()
	fmt.Println("Setting outputs:")
	fmt.Println("  FRONT: 15 (max power)")
	redstone.SetRedstone(redstone.SideFront, 15)

	fmt.Println("  BACK: 7 (half power)")
	redstone.SetRedstone(redstone.SideBack, 7)

	fmt.Println("  LEFT: 0 (off)")
	redstone.SetRedstone(redstone.SideLeft, 0)

	fmt.Println("  RIGHT: 15 (max power)")
	redstone.SetRedstone(redstone.SideRight, 15)

	fmt.Println("  TOP: 10")
	redstone.SetRedstone(redstone.SideTop, 10)

	fmt.Println("  BOTTOM: 3")
	redstone.SetRedstone(redstone.SideBottom, 3)

	fmt.Println()
	fmt.Println("Test complete!")
	fmt.Println("The FRONT side is the side you're facing when you placed the block.")
}
