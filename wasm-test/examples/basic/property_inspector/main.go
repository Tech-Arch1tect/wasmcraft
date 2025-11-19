package main

import (
	"fmt"
	"sort"

	"github.com/wasmcraft/bindings/sides"
	"github.com/wasmcraft/bindings/world"
)

func main() {
	fmt.Println("=== Property Inspector ===")
	fmt.Println("Inspecting all blocks around the drone...")
	fmt.Println()

	scanSides := []struct {
		name string
		side sides.Side
	}{
		{"BOTTOM", sides.Bottom},
		{"TOP", sides.Top},
		{"FRONT", sides.Front},
		{"BACK", sides.Back},
		{"LEFT", sides.Left},
		{"RIGHT", sides.Right},
	}

	for _, s := range scanSides {
		fmt.Printf("=== %s ===\n", s.name)

		// Get block ID
		blockID, err := world.GetBlock(s.side)
		if err != nil {
			panic(err)
		}
		fmt.Printf("Block: %s\n", blockID)

		// Get all properties
		props, err := world.GetBlockProperties(s.side)
		if err != nil {
			panic(err)
		}

		if len(props) == 0 {
			fmt.Println("No properties")
		} else {
			fmt.Printf("Properties (%d):\n", len(props))

			// Sort keys for consistent output
			keys := make([]string, 0, len(props))
			for k := range props {
				keys = append(keys, k)
			}
			sort.Strings(keys)

			for _, key := range keys {
				fmt.Printf("  %s = %s\n", key, props[key])
			}
		}

		fmt.Println()
	}
}
