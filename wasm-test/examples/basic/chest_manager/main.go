package main

import (
	"fmt"

	"github.com/wasmcraft/bindings/inventory"
)

const (
	BOTTOM = 0
	TOP    = 1
	FRONT  = 2
	BACK   = 3
	LEFT   = 4
	RIGHT  = 5
)

func main() {
	fmt.Println("=== Chest Manager Demo ===")

	// Check if there's a chest in front
	hasChest, err := inventory.DetectInventory(FRONT)
	if err != nil {
		fmt.Printf("Error detecting inventory: %v\n", err)
		return
	}

	if !hasChest {
		fmt.Println("No chest found in front of drone!")
		fmt.Println("Please place a chest in front and try again.")
		return
	}

	fmt.Println("Chest detected in front!")

	// Get chest size
	chestSize, err := inventory.GetExternalSize(FRONT)
	if err != nil {
		fmt.Printf("Error getting chest size: %v\n", err)
		return
	}
	fmt.Printf("Chest has %d slots\n", chestSize)

	// List chest contents
	fmt.Println("\n=== Chest Contents ===")
	emptySlots := 0
	for i := 0; i < chestSize; i++ {
		item, err := inventory.GetExternalItem(FRONT, i)
		if err != nil {
			fmt.Printf("Error reading slot %d: %v\n", i, err)
			continue
		}

		if item.ID == "minecraft:air" || item.Count == 0 {
			emptySlots++
		} else {
			fmt.Printf("Slot %2d: %dx %s\n", i, item.Count, item.ID)
		}
	}
	fmt.Printf("Empty slots: %d\n", emptySlots)

	// List drone inventory
	fmt.Println("\n=== Drone Inventory ===")
	droneSize, err := inventory.GetSize()
	if err != nil {
		fmt.Printf("Error getting drone inventory size: %v\n", err)
		return
	}

	droneItems := 0
	for i := 0; i < droneSize; i++ {
		item, err := inventory.GetItem(i)
		if err != nil {
			continue
		}

		if item.ID != "minecraft:air" && item.Count > 0 {
			fmt.Printf("Slot %2d: %dx %s\n", i, item.Count, item.ID)
			droneItems++
		}
	}

	if droneItems == 0 {
		fmt.Println("Drone inventory is empty!")
		fmt.Println("\nDemo: Try the following operations:")
		fmt.Println("1. Put items in the drone's inventory")
		fmt.Println("2. Run this program again to push items to the chest")
		return
	}

	// Demo: Push first item from drone to chest
	fmt.Println("\n=== Testing Push Operation ===")
	firstItem := -1
	for i := 0; i < droneSize; i++ {
		item, err := inventory.GetItem(i)
		if err != nil {
			continue
		}
		if item.ID != "minecraft:air" && item.Count > 0 {
			firstItem = i
			break
		}
	}

	if firstItem >= 0 {
		item, _ := inventory.GetItem(firstItem)
		fmt.Printf("Pushing items from drone slot %d (%dx %s)...\n", firstItem, item.Count, item.ID)

		// Find first empty slot in chest
		targetSlot := -1
		for i := 0; i < chestSize; i++ {
			chestItem, err := inventory.GetExternalItem(FRONT, i)
			if err != nil {
				continue
			}
			if chestItem.ID == "minecraft:air" {
				targetSlot = i
				break
			}
		}

		if targetSlot >= 0 {
			transferred, err := inventory.PushItem(FRONT, firstItem, targetSlot, 16)
			if err != nil {
				fmt.Printf("Error pushing items: %v\n", err)
			} else {
				fmt.Printf("Successfully transferred %d items to chest slot %d\n", transferred, targetSlot)
			}
		} else {
			fmt.Println("No empty slots in chest!")
		}
	}

	// Demo: Pull first item from chest
	fmt.Println("\n=== Testing Pull Operation ===")
	chestItemSlot := -1
	for i := 0; i < chestSize; i++ {
		item, err := inventory.GetExternalItem(FRONT, i)
		if err != nil {
			continue
		}
		if item.ID != "minecraft:air" && item.Count > 0 {
			chestItemSlot = i
			break
		}
	}

	if chestItemSlot >= 0 {
		item, _ := inventory.GetExternalItem(FRONT, chestItemSlot)
		fmt.Printf("Pulling items from chest slot %d (%dx %s)...\n", chestItemSlot, item.Count, item.ID)

		// Find empty slot in drone
		droneSlot := -1
		for i := 0; i < droneSize; i++ {
			droneItem, err := inventory.GetItem(i)
			if err != nil {
				continue
			}
			if droneItem.ID == "minecraft:air" {
				droneSlot = i
				break
			}
		}

		if droneSlot >= 0 {
			transferred, err := inventory.PullItem(FRONT, chestItemSlot, droneSlot, 8)
			if err != nil {
				fmt.Printf("Error pulling items: %v\n", err)
			} else {
				fmt.Printf("Successfully transferred %d items to drone slot %d\n", transferred, droneSlot)
			}
		} else {
			fmt.Println("No empty slots in drone!")
		}
	}

	fmt.Println("\n=== Demo Complete ===")
}
