package main

import "fmt"

func main() {
	fmt.Println("Starting infinite loop test...")
	fmt.Println("This program will run forever until you use 'stop' command")

	count := 0
	for {
		count++
		if count % 1000000000 == 0 {
			fmt.Printf("Still running... iteration %d\n", count)
		}
	}
}
