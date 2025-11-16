package main

import (
	"fmt"

	"github.com/wasmcraft/bindings/inventory"
	"github.com/wasmcraft/bindings/movement"
	"github.com/wasmcraft/bindings/world"
)

func main() {
	fmt.Println("=== Drone Builder Example ===")
	fmt.Println()

	selectedSlot := inventory.GetSelectedSlot()
	item := inventory.GetItem(selectedSlot)

	fmt.Printf("Selected slot: %d\n", selectedSlot)
	fmt.Printf("Item: %s (count: %d)\n", item.ID, item.Count)
	fmt.Println()

	if item.ID == "" || item.Count == 0 {
		fmt.Println("ERROR: No item in selected slot!")
		return
	}

	for i := 0; i < 3; i++ {
		blockBelow := world.GetBlock(world.BOTTOM)
		fmt.Printf("Step %d: Block below is %s\n", i+1, blockBelow)

		if blockBelow != "minecraft:air" {
			fmt.Printf("  Block already exists, skipping placement\n")
		} else {
			fmt.Printf("  Placing %s below...\n", item.ID)
			world.PlaceBlock(world.BOTTOM)

			currentItem := inventory.GetItem(selectedSlot)
			fmt.Printf("  SUCCESS! Blocks remaining: %d\n", currentItem.Count)

			if currentItem.Count == 0 {
				fmt.Println("  Out of blocks!")
				break
			}
		}

		if i < 2 {
			fmt.Println("  Moving forward 1 block...")
			movement.MoveForward(1.0)
			fmt.Println()
		}
	}

	fmt.Println("=== Build Complete ===")
	finalItem := inventory.GetItem(selectedSlot)
	blocksUsed := item.Count - finalItem.Count
	fmt.Printf("Blocks used: %d\n", blocksUsed)
	fmt.Printf("Blocks remaining: %d\n", finalItem.Count)
}
