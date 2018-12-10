import javafx.scene.control.TextField;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

public class Box extends TextField {
    public static Box[][] boxes = new Box[9][9];
    public static int numSolved;
    public static boolean changed = false;
    private static Stack<Guess> guessStack;

    private Stack<Status> statusStack;

    private class Status implements Cloneable {
        private Set<Integer> potentialValues;

        private Status(Set<Integer> potentialValues) {
            this.potentialValues = potentialValues;
        }

        @Override
        public Status clone() {
            return new Status((Set<Integer>) ((HashSet<Integer>)potentialValues).clone());
        }
    }

    private static class Guess {
        private Object[] potentialValues;
        private int index;
        private int[] cell;

        private Guess(Set<Integer> potentialValues, int[] cell) {
            this.potentialValues = potentialValues.toArray();
            this.cell = cell;
        }

        private boolean hasNext() {
            return index < potentialValues.length;
        }

        private Integer next() {
            return (Integer) potentialValues[index++];
        }
    }

    public Box() {
        super();
        statusStack = new Stack<>();
        statusStack.push(new Status(new HashSet<>()));
        guessStack = new Stack<>();
        clear();
    }

    public static boolean guess(int[] cell) {
        if (cell == null) {
            if (guessStack.isEmpty()) {
                return false; // grid is unsolvable
            } else if (!guessStack.peek().hasNext()) {
                guessStack.pop(); // backtrack to the last layer
                popAll();
                return guess(null);
            } else {
                popAll();
                newLayer();
                boxes[guessStack.peek().cell[0]][guessStack.peek().cell[1]].
                        setSolved(guessStack.peek().next());
                return true;
            }
        } else {
            newLayer();
            guessStack.push(new Guess(boxes[cell[0]][cell[1]].getPotentialValues(), cell));
            boxes[cell[0]][cell[1]].setSolved(guessStack.peek().next());
            return true;
        }
    }

    /**
     * Gets the coordinates (r, c) of the box with the least possibilities
     * @return a pair of coordinates, or null if the min number of possibilities
     * is 0
     */
    public static int[] minPossibilities() {
        int[] out = new int[2];
        for (int r = 0; r < 9; ++r) {
            for (int c = 0; c < 9; ++c) {
                if (!boxes[r][c].isSolved()) {
                    if (out[0] != 0 && out[1] != 0) {
                        if (boxes[r][c].getPotentialValues().size() <
                                boxes[out[0]][out[1]].getPotentialValues().size()) {
                            out[0] = r;
                            out[1] = c;
                        }
                    } else {
                        out[0] = r;
                        out[1] = c;
                    }
                    if (boxes[out[0]][out[1]].getPotentialValues().size() == 0) {
                        //System.out.println("Cell (" + r + ", " + c + ") has no options");
                        return null;
                    }
                }
            }
        }
        return out;
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

    /**
     * Cross out {@code i} from the list of potential values
     * @param i the number to cross out
     * @return -1 if nothing happened, or if there is only one value remaining,
     * return the value
     */
    public Integer crossOut(Integer i) {
        if (statusStack.peek().potentialValues.contains(i)) {
            statusStack.peek().potentialValues.remove(i);
            changed = true;
            if (statusStack.peek().potentialValues.size() == 1) {
                Integer value = statusStack.peek().potentialValues.iterator().next();
                setSolved(value);
                return value;
            }
        }
        return -1;
    }

    public Set<Integer> getPotentialValues() {
        return statusStack.peek().potentialValues;
    }

    public void setSolved(Integer i) {
        Iterator<Integer> iterator = getPotentialValues().iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().equals(i)) {
                iterator.remove();
            }
        }
        update();
    }

    public boolean isSolved() {
        return statusStack.peek().potentialValues.size() == 1;
    }

    public void clear() {
        numSolved = 0;
        for (int i = 1; i < 10; ++i) {
            statusStack.peek().potentialValues.add(i);
        }
        update();
    }

    public static void clearAll() {
        for (Box[] r : boxes) {
            for (Box c : r) {
                c.clear();
            }
        }
    }

    public static void popAll() {
        for (Box[] r : boxes) {
            for (Box c : r) {
                c.statusStack.pop();
                c.update();
            }
        }
    }

    public static void newLayer() {
        for (Box[] r : boxes) {
            for (Box c : r) {
                c.statusStack.push(c.statusStack.peek().clone());
            }
        }
    }

    private boolean update() {
        if (getPotentialValues().size() == 1) {
            setText(String.valueOf(getPotentialValues().iterator().next()));
            setStyle("-fx-background-color: green; -fx-border-color: black;");
            return true;
        } else {
            setText("");
            setStyle("-fx-background-color: white; -fx-border-color: black;");
            return false;
        }
    }

    public static void updateAll() {
        numSolved = 0;
        for (Box[] r : boxes) {
            for (Box c : r) {
                if (c.update()) {
                    ++numSolved;
                }
            }
        }
    }
}
