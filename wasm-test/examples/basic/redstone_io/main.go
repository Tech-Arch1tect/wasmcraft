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
		power := redstone.GetRedstone(side)
		fmt.Printf("  %-6s: %2d\n", side, power)
	}

	fmt.Println("\nSetting redstone outputs:")
	redstone.SetRedstone(redstone.SideFront, 15)
	fmt.Println("  FRONT  → 15 (max power)")

	redstone.SetRedstone(redstone.SideBack, 7)
	fmt.Println("  BACK   →  7 (half power)")

	redstone.SetRedstone(redstone.SideLeft, 0)
	fmt.Println("  LEFT   →  0 (off)")

	redstone.SetRedstone(redstone.SideRight, 15)
	fmt.Println("  RIGHT  → 15 (max power)")

	redstone.SetRedstone(redstone.SideTop, 10)
	fmt.Println("  TOP    → 10")

	redstone.SetRedstone(redstone.SideBottom, 3)
	fmt.Println("  BOTTOM →  3")

	fmt.Println("\nNote: FRONT is the direction you faced when placing the computer.")
}
