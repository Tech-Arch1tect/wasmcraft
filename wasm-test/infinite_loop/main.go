package main

import (
	"fmt"
	"time"
)

func main() {
	fmt.Println("Starting infinite loop test...")
	fmt.Println("This program will run forever until you use 'stop' command")
	fmt.Println("")

	count := 0
	for {
		count++
		if count % 100000000 == 0 {
			fmt.Printf("Iteration %d (running for %.1f seconds)\n", count, float64(count)/100000000.0)
			time.Sleep(100 * time.Millisecond)
		}
	}
}
