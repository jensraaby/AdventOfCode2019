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
	testProgram := []int{3, 9, 8, 9, 10, 9, 3, 9, 4, 9, 99, -1, 8}
	inputChan := make(chan int)
	outputChan := make(chan int)
	haltChan := make(chan bool)
	go ExecuteProgram(testProgram, inputChan, outputChan, haltChan)
	inputChan <- 9
	inputChan <- 8
	output := <-outputChan
	<-haltChan
	fmt.Printf("Finished test execution. Output: %v\n", output)

	testProg1 := []int{3, 15, 3, 16, 1002, 16, 10, 16, 1, 16, 15, 15, 4, 15, 99, 0, 0}
	signal1 := runAmplificationCircuit(testProg1, 0, 4, 3, 2, 1, 0)
	fmt.Printf("Output from example program 1: %v\n", signal1)

	testProg2 := []int{3, 23, 3, 24, 1002, 24, 10, 24, 1002, 23, -1, 23, 101, 5, 23, 23, 1, 24, 23, 23, 4, 23, 99, 0, 0}
	signal2 := runAmplificationCircuit(testProg2, 0, 0, 1, 2, 3, 4)
	fmt.Printf("Output from example program 2: %v\n", signal2)

	testProg3 := []int{3, 31, 3, 32, 1002, 32, 10, 32, 1001, 31, -2, 31, 1007, 31, 0, 33, 1002, 33, 7, 33, 1, 33, 31, 31, 1, 32, 31, 31, 4, 31, 99, 0, 0, 0}
	signal3 := runAmplificationCircuit(testProg3, 0, 1, 0, 4, 3, 2)
	fmt.Printf("Output from example program 2: %v\n", signal3)

	inputProgram := loadProgramFromFile("input.txt")
	sig := runAmplificationCircuit(inputProgram, 0, 0, 1, 2, 3, 4)
	fmt.Printf("Output from part 1 prog: %v\n", sig)

	phaseInputsPart1 := []int{0, 1, 2, 3, 4}
	findBestPhases(phaseInputsPart1, inputProgram)

	fmt.Println("###################################")
	fmt.Println("PART 2:")
	testProg := []int{3, 26, 1001, 26, -4, 26, 3, 27, 1002, 27, 2, 27, 1, 27, 26, 27, 4, 27, 1001, 28, -1, 28, 1005, 28, 6, 99, 0, 0, 5}
	resultTestP2 := runAmplificationCircuit(testProg, 0, 9, 8, 7, 6, 5)
	fmt.Printf("Result for test example 1: %v\n", resultTestP2)

	testProg = []int{3, 52, 1001, 52, -5, 52, 3, 53, 1, 52, 56, 54, 1007, 54, 5, 55, 1005, 55, 26, 1001, 54,
		-5, 54, 1105, 1, 12, 1, 53, 54, 53, 1008, 54, 0, 55, 1001, 55, 1, 55, 2, 53, 55, 53, 4,
		53, 1001, 56, -1, 56, 1005, 56, 6, 99, 0, 0, 0, 0, 10}
	resultTestP2 = runAmplificationCircuit(testProg, 0, 9, 7, 8, 5, 6)
	fmt.Printf("Result for test example 2: %v\n", resultTestP2)

	phaseInputsPart2 := []int{5, 6, 7, 8, 9}
	findBestPhases(phaseInputsPart2, inputProgram)
}

func findBestPhases(initialPhaseValues []int, inputProgram []int) (chan []int, [][]int, []int, int) {
	phasePermutationChan := make(chan []int)
	phasePermutations := make([][]int, 1)
	bestPermutation := initialPhaseValues
	bestResult := 0
	go permutations(initialPhaseValues, phasePermutationChan)
	for i := range phasePermutationChan {
		phasePermutations = append(phasePermutations, i)
		result := runAmplificationCircuit(inputProgram, 0, i[0], i[1], i[2], i[3], i[4])
		if result > bestResult {
			bestResult = result
			bestPermutation = i
		}
	}
	fmt.Printf("Best result: %v for permutation: %v\n", bestResult, bestPermutation)
	return phasePermutationChan, phasePermutations, bestPermutation, bestResult
}

func runAmplificationCircuit(software []int, inputSignal, phase1, phase2, phase3, phase4, phase5 int) int {
	eToA := make(chan int, 1) // size 1 so it waits for sig from E
	aToB := make(chan int)
	bToC := make(chan int)
	cToD := make(chan int)
	dToE := make(chan int)

	halt := make(chan bool)

	// wire together the inputs and outputs
	go ExecuteProgram(software, eToA, aToB, halt)
	go ExecuteProgram(software, aToB, bToC, halt)
	go ExecuteProgram(software, bToC, cToD, halt)
	go ExecuteProgram(software, cToD, dToE, halt)
	go ExecuteProgram(software, dToE, eToA, halt)

	// send phase values to inputs
	eToA <- phase1
	aToB <- phase2
	bToC <- phase3
	cToD <- phase4
	dToE <- phase5

	// send initial signal to first amplifier
	eToA <- inputSignal

	// wait for all 5 amplifiers to send halt signal
	for i := 0; i < 5; i++ {
		<-halt
	}

	// the result is the output of the last amplifier
	return <-eToA
}

func ExecuteProgram(memory []int, input chan int, outputChan chan int, halt chan bool) {
	// Create copy of memory rather than mutating in place
	newMemory := make([]int, len(memory))
	copy(newMemory, memory)

	instructionPointer := 0

	for {
		instruction, err := parseProgram(memory[instructionPointer])
		if err != nil {
			// give up if we can't parse the program
			panic(err)
		}
		if instruction.opcode == 99 {
			halt <- true
			return
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
				newMemory[newMemory[instructionPointer+1]] = <-input // read from channel
				instructionPointer += 2
			} else if instruction.opcode == 4 { // Output
				outputChan <- param1 // send to channel
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

func permutations(initial []int, out chan []int) {
	// algorithm from https://quickperm.org/
	a := make([]int, len(initial))
	out <- a
	copy(a, initial)
	N := len(a)
	p := make([]int, N+1)
	for i := 0; i < N+1; i++ {
		p[i] = i
	}
	i := 1
	for i < N {
		p[i]--
		j := 0
		if i%2 == 1 {
			j = p[i]
		}
		a[i], a[j] = a[j], a[i]
		out <- a
		i = 1
		for p[i] == 0 {
			p[i] = i
			i++
		}
	}
	close(out)
}

func loadProgramFromFile(path string) []int {
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
