import javafx.scene.control.TextField;

import java.util.HashSet;
import java.util.Set;

public class Box extends TextField {
    public static int numSolved = 0;
    private boolean solved;
    private Set<Integer> potentialValues;

    public Box() {
        super();
        potentialValues = new HashSet<>();
        clear();
    }

    public Integer getValue() {
        if (!getText().equals("")) {
            try {
                Integer i = Integer.parseInt(getText());
                setSolved(i);
                return i;
            } catch (NumberFormatException e) {
                System.out.println("Invalid text: " + getText());
            }
        }
        return null;
    }

    public Integer crossOut(Integer i) {
        if (!solved) {
            if (potentialValues.contains(i)) {
                potentialValues.remove(i);
                if (potentialValues.size() == 1) {
                    Integer value = potentialValues.iterator().next();
                    setSolved(value);
                    return value;
                }
            }
        }
        return -1;
    }

    public Set<Integer> getPotentialValues() {
        return potentialValues;
    }

    public void setSolved(Integer i) {
        if (!solved) {
            setText(String.valueOf(i));
            solved = true;
            ++numSolved;
            setStyle("-fx-background-color: green; -fx-border-color: black;");
        }
    }

    public boolean isSolved() {
        return solved;
    }

    public void clear() {
        setText("");
        solved = false;
        numSolved = 0;
        potentialValues.clear();
        for (int i = 1; i < 10; ++i) {
            potentialValues.add(i);
        }
        setStyle("-fx-background-color: white; -fx-border-color: black;");
    }
}
