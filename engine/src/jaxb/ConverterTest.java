package jaxb;

import core.logic.program.SProgram;

public class ConverterTest {

    public static void main(String[] args) {
        System.out.println("Testing JAXB to Engine Converter...");

        // Test 1: Create a simple JAXB program
        jaxb.engine.src.jaxb.schema.generated.SProgram jaxbProgram = createTestJAXBProgram();

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
    }

    private static jaxb.engine.src.jaxb.schema.generated.SProgram createTestJAXBProgram() {
        // Create a test JAXB program manually
        jaxb.engine.src.jaxb.schema.generated.SProgram program = new jaxb.engine.src.jaxb.schema.generated.SProgram();
        program.setName("TestProgram");

        jaxb.engine.src.jaxb.schema.generated.SInstructions instructions = new jaxb.engine.src.jaxb.schema.generated.SInstructions();

        // Create test instruction 1: INCREASE
        jaxb.engine.src.jaxb.schema.generated.SInstruction instruction1 = new jaxb.engine.src.jaxb.schema.generated.SInstruction();
        instruction1.setType("basic");
        instruction1.setName("INCREASE");
        instruction1.setSVariable("X");
        instructions.getSInstruction().add(instruction1);

        // Create test instruction 2: JUMP_NOT_ZERO
        jaxb.engine.src.jaxb.schema.generated.SInstruction instruction2 = new jaxb.engine.src.jaxb.schema.generated.SInstruction();
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
