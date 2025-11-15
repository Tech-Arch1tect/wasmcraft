package main

import (
	"fmt"

	"github.com/wasmcraft/bindings/world"
)

func main() {
	fmt.Println("=== Crop Farm Monitor ===")
	fmt.Println()

	blockBelow := world.GetBlock(world.BOTTOM)
	fmt.Printf("Block below: %s\n", blockBelow)

	// Check if it's a crop
	if blockBelow == "minecraft:wheat" {
		checkWheat()
	} else if blockBelow == "minecraft:carrots" {
		checkCarrots()
	} else if blockBelow == "minecraft:potatoes" {
		checkPotatoes()
	} else if blockBelow == "minecraft:beetroots" {
		checkBeetroots()
	} else if blockBelow == "minecraft:farmland" {
		checkFarmland()
	} else {
		fmt.Println("Not a crop block")
	}
}

func checkWheat() {
	age := world.GetBlockProperty(world.BOTTOM, "age")
	fmt.Printf("Wheat age: %s/7\n", age)

	if age == "7" {
		fmt.Println("✓ Ready to harvest!")
	} else {
		fmt.Println("✗ Still growing...")
	}
}

func checkCarrots() {
	age := world.GetBlockProperty(world.BOTTOM, "age")
	fmt.Printf("Carrots age: %s/7\n", age)

	if age == "7" {
		fmt.Println("✓ Ready to harvest!")
	} else {
		fmt.Println("✗ Still growing...")
	}
}

func checkPotatoes() {
	age := world.GetBlockProperty(world.BOTTOM, "age")
	fmt.Printf("Potatoes age: %s/7\n", age)

	if age == "7" {
		fmt.Println("✓ Ready to harvest!")
	} else {
		fmt.Println("✗ Still growing...")
	}
}

func checkBeetroots() {
	age := world.GetBlockProperty(world.BOTTOM, "age")
	fmt.Printf("Beetroots age: %s/3\n", age)

	if age == "3" {
		fmt.Println("✓ Ready to harvest!")
	} else {
		fmt.Println("✗ Still growing...")
	}
}

func checkFarmland() {
	moisture := world.GetBlockProperty(world.BOTTOM, "moisture")
	fmt.Printf("Farmland moisture: %s/7\n", moisture)

	if moisture == "7" {
		fmt.Println("✓ Fully hydrated")
	} else {
		fmt.Println("⚠ Needs water nearby")
	}
}
