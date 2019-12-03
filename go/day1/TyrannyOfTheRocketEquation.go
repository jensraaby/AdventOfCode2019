package main

import (
	"bufio"
	"fmt"
	"log"
	"math"
	"os"
	"strconv"
)

func main() {
	fmt.Println("Starting up!")

	args := os.Args[1:]
	var masses []float64

	if len(args) == 0 {
		fmt.Println("Missing required argument - input text file")
		os.Exit(1)
	} else {
		filename := args[0]
		masses = extractMasses(filename)
	}

	var fuelForModules float64
	var totalFuel float64
	for _, mass := range masses {
		fuelRequirement := CalculateFuelRequirement(mass)
		fmt.Printf("Fuel for mass %f: %f\n", mass, fuelRequirement)
		fuelForModules += fuelRequirement

		extraFuel := CalculateFuelNeededForFuel(fuelRequirement)
		fmt.Printf("Extra fuel needed for fuel mass of %f: %f\n\n", fuelRequirement, extraFuel)
		totalFuel += fuelRequirement + extraFuel
	}
	fmt.Printf("Fuel needed for modules: %f\n", fuelForModules)
	fmt.Printf("Total fuel requirement with fuel mass taken into account: %f\n", totalFuel)

}

func CalculateFuelRequirement(mass float64) float64 {
	return math.Floor(mass/3) - 2
}

func CalculateFuelNeededForFuel(fuel float64) float64 {
	initialFuel := math.Floor(fuel/3) - 2
	if initialFuel <= 0 {
		return 0
	} else {
		return initialFuel + CalculateFuelNeededForFuel(initialFuel)
	}
}

func extractMasses(path string) []float64 {
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

	var masses []float64
	for _, line := range lines {
		fl, err := strconv.ParseFloat(line, 64)
		if err != nil {
			log.Fatal(err)
		}
		masses = append(masses, fl)
	}
	return masses
}
