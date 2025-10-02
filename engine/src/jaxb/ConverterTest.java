package jaxb;

import core.logic.program.SProgram;
import exception.ProgramValidationException;
import jaxb.generated.SInstruction;
import jaxb.generated.SInstructions;

public class ConverterTest {

    public static void main(String[] args) {
        System.out.println("Testing JAXB to Engine Converter...");

        // Test 1: Create a simple JAXB program
        jaxb.generated.SProgram jaxbProgram = createTestJAXBProgram();

        // Test 2: Convert to engine objects
        try {
            SProgram engineProgram = JAXBToEngineConverter.convertJAXBToEngine(jaxbProgram);

            System.out.println("✅ Conversion successful!");
            System.out.println("Engine Program: " + engineProgram.getName());
            System.out.println("Engine Instructions: " + engineProgram.getInstructionList().size());

            // Test 3: Verify instruction details
            if (engineProgram.getInstructionList().size() > 0) {
                System.out.println("First instruction: " +
                        engineProgram.getInstructionList().get(0).getName());
            }

        } catch (Exception e) {
            System.err.println("❌ Conversion failed: " + e.getMessage());
            e.printStackTrace();
        }

        // Test 4: Test validation with invalid label reference
        testInvalidLabelValidation();

        // Test 5: Test validation with EXIT label (should pass)
        testExitLabelValidation();
    }

    private static void testInvalidLabelValidation() {
        System.out.println("\nTesting invalid label validation...");
        
        jaxb.generated.SProgram program = new jaxb.generated.SProgram();
        program.setName("InvalidLabelTest");

        SInstructions instructions = new SInstructions();

        // Create GOTO instruction with undefined label
        SInstruction gotoInstruction = new SInstruction();
        gotoInstruction.setType("basic");
        gotoInstruction.setName("GOTO_LABEL");
        gotoInstruction.setSLabel("UNDEFINED_LABEL");
        instructions.getSInstruction().add(gotoInstruction);

        program.setSInstructions(instructions);

        try {
            JAXBToEngineConverter.convertJAXBToEngine(program);
            System.err.println("❌ Expected validation to fail for undefined label");
        } catch (IllegalArgumentException | ProgramValidationException e) {
            System.out.println("✅ Validation correctly caught undefined label: " + e.getMessage());
        }
    }

    private static void testExitLabelValidation() {
        System.out.println("\nTesting EXIT label validation...");
        
        jaxb.generated.SProgram program = new jaxb.generated.SProgram();
        program.setName("ExitLabelTest");

        SInstructions instructions = new SInstructions();

        // Create GOTO instruction with EXIT label (should be allowed)
        SInstruction gotoInstruction = new SInstruction();
        gotoInstruction.setType("basic");
        gotoInstruction.setName("GOTO_LABEL");
        gotoInstruction.setSLabel("EXIT");
        instructions.getSInstruction().add(gotoInstruction);

        program.setSInstructions(instructions);

        try {
            SProgram engineProgram = JAXBToEngineConverter.convertJAXBToEngine(program);
            System.out.println("✅ EXIT label validation passed successfully");
        } catch (IllegalArgumentException | ProgramValidationException e) {
            System.err.println("❌ EXIT label should be allowed: " + e.getMessage());
        }
    }

    private static jaxb.generated.SProgram createTestJAXBProgram() {
        // Create a test JAXB program manually
        jaxb.generated.SProgram program = new jaxb.generated.SProgram();
        program.setName("TestProgram");

        SInstructions instructions = new SInstructions();

        // Create test instruction 1: INCREASE
        SInstruction instruction1 = new SInstruction();
        instruction1.setType("basic");
        instruction1.setName("INCREASE");
        instruction1.setSVariable("X");
        instructions.getSInstruction().add(instruction1);

        // Create test instruction 2: JUMP_NOT_ZERO
        SInstruction instruction2 = new SInstruction();
        instruction2.setType("basic");
        instruction2.setName("JUMP_NOT_ZERO");
        instruction2.setSVariable("X");
        instruction2.setSLabel("LOOP");
        instructions.getSInstruction().add(instruction2);

        program.setSInstructions(instructions);

        System.out.println("Created test JAXB program with " +
                instructions.getSInstruction().size() + " instructions");

        return program;
    }
}
