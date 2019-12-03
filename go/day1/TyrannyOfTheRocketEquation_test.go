package main

import "testing"

func TestCalculateFuelRequirement(t *testing.T) {
	tables := []struct {
		input    int
		expected int
	}{
		{12, 2},
		{14, 2},
		{1969, 654},
		{100756, 33583},
	}

	for _, table := range tables {
		fuel := CalculateFuelRequirement(float64(table.input))
		if fuel != float64(table.expected) {
			t.Errorf("Mass was incorrect, got %f, want %f.", fuel, float64(table.expected))
		}
	}

}
