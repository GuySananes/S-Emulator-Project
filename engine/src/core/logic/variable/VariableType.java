package core.logic.variable;

public enum VariableType {
    RESULT {
        @Override
        public String getRepresentation(int number) {
            return "y";
        }
    },
    INPUT {
        @Override
        public String getRepresentation(int number) {
            return "x" + number;
        }
    },
    WORK {
        @Override
        public String getRepresentation(int number) {
            return "z" + number;
        }
    };

    public abstract String getRepresentation(int number);
}