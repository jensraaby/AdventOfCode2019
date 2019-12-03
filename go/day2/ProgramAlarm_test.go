package main

import "testing"

func TestExampleProgram(t *testing.T) {
	input := []int64{
		1, 9, 10, 3,
		2, 3, 11, 0,
		99,
		30, 40, 50}

	expected := []int64{
		3500, 9, 10, 70,
		2, 3, 11, 0,
		99,
		30, 40, 50}

	if !MemoryEqual(RunThroughProgram(input), expected) {
		t.Errorf("Memory not as expected: %v should be %v", input, expected)
	}
}

func TestAdd(t *testing.T) {
	input := []int64{1, 0, 0, 0, 99}
	expected := []int64{2, 0, 0, 0, 99}

	if !MemoryEqual(RunThroughProgram(input), expected) {
		t.Errorf("Memory not as expected: %v should be %v", input, expected)
	}
}

func TestMul(t *testing.T) {
	input := []int64{2, 3, 0, 3, 99}
	expected := []int64{2, 3, 0, 6, 99}

	if !MemoryEqual(RunThroughProgram(input), expected) {
		t.Errorf("Memory not as expected: %v should be %v", input, expected)
	}
}

func TestMul2(t *testing.T) {
	input := []int64{2, 4, 4, 5, 99, 0}
	expected := []int64{2, 4, 4, 5, 99, 9801}

	if !MemoryEqual(RunThroughProgram(input), expected) {
		t.Errorf("Memory not as expected: %v should be %v", input, expected)
	}
}

func TestAdd2(t *testing.T) {
	input := []int64{1, 1, 1, 4, 99, 5, 6, 0, 99}
	expected := []int64{30, 1, 1, 4, 2, 5, 6, 0, 99}

	if !MemoryEqual(RunThroughProgram(input), expected) {
		t.Errorf("Memory not as expected: %v should be %v", input, expected)
	}
}

func MemoryEqual(a, b []int64) bool {
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
