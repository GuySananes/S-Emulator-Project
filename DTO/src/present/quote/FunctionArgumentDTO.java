package present.quote;

import java.util.List;

public class FunctionArgumentDTO implements ArgumentDTO {
    private final String ProgramOrFunctionName;
    private final List<ArgumentDTO> arguments;
    private final String representation;

    public FunctionArgumentDTO(String programOrFunctionName, List<ArgumentDTO> arguments, String representation) {
        ProgramOrFunctionName = programOrFunctionName;
        this.arguments = arguments;
        this.representation = representation;
    }

    public String getProgramOrFunctionName() {
        return ProgramOrFunctionName;
    }

    public List<ArgumentDTO> getArguments() {
        return arguments;
    }

    @Override
    public String getRepresentation() {
        return representation;
    }
}
