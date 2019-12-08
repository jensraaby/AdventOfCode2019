package main

import (
	"testing"
)

func TestInputOutput(t *testing.T) {
	inputChan := make(chan int)
	outputChan := make(chan int)
	input := 3
	inputProgram := []int{3, 0, 4, 0, 99}
	go ExecuteProgram(inputProgram, inputChan, outputChan)
	inputChan <- input
	result := <-outputChan

	if result != input {
		t.Errorf("Output should be same as input (%v should be %v)", result, input)
	}
}
