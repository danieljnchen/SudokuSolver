import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SudokuSolver extends Application {
    private static Cell[][] cells = new Cell[9][9];
    private static StackPane[][] grid = new StackPane[9][9];
    private static int focusR;
    private static int focusC;

    public enum State {
        UNCHANGED,
        CHANGED,
        SOLVED
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox();
        root.setOnMouseClicked(mouseEvent -> root.requestFocus());
        Scene scene = new Scene(root, 370, 424);

        for (int r = 0; r < cells.length; ++r) {
            for (int c = 0; c < cells[0].length; ++c) {
                cells[r][c] = new Cell();
            }
        }

        VBox rowHolder = new VBox();
        HBox[] rows = new HBox[9];
        for (int r = 0; r < rows.length; ++r) {
            rows[r] = new HBox();
            for (int c = 0; c < cells.length; ++c) {
                grid[r][c] = new StackPane(new Rectangle(40, 40), new Label(""));
                ((Label) grid[r][c].getChildren().get(1)).setFont(Font.font("Times New Roman", 20));
                grid[r][c].getChildren().get(0).setStyle("-fx-fill: white; -fx-stroke: black;");
                rows[r].getChildren().add(grid[r][c]);
                if (c % 3 == 2 && c != rows.length - 1) {
                    rows[r].getChildren().add(new Line(0, 0, 0,40));
                }
            }
            rowHolder.getChildren().add(rows[r]);
            if (r % 3 == 2 && r != rows.length - 1) {
                rowHolder.getChildren().add(new Line(0, 0, 370,0));
            }
        }

        updateFocus(0, 0, 0, 0);

        root.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.LEFT) {
                if (focusC > 0) {
                    updateFocus(focusR, focusC, focusR, --focusC);
                }
            } else if (keyEvent.getCode() == KeyCode.RIGHT) {
                if (focusC < 8) {
                    updateFocus(focusR, focusC, focusR, ++focusC);
                }
            } else if (keyEvent.getCode() == KeyCode.UP) {
                if (focusR > 0) {
                    updateFocus(focusR, focusC, --focusR, focusC);
                }
            } else if (keyEvent.getCode() == KeyCode.DOWN) {
                if (focusR < 8) {
                    updateFocus(focusR, focusC, ++focusR, focusC);
                }
            } else if (keyEvent.getCode() == KeyCode.DIGIT1) {
                ((Label) grid[focusR][focusC].getChildren().get(1)).setText("1");
            } else if (keyEvent.getCode() == KeyCode.DIGIT2) {
                ((Label) grid[focusR][focusC].getChildren().get(1)).setText("2");
            } else if (keyEvent.getCode() == KeyCode.DIGIT3) {
                ((Label) grid[focusR][focusC].getChildren().get(1)).setText("3");
            } else if (keyEvent.getCode() == KeyCode.DIGIT4) {
                ((Label) grid[focusR][focusC].getChildren().get(1)).setText("4");
            } else if (keyEvent.getCode() == KeyCode.DIGIT5) {
                ((Label) grid[focusR][focusC].getChildren().get(1)).setText("5");
            } else if (keyEvent.getCode() == KeyCode.DIGIT6) {
                ((Label) grid[focusR][focusC].getChildren().get(1)).setText("6");
            } else if (keyEvent.getCode() == KeyCode.DIGIT7) {
                ((Label) grid[focusR][focusC].getChildren().get(1)).setText("7");
            } else if (keyEvent.getCode() == KeyCode.DIGIT8) {
                ((Label) grid[focusR][focusC].getChildren().get(1)).setText("8");
            } else if (keyEvent.getCode() == KeyCode.DIGIT9) {
                ((Label) grid[focusR][focusC].getChildren().get(1)).setText("9");
            } else if (keyEvent.getCode() == KeyCode.BACK_SPACE) {
                ((Label) grid[focusR][focusC].getChildren().get(1)).setText("");
            }
        });

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));

        VBox ui = new VBox();
        HBox buttons = new HBox();
        Button solve = new Button("Solve");
        solve.setOnAction(actionEvent -> {
            updateCells();
            solve();
        });
        Button clear = new Button("Clear");
        clear.setOnAction(actionEvent -> clearAll());
        Button chooseFile = new Button("Choose File");
        chooseFile.setOnAction(actionEvent -> {
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                loadFromFile(file);
            }
        });
        buttons.getChildren().addAll(clear, solve, chooseFile);
        HBox fileWriting = new HBox();
        TextField fileName = new TextField();
        Button writeFile = new Button("Write to File");
        writeFile.setOnAction(actionEvent -> writeToFile(fileName.getText()));
        fileWriting.getChildren().addAll(fileName, writeFile);
        ui.getChildren().addAll(buttons, fileWriting);

        root.getChildren().addAll(rowHolder, ui);

        primaryStage.setTitle("Sudoku Solver");
        primaryStage.setScene(scene);
        primaryStage.show();

        root.requestFocus();
    }

    public void updateFocus(int oldR, int oldC, int newR, int newC) {
        if (cells[oldR][oldC].isSolved()) {
            grid[oldR][oldC].getChildren().get(0).setStyle("-fx-fill: green; -fx-stroke: black;");
        } else {
            grid[oldR][oldC].getChildren().get(0).setStyle("-fx-fill: white; -fx-stroke: black;");
        }
        grid[newR][newC].getChildren().get(0).setStyle("-fx-fill: lightblue; -fx-stroke: black;");
    }

    public void solve() {
        solve(cells);
    }

    public boolean solve(Cell[][] cells) {
        State state;
        while ((state = prune(cells)) == State.CHANGED) {}
        if (state == State.SOLVED) {
            this.cells = cells;
            return true;
        }
        int[] cell = minPossibilities(cells);
        if (cell == null) {
            return false;
        } else {
            for (Integer i : cells[cell[0]][cell[1]].getPotentialValues()) {
                Cell[][] cellsClone = cloneCells(cells);
                cellsClone[cell[0]][cell[1]].setSolved(i);
                if (solve(cellsClone)) {
                    return true;
                }
            }
            return false;
        }
    }

    public Cell[][] cloneCells(Cell[][] cells) {
        Cell[][] out = new Cell[cells.length][cells[0].length];
        for (int r = 0; r < cells.length; ++r) {
            for (int c = 0; c < cells[0].length; ++c) {
                out[r][c] = cells[r][c].clone();
            }
        }
        return out;
    }

    public static int[] minPossibilities(Cell[][] cells) {
        int[] out = new int[2];
        for (int r = 0; r < 9; ++r) {
            for (int c = 0; c < 9; ++c) {
                if (!cells[r][c].isSolved()) {
                    if (out[0] != 0 && out[1] != 0) {
                        if (cells[r][c].getPotentialValues().size() <
                                cells[out[0]][out[1]].getPotentialValues().size()) {
                            out[0] = r;
                            out[1] = c;
                        }
                    } else {
                        out[0] = r;
                        out[1] = c;
                    }
                    if (cells[out[0]][out[1]].getPotentialValues().size() == 0) {
                        return null;
                    }
                }
            }
        }
        return out;
    }

    public State prune(Cell[][] cells) {
        class NumFilled {
            Integer num;
            boolean filled;
            public NumFilled(Integer num) {
                this.num = num;
            }

            public void setFilled() {
                filled = true;
            }
        }

        State out = State.UNCHANGED;

        // rows
        for (int r = 0; r < cells.length; ++r) {
            List<NumFilled> containedList = new ArrayList<>(9);
            for (int c = 0; c < cells[0].length; ++c) { // find which values are solved
                Integer i = cells[r][c].getValue();
                if (i != null) {
                    containedList.add(new NumFilled(i));
                }
            }
            for (int i = 0; i < containedList.size(); ++i) {
                for (int c = 0; c < cells[0].length; ++c) { // cross out the solved values for all the cells
                    if (!containedList.get(i).filled
                            && cells[r][c].isSolved()
                            && cells[r][c].getValue().equals(
                            containedList.get(i).num)) {
                        containedList.get(i).setFilled();
                    } else {
                        Integer solved = cells[r][c].crossOut(containedList.get(i).num);
                        if (solved != -1) {
                            out = State.CHANGED;
                            if (solved != 0) {
                                containedList.add(new NumFilled(solved));
                            }
                        }
                    }
                }
            }
            // if only one box has a number that is needed, fill it in
            Map<Integer, Integer> cellsContaining = new HashMap<>();
            for (int c = 0; c < cells[0].length; ++c) {
                if (!cells[r][c].isSolved()) {
                    for (Integer i : cells[r][c].getPotentialValues()) {
                        if (cellsContaining.containsKey(i)) {
                            if (cellsContaining.get(i) > 0) {
                                cellsContaining.put(i, -1);
                            }
                        } else {
                            cellsContaining.put(i, c);
                        }
                    }
                }
            }
            for (Integer i : cellsContaining.keySet()) {
                int c = cellsContaining.get(i);
                if (c > 0) {
                    cells[r][c].setSolved(i);
                    out = State.CHANGED;
                }
            }
        }
        // columns
        for (int c = 0; c < cells[0].length; ++c) {
            List<NumFilled> containedList = new ArrayList<>(9);
            for (int r = 0; r < cells.length; ++r) {
                Integer i = cells[r][c].getValue();
                if (i != null) {
                    containedList.add(new NumFilled(i));
                }
            }
            for (int i = 0; i < containedList.size(); ++i) {
                for (int r = 0; r < cells.length; ++r) { // cross out the solved values for all the cells
                    if (!containedList.get(i).filled
                            && cells[r][c].isSolved()
                            && cells[r][c].getValue().equals(
                            containedList.get(i).num)) {
                        containedList.get(i).setFilled();
                    } else {
                        Integer solved = cells[r][c].crossOut(containedList.get(i).num);
                        if (solved != -1) {
                            out = State.CHANGED;
                            if (solved != 0) {
                                containedList.add(new NumFilled(solved));
                            }
                        }
                    }
                }
            }
            Map<Integer, Integer> cellsContaining = new HashMap<>();
            for (int r = 0; r < cells[0].length; ++r) {
                if (!cells[r][c].isSolved()) {
                    for (Integer i : cells[r][c].getPotentialValues()) {
                        if (cellsContaining.containsKey(i)) {
                            if (cellsContaining.get(i) > 0) {
                                cellsContaining.put(i, -1);
                            }
                        } else {
                            cellsContaining.put(i, r);
                        }
                    }
                }
            }
            for (Integer i : cellsContaining.keySet()) {
                int r = cellsContaining.get(i);
                if (r > 0) {
                    cells[r][c].setSolved(i);
                    out = State.CHANGED;
                }
            }
        }

        // cells
        int numSolved = 0;
        for (int b = 0; b < 9; ++b) {
            List<NumFilled> containedList = new ArrayList<>(9);
            for (int r = (b - (b % 3)); r < b - (b % 3) + 3; ++r) {
                for (int c = (b % 3) * 3; c < ((b % 3) + 1) * 3; ++c) {
                    Integer i = cells[r][c].getValue();
                    if (i != null) {
                        containedList.add(new NumFilled(i));
                    }
                }
            }
            for (int i = 0; i < containedList.size(); ++i) {
                for (int r = (b - (b % 3)); r < b - (b % 3) + 3; ++r) {
                    for (int c = (b % 3) * 3; c < ((b % 3) + 1) * 3; ++c) {
                        if (!containedList.get(i).filled
                                && cells[r][c].isSolved()
                                && cells[r][c].getValue().equals(
                                containedList.get(i).num)) {
                            containedList.get(i).setFilled();
                        } else {
                            Integer solved = cells[r][c].crossOut(containedList.get(i).num);
                            if (solved != -1) {
                                out = State.CHANGED;
                                if (solved != -1) {
                                    out = State.CHANGED;
                                    if (solved != 0) {
                                        containedList.add(new NumFilled(solved));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Map<Integer, Integer> cellsContaining = new HashMap<>();
            for (int r = (b - (b % 3)); r < b - (b % 3) + 3; ++r) {
                for (int c = (b % 3) * 3; c < ((b % 3) + 1) * 3; ++c) {
                    if (!cells[r][c].isSolved()) {
                        for (Integer i : cells[r][c].getPotentialValues()) {
                            if (cellsContaining.containsKey(i)) {
                                if (cellsContaining.get(i) > 0) {
                                    cellsContaining.put(i, -1);
                                }
                            } else {
                                cellsContaining.put(i, r * 9 + c);
                            }
                        }
                    } else {
                        ++numSolved;
                        if (numSolved >= 81) {
                            updateGrid(cells);
                            return State.SOLVED;
                        }
                    }
                }
            }
            for (Integer i : cellsContaining.keySet()) {
                int n = cellsContaining.get(i);
                if (n > 0) {
                    cells[(n - (n % 9)) / 9][n % 9].setSolved(i);
                    out = State.CHANGED;
                }
            }
        }
        updateGrid(cells);
        return out;
    }

    public static void writeToFile(String fileName) {
        File currentDirectory = new File(System.getProperty("user.dir"));
        if (currentDirectory.listFiles((dir, name) -> {return fileName.equals(name);}).length == 0) {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
                for (int r = 0; r < 9; ++r) {
                    for (int c = 0; c < 9; ++c) {
                        String append = ((Label) grid[r][c].getChildren().get(1)).getText();
                        if (append.length() != 0) {
                            writer.append(append);
                        }
                        if (c != 8) {
                            writer.append(",");
                        }
                    }
                    writer.append(";");
                }
                writer.close();
            } catch (IOException e) {
                System.out.println("An IO error occurred");
            }
        } else {
            System.out.println("File already exists in the current directory; try changing the file name");
        }
    }
    public static void loadFromFile(File file) {
        clearAll();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            int c;
            int row = 0;
            int col = 0;
            while (-1 != (c = reader.read())) {
                if (c == ',') {
                    ++col;
                } else if (c == ';') {
                    ++row;
                    col = 0;
                } else if ('0' < c && '9' >= c) {
                    ((Label) grid[row][col].getChildren().get(1)).setText(String.valueOf(((char) c)));
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Could not find file");
        } catch (IOException e) {
            System.out.println("An IO error ocurred");
        }
        updateCells();
    }
    /**
     * Gets the coordinates (r, c) of the box with the least possibilities
     * @return a pair of coordinates, or null if the min number of possibilities
     * is 0
     */

    public static void clearAll() {
        for (Cell[] r : cells) {
            for (Cell c : r) {
                c.clear();
            }
        }
        updateGrid(cells);
    }

    private static void updateGrid(Cell[][] cells) {
        for (int r = 0; r < cells.length; ++r) {
            for (int c = 0; c < cells[0].length; ++c) {
                Integer num = cells[r][c].getValue();
                if (num != null) {
                    ((Label) grid[r][c].getChildren().get(1)).setText(String.valueOf(num));
                    if (r != focusR || c != focusC) {
                        grid[r][c].getChildren().get(0).setStyle("-fx-fill: green; -fx-stroke: black;");
                    }
                } else {
                    ((Label) grid[r][c].getChildren().get(1)).setText("");
                    if (r != focusR || c != focusC) {
                        grid[r][c].getChildren().get(0).setStyle("-fx-fill: white; -fx-stroke: black;");
                    }
                }
            }
        }
    }

    public static void updateCells() {
        for (int r = 0; r < cells.length; ++r) {
            for (int c = 0; c < cells[0].length; ++c) {
                String text = ((Label) grid[r][c].getChildren().get(1)).getText();
                if (!text.equals("")) {
                    cells[r][c].setSolved(Integer.parseInt(text));
                }
            }
        }
    }
}
