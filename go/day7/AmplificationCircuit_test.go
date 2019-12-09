package main

import (
	"testing"
)

func TestInputOutput(t *testing.T) {
	inputChan := make(chan int)
	outputChan := make(chan int)
	haltChan := make(chan bool)
	input := 3
	inputProgram := []int{3, 0, 4, 0, 99}
	go ExecuteProgram(inputProgram, inputChan, outputChan, haltChan)
	inputChan <- input
	result := <-outputChan
	<-haltChan

	if result != input {
		t.Errorf("Output should be same as input (%v should be %v)", result, input)
	}
}
