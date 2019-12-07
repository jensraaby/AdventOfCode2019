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
	testMemory := []int{3, 9, 8, 9, 10, 9, 4, 9, 99, -1, 8}

	result := RunThroughProgram(testMemory, 8)
	fmt.Printf("Part 1: Finished execution. Memory state: %v\n", result.memory)
	fmt.Printf("Outputs: %v\n", result.outputs)
}

type ProgramResult struct {
	memory  []int
	outputs []int
}

func RunThroughProgram(memory []int, input int) ProgramResult {
	// Create copy of memory rather than mutating in place
	newMemory := make([]int, len(memory))
	copy(newMemory, memory)

	// Output slice
	outputs := make([]int, 0)

	instructionPointer := 0
	stopped := false

	for !stopped {
		instruction, err := parseProgram(memory[instructionPointer])
		if err != nil {
			// give up if we can't parse the program
			panic(err)
		}
		//fmt.Printf("Opcode: %v\n Mode 1: %v Mode 2: %v\n", instruction.opcode, instruction.mode1, instruction.mode2)
		if instruction.opcode == 99 {
			stopped = true
		} else {
			// load params from memory. This could fail if the mode is "position" and the index is then out of bounds
			param1, param1err := readFromMemory(instruction.mode1, instructionPointer+1, newMemory)
			if param1err != nil {
				// first parameter should always load (all instructions take at least 1 arg)
				panic(param1err)
			}
			param2, param2err := readFromMemory(instruction.mode2, instructionPointer+2, newMemory)
			if param2err != nil {
				// second parameter is optional, so it doesn't matter if it fails to read
			}
			if instruction.opcode == 1 { // Add
				newMemory[newMemory[instructionPointer+3]] = param1 + param2
				instructionPointer += 4 // Multiply
			} else if instruction.opcode == 2 {
				newMemory[newMemory[instructionPointer+3]] = param1 * param2
				instructionPointer += 4
			} else if instruction.opcode == 3 { // Store Input
				newMemory[newMemory[instructionPointer+1]] = input
				instructionPointer += 2
			} else if instruction.opcode == 4 { // Output
				outputs = append(outputs, param1)
				instructionPointer += 2
			} else if instruction.opcode == 5 { // Jump if true
				if param1 != 0 {
					instructionPointer = param2
				} else {
					instructionPointer += 3
				}
			} else if instruction.opcode == 6 { // Jump if false
				if param1 == 0 {
					instructionPointer = param2
				} else {
					instructionPointer += 3
				}
			} else if instruction.opcode == 7 { // Less than
				var result int
				if param1 < param2 {
					result = 1
				} else {
					result = 0
				}
				newMemory[newMemory[instructionPointer+3]] = result
				instructionPointer += 4
			} else if instruction.opcode == 8 { // Equals
				var result int
				if param1 == param2 {
					result = 1
				} else {
					result = 0
				}
				newMemory[newMemory[instructionPointer+3]] = result
				instructionPointer += 4
			} else {
				panic(fmt.Errorf("invalid opcode %v", instruction.opcode))
			}
		}
	}
	return ProgramResult{
		memory:  newMemory,
		outputs: outputs,
	}
}

func readFromMemory(mode int, index int, memory []int) (int, error) {
	if mode == 1 {
		return memory[index], nil
	} else {
		initialAddress := memory[index]
		if initialAddress > len(memory)-1 {
			return 0, fmt.Errorf("index out of bounds")
		}
		return memory[initialAddress], nil
	}
}

type instruction struct {
	opcode int
	mode1  int
	mode2  int
}

func parseProgram(prog int) (*instruction, error) {
	asPaddedString := fmt.Sprintf("%05d", prog)
	opcode, err := strconv.Atoi(asPaddedString[3:])
	if err != nil {
		return nil, fmt.Errorf("could not parse opcode: %v", err)
	}
	mode1, err1 := strconv.Atoi(fmt.Sprintf("%c", asPaddedString[2]))
	if err1 != nil {
		return nil, fmt.Errorf("could not parse mode 1: %v", err1)
	}
	mode2, err2 := strconv.Atoi(fmt.Sprintf("%c", asPaddedString[1]))
	if err2 != nil {
		return nil, fmt.Errorf("could not parse mode 2: %v", err2)
	}
	return &instruction{
		opcode,
		mode1,
		mode2,
	}, nil

}

// from day 2
func loadTestMemory(path string) []int {
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

	var memory []int
	for _, line := range lines {
		nums := strings.Split(line, ",")
		for _, num := range nums {
			parsed, err := strconv.Atoi(num)
			if err != nil {
				log.Fatal(err)
			}
			memory = append(memory, parsed)
		}

	}
	return memory
}
