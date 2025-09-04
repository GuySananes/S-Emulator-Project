package core.logic.label;

import java.util.Comparator;

public class LabelComparator implements Comparator<Label> {

    @Override
    public int compare(Label l1, Label l2) {
        if (l1 == l2) {
            return 0;
        }

        boolean isL1Exit = "EXIT".equalsIgnoreCase(l1.getRepresentation());
        boolean isL2Exit = "EXIT".equalsIgnoreCase(l2.getRepresentation());

        if (isL1Exit && !isL2Exit) {
            return 1;
        }
        if (!isL1Exit && isL2Exit) {
            return -1;
        }

        return l1.getRepresentation().compareTo(l2.getRepresentation());
    }
}
