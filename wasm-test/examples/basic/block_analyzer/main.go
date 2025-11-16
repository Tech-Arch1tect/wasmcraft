package main

import (
	"fmt"
	"strings"

	"github.com/wasmcraft/bindings/world"
)

func main() {
	fmt.Println("=== Block Analyzer ===")
	fmt.Println("Analyzing block in front...")
	fmt.Println()

	blockID, err := world.GetBlock(world.FRONT)
	if err != nil {
		panic(err)
	}
	fmt.Printf("Block ID: %s\n", blockID)
	fmt.Println()

	// Get all tags
	tags, err := world.GetBlockTags(world.FRONT)
	if err != nil {
		panic(err)
	}

	if len(tags) == 0 {
		fmt.Println("Block has no tags")
		return
	}

	fmt.Printf("Block has %d tags:\n", len(tags))
	fmt.Println()

	// Categorize tags
	mineable := []string{}
	materials := []string{}
	other := []string{}

	for _, tag := range tags {
		if strings.HasPrefix(tag, "minecraft:mineable/") {
			tool := strings.TrimPrefix(tag, "minecraft:mineable/")
			mineable = append(mineable, tool)
		} else if strings.Contains(tag, "material") || strings.Contains(tag, "ore") ||
			strings.Contains(tag, "log") || strings.Contains(tag, "plank") {
			materials = append(materials, tag)
		} else {
			other = append(other, tag)
		}
	}

	// Display categorized tags
	if len(mineable) > 0 {
		fmt.Println("Mineable with:")
		for _, tool := range mineable {
			fmt.Printf("  - %s\n", tool)
		}
		fmt.Println()
	}

	if len(materials) > 0 {
		fmt.Println("Material Categories:")
		for _, mat := range materials {
			fmt.Printf("  - %s\n", mat)
		}
		fmt.Println()
	}

	if len(other) > 0 {
		fmt.Println("Other Tags:")
		for _, tag := range other {
			fmt.Printf("  - %s\n", tag)
		}
	}
}
