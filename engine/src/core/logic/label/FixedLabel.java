package core.logic.label;

public enum FixedLabel implements Label{

    EXIT {
        @Override
        public String getRepresentation() {
            return "EXIT";
        }
    },
    EMPTY {
        @Override
        public String getRepresentation() {
            return "";
        }
    };

    @Override
    public abstract String getRepresentation();

    @Override
    public Label deepCopy() {
        return this;
    }

}