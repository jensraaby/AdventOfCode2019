package main

import (
	"testing"
)

func TestAdd(t *testing.T) {
	input := []int{1, 0, 0, 1, 99}
	expected := []int{1, 2, 0, 1, 99}

	actual := RunThroughProgram(input, 0).memory
	if !IntSliceEqual(actual, expected) {
		t.Errorf("Memory not as expected: %v should be %v", actual, expected)
	}
}

func TestMul(t *testing.T) {
	input := []int{2, 0, 0, 1, 99}
	expected := []int{2, 4, 0, 1, 99}

	actual := RunThroughProgram(input, 0).memory
	if !IntSliceEqual(actual, expected) {
		t.Errorf("Memory not as expected: %v should be %v", actual, expected)
	}
}

func TestInputOutput(t *testing.T) {
	input := 1
	inputMemory := []int{3, 0, 4, 0, 99}
	expectedMemory := []int{input, 0, 4, 0, 99}
	expectedOutput := []int{input}
	result := RunThroughProgram(inputMemory, input)

	if !IntSliceEqual(result.memory, expectedMemory) {
		t.Errorf("Memory not as expected: %v should be %v", result.memory, expectedMemory)
	}

	if !IntSliceEqual(result.outputs, expectedOutput) {
		t.Errorf("Program output not as expected: %v should be %v", result.outputs, expectedOutput)
	}
}

func TestComparison(t *testing.T) {
	input := 8
	progIsInput8 := []int{3, 9, 8, 9, 10, 9, 4, 9, 99, -1, 8}
	expectedOutput := []int{1}
	result := RunThroughProgram(progIsInput8, input)

	if !IntSliceEqual(result.outputs, expectedOutput) {
		t.Errorf("Program output not as expected: %v should be %v", result.outputs, expectedOutput)
	}

	inputNot8 := 808
	expectedOutputNot8 := []int{0}
	resultNot8 := RunThroughProgram(progIsInput8, inputNot8)

	if !IntSliceEqual(resultNot8.outputs, expectedOutputNot8) {
		t.Errorf("Program output not as expected: %v should be %v", resultNot8.outputs, expectedOutputNot8)
	}
}

func IntSliceEqual(a, b []int) bool {
	if len(a) != len(b) {
		return false
	}

	for i, v := range a {
		if v != b[i] {
			return false
		}
	}
	return true
}
