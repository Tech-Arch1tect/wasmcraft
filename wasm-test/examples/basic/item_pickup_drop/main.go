package main

import (
	"fmt"

	"github.com/wasmcraft/bindings/inventory"
)

const (
	BOTTOM = 0
	TOP    = 1
	FRONT  = 2
)

func main() {
	fmt.Println("=== Item Pickup & Drop Demo ===")
	fmt.Println()

	// Scan for items below the drone
	fmt.Println("Scanning for items below...")
	itemsBelow, err := inventory.Scan(BOTTOM)
	if err != nil {
		fmt.Printf("Error scanning items: %v\n", err)
	} else if len(itemsBelow) == 0 {
		fmt.Println("No items found below")
	} else {
		fmt.Printf("Found %d item stack(s) below:\n", len(itemsBelow))
		for i, item := range itemsBelow {
			fmt.Printf("  %d. %s x%d\n", i+1, item.ID, item.Count)
		}
	}
	fmt.Println()

	// Scan for items on top
	fmt.Println("Scanning for items above...")
	itemsAbove, err := inventory.Scan(TOP)
	if err != nil {
		fmt.Printf("Error scanning items: %v\n", err)
	} else if len(itemsAbove) == 0 {
		fmt.Println("No items found above")
	} else {
		fmt.Printf("Found %d item stack(s) above:\n", len(itemsAbove))
		for i, item := range itemsAbove {
			fmt.Printf("  %d. %s x%d\n", i+1, item.ID, item.Count)
		}
	}
	fmt.Println()

	// Scan for items in front
	fmt.Println("Scanning for items in front...")
	itemsFront, err := inventory.Scan(FRONT)
	if err != nil {
		fmt.Printf("Error scanning items: %v\n", err)
	} else if len(itemsFront) == 0 {
		fmt.Println("No items found in front")
	} else {
		fmt.Printf("Found %d item stack(s) in front:\n", len(itemsFront))
		for i, item := range itemsFront {
			fmt.Printf("  %d. %s x%d\n", i+1, item.ID, item.Count)
		}
	}
	fmt.Println()

	// Try to suck items from below
	fmt.Println("Attempting to pick up items from below...")
	collected, err := inventory.Suck(BOTTOM)
	if err != nil {
		fmt.Printf("Warning during pickup: %v (collected %d items)\n", err, collected)
	} else {
		fmt.Printf("Successfully collected %d item entity(ies)\n", collected)
	}
	fmt.Println()

	// Show inventory contents after pickup
	fmt.Println("=== Inventory After Pickup ===")
	showInventory()
	fmt.Println()

	// Drop 5 items from slot 0 in front
	fmt.Println("Attempting to drop 5 items from slot 0...")
	item, err := inventory.GetItem(0)
	if err != nil {
		fmt.Printf("Error getting item from slot 0: %v\n", err)
	} else if item.ID == "minecraft:air" || item.Count == 0 {
		fmt.Println("Slot 0 is empty, nothing to drop")
	} else {
		dropped, err := inventory.Drop(0, 5)
		if err != nil {
			fmt.Printf("Error dropping items: %v\n", err)
		} else {
			fmt.Printf("Successfully dropped %d items (%s) in front\n", dropped, item.ID)
		}
	}
	fmt.Println()

	// Show final inventory
	fmt.Println("=== Final Inventory ===")
	showInventory()
}

func showInventory() {
	size, err := inventory.GetSize()
	if err != nil {
		fmt.Printf("Error getting inventory size: %v\n", err)
		return
	}

	currentSlot, _ := inventory.GetSelectedSlot()

	itemCount := 0
	for slot := 0; slot < size; slot++ {
		isEmpty, err := inventory.IsEmpty(slot)
		if err != nil || isEmpty {
			continue
		}

		item, err := inventory.GetItem(slot)
		if err != nil {
			continue
		}

		marker := ""
		if slot == currentSlot {
			marker = " <- SELECTED"
		}

		fmt.Printf("Slot %2d: %s x%d%s\n", slot, item.ID, item.Count, marker)
		itemCount++
	}

	if itemCount == 0 {
		fmt.Println("Inventory is empty")
	}
}
