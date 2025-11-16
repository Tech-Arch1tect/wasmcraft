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
		block, err := world.GetBlock(s.side)
		if err != nil {
			panic(err)
		}
		fmt.Printf("%s: %s\n", s.name, block)
	}
}
