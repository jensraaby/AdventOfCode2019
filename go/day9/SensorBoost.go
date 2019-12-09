package main

import (
	"bufio"
	"fmt"
	"log"
	"os"
	"strconv"
	"strings"
)

func testProgram(program []int, name string) {
	computer := makeComputer(program)
	go computer.ExecuteProgram()
	go printOutputs(computer.output, name)
	<-computer.halt
}

func printOutputs(outputChan chan int, name string) {
	for output := range outputChan {
		fmt.Printf("Output for %s: %v\n", name, output)
	}
	return
}

func main() {
	//selfCopyingProgram := []int{109, 1, 204, -1, 1001, 100, 1, 100, 1008, 100, 16, 101, 1006, 101, 0, 99}
	//testProgram(selfCopyingProgram)
	//fmt.Printf("Finished test execution #1.\n")
	//
	//bigNumberProgram := []int{1102, 34915192, 34915192, 7, 4, 7, 99, 0}
	//testProgram(bigNumberProgram)
	//fmt.Printf("Finished test execution #2.\n")
	//
	//bigOutputProgram := []int{104, 1125899906842624, 99}
	//testProgram(bigOutputProgram)
	//fmt.Printf("Finished test execution #3.\n")

	fmt.Println("###################################\nEXECUTING PART 1")
	boostDiagnosticProgram := loadProgramFromFile("input.txt")
	diagnosticComputer := makeComputer(boostDiagnosticProgram)
	go diagnosticComputer.ExecuteProgram()
	go printOutputs(diagnosticComputer.output, "part 1")
	diagnosticComputer.input <- 1
	<-diagnosticComputer.halt
	fmt.Printf("Finished execution of diagnostic program.\n")

	fmt.Println("###################################\nEXECUTING PART 2")
	sensorBoostComputer := makeComputer(boostDiagnosticProgram)
	go printOutputs(sensorBoostComputer.output, "part 2")
	go sensorBoostComputer.ExecuteProgram()
	sensorBoostComputer.input <- 2
	<-sensorBoostComputer.halt
	fmt.Printf("Finished execution of sensor boost program.\n")

}

type IntCodeComputer struct {
	memory       []int
	input        chan int
	output       chan int
	halt         chan bool
	relativeBase int
}

func makeComputer(program []int) *IntCodeComputer {
	// each computer has its own copy of the program
	initialMemory := make([]int, len(program))
	copy(initialMemory, program)
	return &IntCodeComputer{
		memory:       initialMemory,
		input:        make(chan int, 1),
		output:       make(chan int),
		halt:         make(chan bool),
		relativeBase: 0,
	}
}

func (c *IntCodeComputer) expandMemory(newAddressValue int) {
	newSize := len(c.memory)
	for newSize < newAddressValue {
		newSize = (newSize + 1) * 2
	}
	newMemory := make([]int, newSize)
	copy(newMemory, c.memory)
	c.memory = newMemory
}

func (c *IntCodeComputer) write(address int, value int, mode int) {
	if address > len(c.memory) {
		c.expandMemory(address)
	}
	switch mode {
	case 0:
		c.memory[address] = value
	case 1:
		panic(fmt.Errorf("can't write in mode 1"))
	case 2:
		c.memory[c.relativeBase+address] = value
	default:
		panic(fmt.Sprintf("invalid mode %d", mode))
	}
}

func (c *IntCodeComputer) read(address int, mode int) int {
	switch mode {
	case 0:
		actualAddress := c.memory[address]
		if actualAddress > len(c.memory) {
			c.expandMemory(actualAddress)
		}
		return c.memory[actualAddress]
	case 1:
		return c.memory[address]
	case 2:
		actualAddress := c.relativeBase + c.memory[address]
		if actualAddress > len(c.memory) {
			c.expandMemory(actualAddress)
		}
		return c.memory[actualAddress]
	default:
		fmt.Printf("Invalid mode %v\n", mode)
		return 0
	}
}

func (c *IntCodeComputer) ExecuteProgram() {
	instructionPointer := 0
	for {
		instruction, err := parseProgram(c.memory[instructionPointer])
		if err != nil {
			// give up if we can't parse the program
			panic(err)
		}
		if instruction.opcode == 99 {
			close(c.output)
			c.halt <- true
			return
		} else {
			if instruction.opcode == 1 { // Add
				param1 := c.read(instructionPointer+1, instruction.mode1)
				param2 := c.read(instructionPointer+2, instruction.mode2)
				c.write(c.memory[instructionPointer+3], param1+param2, instruction.mode3)
				instructionPointer += 4 // Multiply
			} else if instruction.opcode == 2 {
				param1 := c.read(instructionPointer+1, instruction.mode1)
				param2 := c.read(instructionPointer+2, instruction.mode2)
				c.write(c.memory[instructionPointer+3], param1*param2, instruction.mode3)
				instructionPointer += 4
			} else if instruction.opcode == 3 { // Store Input
				input := <-c.input // read from input channel
				c.write(c.memory[instructionPointer+1], input, instruction.mode1)
				instructionPointer += 2
			} else if instruction.opcode == 4 { // Output
				param1 := c.read(instructionPointer+1, instruction.mode1)
				c.output <- param1 // send to channel
				instructionPointer += 2
			} else if instruction.opcode == 5 { // Jump if true
				param1 := c.read(instructionPointer+1, instruction.mode1)
				param2 := c.read(instructionPointer+2, instruction.mode2)
				if param1 != 0 {
					instructionPointer = param2
				} else {
					instructionPointer += 3
				}
			} else if instruction.opcode == 6 { // Jump if false
				param1 := c.read(instructionPointer+1, instruction.mode1)
				param2 := c.read(instructionPointer+2, instruction.mode2)
				if param1 == 0 {
					instructionPointer = param2
				} else {
					instructionPointer += 3
				}
			} else if instruction.opcode == 7 { // Less than
				param1 := c.read(instructionPointer+1, instruction.mode1)
				param2 := c.read(instructionPointer+2, instruction.mode2)
				var result int
				if param1 < param2 {
					result = 1
				} else {
					result = 0
				}
				c.write(c.memory[instructionPointer+3], result, instruction.mode3)
				instructionPointer += 4
			} else if instruction.opcode == 8 { // Equals
				var result int
				param1 := c.read(instructionPointer+1, instruction.mode1)
				param2 := c.read(instructionPointer+2, instruction.mode2)
				if param1 == param2 {
					result = 1
				} else {
					result = 0
				}
				c.write(c.memory[instructionPointer+3], result, instruction.mode3)
				instructionPointer += 4
			} else if instruction.opcode == 9 { // adjust relative base
				param1 := c.read(instructionPointer+1, instruction.mode1)
				c.relativeBase += param1
				instructionPointer += 2
			} else {
				panic(fmt.Errorf("invalid opcode %v", instruction.opcode))
			}
		}
	}
}

type instruction struct {
	opcode int
	mode1  int
	mode2  int
	mode3  int
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
	mode3, err3 := strconv.Atoi(fmt.Sprintf("%c", asPaddedString[0]))
	if err3 != nil {
		return nil, fmt.Errorf("could not parse mode 3: %v", err2)
	}
	return &instruction{
		opcode,
		mode1,
		mode2,
		mode3,
	}, nil
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
