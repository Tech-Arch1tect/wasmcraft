package main

import (
	"fmt"

	"github.com/wasmcraft/bindings/sides"
	"github.com/wasmcraft/bindings/world"
)

func main() {
	fmt.Println("Scanning blocks around drone...")

	scanSides := []struct {
		name string
		side sides.Side
	}{
		{"Below", sides.Bottom},
		{"Above", sides.Top},
		{"Front", sides.Front},
		{"Back", sides.Back},
		{"Left", sides.Left},
		{"Right", sides.Right},
	}

	for _, s := range scanSides {
		block, err := world.GetBlock(s.side)
		if err != nil {
			panic(err)
		}
		fmt.Printf("%s: %s\n", s.name, block)
	}
}
