package main

import (
	"fmt"
	"sort"
)

func main() {
	startNum := 134792
	endNum := 675810

	guessesPart1 := 0
	guessesPart2 := 0
	for i := startNum; i <= endNum; i++ {
		digits := numberToDigits(i)
		validP1 := isValidPassword(digits)
		validP2 := containsExactlyTwoSameNeighbours(digits)
		if validP1 {
			guessesPart1++
		}
		if validP1 && validP2 {
			guessesPart2++
		}
	}
	fmt.Printf("Number of valid passwords (Part 1): %d\n", guessesPart1)
	fmt.Printf("Number of valid passwords (Part 2): %d\n", guessesPart2)
}

func numberToDigits(number int) []int {
	if number > 0 {
		mod10 := number % 10
		div10 := number / 10
		return append(numberToDigits(div10), mod10)
	} else {
		return []int{}
	}
}

func isValidPassword(digits []int) bool {

	sortedDigits := make([]int, len(digits))
	copy(sortedDigits, digits)
	sort.Ints(sortedDigits)

	for i := range sortedDigits {
		if sortedDigits[i] != digits[i] {
			return false
		}
	}

	return containsTwoSameNeighbours(digits)
}

func containsTwoSameNeighbours(digits []int) bool {
	for i, d := range digits {
		if i == len(digits)-1 {
			return false
		}
		if d == digits[i+1] {
			return true
		}
	}
	return false
}

func containsExactlyTwoSameNeighbours(digits []int) bool {
	paddedDigits := make([]int, len(digits)+2)
	paddedDigits[0] = 10
	for i, d := range digits {
		paddedDigits[i+1] = d
	}
	paddedDigits[len(digits)+1] = 10

	for i := 1; i < len(paddedDigits)-2; i++ {
		valid := paddedDigits[i-1] != paddedDigits[i] && paddedDigits[i] == paddedDigits[i+1] && paddedDigits[i+1] != paddedDigits[i+2]
		if valid {
			return true
		}
	}
	return false
}
