package main

import (
	"fmt"

	"github.com/wasmcraft/bindings/world"
)

func main() {
	fmt.Println("Scanning blocks around drone...")

	sides := []struct {
		name string
		side int
	}{
		{"Below", world.BOTTOM},
		{"Above", world.TOP},
		{"Front", world.FRONT},
		{"Back", world.BACK},
		{"Left", world.LEFT},
		{"Right", world.RIGHT},
	}

	for _, s := range sides {
		block := world.GetBlock(s.side)
		fmt.Printf("%s: %s\n", s.name, block)
	}
}
