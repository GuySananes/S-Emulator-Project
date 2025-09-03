package core.logic.label;

import java.util.Comparator;

public class LabelComparator implements Comparator<Label> {

    @Override
    public int compare(Label l1, Label l2) {
        if (l1 == l2) {
            return 0;
        }

        if (l1 == FixedLabel.EXIT) {
            return 1;
        }
        if (l2 == FixedLabel.EXIT) {
            return -1;
        }

        return l1.getRepresentation().compareTo(l2.getRepresentation());
    }
}
