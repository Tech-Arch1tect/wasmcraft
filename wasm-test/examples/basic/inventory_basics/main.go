package main

import (
	"fmt"

	"github.com/wasmcraft/bindings/inventory"
)

func main() {
	fmt.Println("=== Inventory API Basics ===")
	fmt.Println()

	size, err := inventory.GetSize()
	if err != nil {
		fmt.Printf("Error getting inventory size: %v\n", err)
		return
	}
	fmt.Printf("Inventory size: %d slots\n", size)
	fmt.Println()

	currentSlot, err := inventory.GetSelectedSlot()
	if err != nil {
		fmt.Printf("Error getting selected slot: %v\n", err)
		return
	}
	fmt.Printf("Currently selected slot: %d\n", currentSlot)
	fmt.Println()

	fmt.Println("=== Inventory Contents ===")
	for slot := 0; slot < size; slot++ {
		isEmpty, err := inventory.IsEmpty(slot)
		if err != nil {
			fmt.Printf("Error checking if slot %d is empty: %v\n", slot, err)
			continue
		}
		if isEmpty {
			continue
		}

		item, err := inventory.GetItem(slot)
		if err != nil {
			fmt.Printf("Error getting item in slot %d: %v\n", slot, err)
			continue
		}

		marker := ""
		if slot == currentSlot {
			marker = " <- SELECTED"
		}

		fmt.Printf("Slot %2d: %s x%d%s\n", slot, item.ID, item.Count, marker)
	}
	fmt.Println()

	fmt.Println("=== Changing Selected Slot ===")
	newSlot := 5
	if err := inventory.SetSelectedSlot(newSlot); err != nil {
		fmt.Printf("Error setting selected slot: %v\n", err)
		return
	}
	fmt.Printf("Selected slot changed to: %d\n", newSlot)
}
