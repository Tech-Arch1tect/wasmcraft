package main

import (
	"fmt"

	"github.com/wasmcraft/bindings/inventory"
)

func main() {
	fmt.Println("=== Inventory API Basics ===")
	fmt.Println()

	size := inventory.GetSize()
	fmt.Printf("Inventory size: %d slots\n", size)
	fmt.Println()

	currentSlot := inventory.GetSelectedSlot()
	fmt.Printf("Currently selected slot: %d\n", currentSlot)
	fmt.Println()

	fmt.Println("=== Inventory Contents ===")
	for slot := 0; slot < size; slot++ {
		if inventory.IsEmpty(slot) {
			continue
		}

		item := inventory.GetItem(slot)

		marker := ""
		if slot == currentSlot {
			marker = " <- SELECTED"
		}

		fmt.Printf("Slot %2d: %s x%d%s\n", slot, item.ID, item.Count, marker)
	}
	fmt.Println()

	fmt.Println("=== Changing Selected Slot ===")
	newSlot := 5
	inventory.SetSelectedSlot(newSlot)
	fmt.Printf("Selected slot changed to: %d\n", newSlot)
}
