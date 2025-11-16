package main

import (
	"fmt"

	"github.com/wasmcraft/bindings/world"
)

func main() {
	fmt.Println("=== Crop Farm Monitor ===")
	fmt.Println()

	blockBelow, err := world.GetBlock(world.BOTTOM)
	if err != nil {
		panic(err)
	}
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
	age, err := world.GetBlockProperty(world.BOTTOM, "age")
	if err != nil {
		panic(err)
	}
	fmt.Printf("Wheat age: %s/7\n", age)

	if age == "7" {
		fmt.Println("✓ Ready to harvest!")
	} else {
		fmt.Println("✗ Still growing...")
	}
}

func checkCarrots() {
	age, err := world.GetBlockProperty(world.BOTTOM, "age")
	if err != nil {
		panic(err)
	}
	fmt.Printf("Carrots age: %s/7\n", age)

	if age == "7" {
		fmt.Println("✓ Ready to harvest!")
	} else {
		fmt.Println("✗ Still growing...")
	}
}

func checkPotatoes() {
	age, err := world.GetBlockProperty(world.BOTTOM, "age")
	if err != nil {
		panic(err)
	}
	fmt.Printf("Potatoes age: %s/7\n", age)

	if age == "7" {
		fmt.Println("✓ Ready to harvest!")
	} else {
		fmt.Println("✗ Still growing...")
	}
}

func checkBeetroots() {
	age, err := world.GetBlockProperty(world.BOTTOM, "age")
	if err != nil {
		panic(err)
	}
	fmt.Printf("Beetroots age: %s/3\n", age)

	if age == "3" {
		fmt.Println("✓ Ready to harvest!")
	} else {
		fmt.Println("✗ Still growing...")
	}
}

func checkFarmland() {
	moisture, err := world.GetBlockProperty(world.BOTTOM, "moisture")
	if err != nil {
		panic(err)
	}
	fmt.Printf("Farmland moisture: %s/7\n", moisture)

	if moisture == "7" {
		fmt.Println("✓ Fully hydrated")
	} else {
		fmt.Println("⚠ Needs water nearby")
	}
}
