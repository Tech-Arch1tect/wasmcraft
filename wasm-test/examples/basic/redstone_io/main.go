package main

import (
	"fmt"

	"github.com/wasmcraft/bindings/redstone"
	"github.com/wasmcraft/bindings/sides"
)

func main() {
	fmt.Println("=== Redstone I/O Example ===\n")

	allSides := sides.All()

	fmt.Println("Reading redstone inputs:")
	for _, side := range allSides {
		power, err := redstone.GetRedstone(side)
		if err != nil {
			panic(err)
		}
		fmt.Printf("  %-6s: %2d\n", side, power)
	}

	fmt.Println("\nSetting redstone outputs:")
	err := redstone.SetRedstone(sides.Front, 15)
	if err != nil {
		panic(err)
	}
	fmt.Println("  FRONT  → 15 (max power)")

	err = redstone.SetRedstone(sides.Back, 7)
	if err != nil {
		panic(err)
	}
	fmt.Println("  BACK   →  7 (half power)")

	err = redstone.SetRedstone(sides.Left, 0)
	if err != nil {
		panic(err)
	}
	fmt.Println("  LEFT   →  0 (off)")

	err = redstone.SetRedstone(sides.Right, 15)
	if err != nil {
		panic(err)
	}
	fmt.Println("  RIGHT  → 15 (max power)")

	err = redstone.SetRedstone(sides.Top, 10)
	if err != nil {
		panic(err)
	}
	fmt.Println("  TOP    → 10")

	err = redstone.SetRedstone(sides.Bottom, 3)
	if err != nil {
		panic(err)
	}
	fmt.Println("  BOTTOM →  3")

	fmt.Println("\nNote: FRONT is the direction you faced when placing the computer.")
}
