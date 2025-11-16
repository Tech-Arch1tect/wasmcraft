package main

import (
	"fmt"

	"github.com/wasmcraft/bindings/redstone"
)

func main() {
	fmt.Println("=== Redstone I/O Example ===\n")

	sides := []redstone.Side{
		redstone.SideBottom,
		redstone.SideTop,
		redstone.SideFront,
		redstone.SideBack,
		redstone.SideLeft,
		redstone.SideRight,
	}

	fmt.Println("Reading redstone inputs:")
	for _, side := range sides {
		power, err := redstone.GetRedstone(side)
		if err != nil {
			panic(err)
		}
		fmt.Printf("  %-6s: %2d\n", side, power)
	}

	fmt.Println("\nSetting redstone outputs:")
	err := redstone.SetRedstone(redstone.SideFront, 15)
	if err != nil {
		panic(err)
	}
	fmt.Println("  FRONT  → 15 (max power)")

	err = redstone.SetRedstone(redstone.SideBack, 7)
	if err != nil {
		panic(err)
	}
	fmt.Println("  BACK   →  7 (half power)")

	err = redstone.SetRedstone(redstone.SideLeft, 0)
	if err != nil {
		panic(err)
	}
	fmt.Println("  LEFT   →  0 (off)")

	err = redstone.SetRedstone(redstone.SideRight, 15)
	if err != nil {
		panic(err)
	}
	fmt.Println("  RIGHT  → 15 (max power)")

	err = redstone.SetRedstone(redstone.SideTop, 10)
	if err != nil {
		panic(err)
	}
	fmt.Println("  TOP    → 10")

	err = redstone.SetRedstone(redstone.SideBottom, 3)
	if err != nil {
		panic(err)
	}
	fmt.Println("  BOTTOM →  3")

	fmt.Println("\nNote: FRONT is the direction you faced when placing the computer.")
}
