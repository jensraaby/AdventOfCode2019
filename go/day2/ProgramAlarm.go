package main

import (
	"bufio"
	"fmt"
	"log"
	"os"
	"strconv"
	"strings"
)

func main() {
	testMemory := loadTestMemory("input.txt")

	// mutate the memory for first puzzle
	testMemory[1] = 12
	testMemory[2] = 2

	memoryAfterRun := RunThroughProgram(testMemory)
	fmt.Printf("Part 1: Finished execution. Memory state: %v\n", memoryAfterRun)

	cleanMemory := loadTestMemory("input.txt")
	noun, verb := FindSolutionForPart2(cleanMemory)
	fmt.Printf("Part 2: Found solution: %v %v", noun, verb)
}

func RunThroughProgram(memory []int64) []int64 {
	instructionPointer := 0

	stopped := false
	for !stopped {
		instruction := memory[instructionPointer]
		if instruction == 99 {
			stopped = true
		} else {
			address1, address2, address3 := memory[instructionPointer+1], memory[instructionPointer+2], memory[instructionPointer+3]
			if instruction == 1 {
				memory[address3] = memory[address1] + memory[address2]
			} else if instruction == 2 {
				memory[address3] = memory[address1] * memory[address2]
			} else {
				fmt.Printf("Invalid instruction: %v\n", instruction)
				os.Exit(1)
			}
			instructionPointer += 4
		}
	}
	return memory
}

func FindSolutionForPart2(initialMemory []int64) (int, int) {
	memoryCopy := make([]int64, len(initialMemory))

	for noun := 0; noun < 100; noun++ {
		for verb := 0; verb < 100; verb++ {
			copy(memoryCopy, initialMemory)
			memoryCopy[1] = int64(noun)
			memoryCopy[2] = int64(verb)
			result := RunThroughProgram(memoryCopy)
			if result[0] == 19690720 {
				return noun, verb
			}
		}
	}
	return 0, 0
}

// This assumes whole file is the memory
func loadTestMemory(path string) []int64 {
	file, err := os.Open(path)
	if err != nil {
		log.Fatal(err)
	}
	defer file.Close()

	scanner := bufio.NewScanner(file)
	var lines []string

	for scanner.Scan() {
		lines = append(lines, scanner.Text())
	}

	var memory []int64
	for _, line := range lines {
		nums := strings.Split(line, ",")
		for _, num := range nums {
			parsed, err := strconv.ParseInt(num, 10, 32)
			if err != nil {
				log.Fatal(err)
			}
			memory = append(memory, parsed)
		}

	}
	return memory
}
