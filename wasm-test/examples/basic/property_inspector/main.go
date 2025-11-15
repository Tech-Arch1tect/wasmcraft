package main

import (
	"fmt"
	"sort"

	"github.com/wasmcraft/bindings/world"
)

func main() {
	fmt.Println("=== Property Inspector ===")
	fmt.Println("Inspecting all blocks around the drone...")
	fmt.Println()

	sides := []struct {
		name string
		side int
	}{
		{"BOTTOM", world.BOTTOM},
		{"TOP", world.TOP},
		{"FRONT", world.FRONT},
		{"BACK", world.BACK},
		{"LEFT", world.LEFT},
		{"RIGHT", world.RIGHT},
	}

	for _, s := range sides {
		fmt.Printf("=== %s ===\n", s.name)

		// Get block ID
		blockID := world.GetBlock(s.side)
		fmt.Printf("Block: %s\n", blockID)

		// Get all properties
		props := world.GetBlockProperties(s.side)

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
