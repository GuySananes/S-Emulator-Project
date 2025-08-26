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
    public int compareTo(Label other) {
        if(other instanceof LabelImpl) {
            return -1;
        }

        return 0;
    }


}