import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SudokuSolver extends Application {
    private static Box[][] boxes;

    @Override
    public void start(Stage primaryStage) throws Exception {
        VBox root = new VBox();

        boxes = new Box[9][9];
        for (int r = 0; r < boxes.length; ++r) {
            for (int c = 0; c < boxes[0].length; ++c) {
                boxes[r][c] = new Box();
            }
        }

        VBox rowHolder = new VBox();
        HBox[] rows = new HBox[9];
        for (int r = 0; r < rows.length; ++r) {
            rows[r] = new HBox();
            for (int c = 0; c < boxes.length; ++c) {
                rows[r].getChildren().add(boxes[r][c]);
                if (c % 3 == 2 && c != rows.length - 1) {
                    rows[r].getChildren().add(new Line(0, 0, 0, 25));
                }
            }
            rowHolder.getChildren().add(rows[r]);
            if (r % 3 == 2 && r != rows.length - 1) {
                rowHolder.getChildren().add(new Line(0, 0, 300,0));
            }
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));

        HBox buttons = new HBox();
        Button solve = new Button("Solve");
        solve.setOnAction(actionEvent -> (new Solver()).run());
        Button clear = new Button("Clear");
        clear.setOnAction(actionEvent -> clear());
        Button chooseFile = new Button("Choose File");
        chooseFile.setOnAction(actionEvent -> {
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                loadFromFile(file);
            }
        });
        buttons.getChildren().addAll(solve, clear, chooseFile);

        root.getChildren().addAll(rowHolder, buttons);

        primaryStage.setTitle("Sudoku Solver");
        Scene scene = new Scene(root, 300, 275);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void solve() {
        // rows
        for (int r = 0; r < boxes.length; ++r) {
            Set<Integer> contained = new HashSet<>();
            for (int c = 0; c < boxes[0].length; ++c) { // find which values are solved
                Integer i = boxes[r][c].getValue();
                if (i != null) {
                    contained.add(i);
                }
            }
            List<Integer> containedList = new ArrayList<>(9);
            containedList.addAll(contained);
            for (int i = 0; i < containedList.size(); ++i) {
                for (int c = 0; c < boxes[0].length; ++c) { // cross out the solved values for all the boxes
                    Integer solved = boxes[r][c].crossOut(containedList.get(i));
                    if (solved.intValue() != -1) {
                        contained.add(solved);
                        containedList.add(solved);
                    }
                }
            }
            // if only one box has a number that is needed, fill it in
            Map<Integer, Integer> boxesContaining = new HashMap<>();
            for (int c = 0; c < boxes[0].length; ++c) {
                if (!boxes[r][c].isSolved()) {
                    for (Integer i : boxes[r][c].getPotentialValues()) {
                        if (boxesContaining.containsKey(i)) {
                            if (boxesContaining.get(i).intValue() > 0) {
                                boxesContaining.put(i, -1);
                            }
                        } else {
                            boxesContaining.put(i, c);
                        }
                    }
                }
            }
            for (Integer i : boxesContaining.keySet()) {
                int c = boxesContaining.get(i);
                if (c > 0) {
                    boxes[r][c].setSolved(i);
                }
            }
        }
        // columns
        for (int c = 0; c < boxes[0].length; ++c) {
            Set<Integer> contained = new HashSet<>();
            for (int r = 0; r < boxes.length; ++r) {
                Integer i = boxes[r][c].getValue();
                if (i != null) {
                    contained.add(i);
                }
            }
            List<Integer> containedList = new ArrayList<>(9);
            containedList.addAll(contained);
            for (int i = 0; i < containedList.size(); ++i) {
                for (int r = 0; r < boxes.length; ++r) { // cross out the solved values for all the boxes
                    Integer solved = boxes[r][c].crossOut(containedList.get(i));
                    if (solved.intValue() != -1) {
                        contained.add(solved);
                        containedList.add(solved);
                    }
                }
            }
            Map<Integer, Integer> boxesContaining = new HashMap<>();
            for (int r = 0; r < boxes[0].length; ++r) {
                if (!boxes[r][c].isSolved()) {
                    for (Integer i : boxes[r][c].getPotentialValues()) {
                        if (boxesContaining.containsKey(i)) {
                            if (boxesContaining.get(i).intValue() > 0) {
                                boxesContaining.put(i, -1);
                            }
                        } else {
                            boxesContaining.put(i, r);
                        }
                    }
                }
            }
            for (Integer i : boxesContaining.keySet()) {
                int r = boxesContaining.get(i);
                if (r > 0) {
                    boxes[r][c].setSolved(i);
                }
            }
        }

        // boxes
        for (int b = 0; b < 9; ++b) {
            Set<Integer> contained = new HashSet<>();
            for (int r = (b - (b % 3)); r < b - (b % 3) + 3; ++r) {
                for (int c = (b % 3) * 3; c < ((b % 3) + 1) * 3; ++c) {
                    Integer i = boxes[r][c].getValue();
                    if (i != null) {
                        contained.add(i);
                    }
                }
            }
            List<Integer> containedList = new ArrayList<Integer>(9);
            containedList.addAll(contained);
            for (int i = 0; i < containedList.size(); ++i) {
                for (int r = (b - (b % 3)); r < b - (b % 3) + 3; ++r) {
                    for (int c = (b % 3) * 3; c < ((b % 3) + 1) * 3; ++c) {
                        Integer solved = boxes[r][c].crossOut(containedList.get(i));
                        if (solved.intValue() != -1) {
                            contained.add(solved);
                            containedList.add(solved);
                        }
                    }
                }
            }

            Map<Integer, Integer> boxesContaining = new HashMap<>();
            for (int r = (b - (b % 3)); r < b - (b % 3) + 3; ++r) {
                for (int c = (b % 3) * 3; c < ((b % 3) + 1) * 3; ++c) {
                    if (!boxes[r][c].isSolved()) {
                        for (Integer i : boxes[r][c].getPotentialValues()) {
                            if (boxesContaining.containsKey(i)) {
                                if (boxesContaining.get(i).intValue() > 0) {
                                    boxesContaining.put(i, -1);
                                }
                            } else {
                                boxesContaining.put(i, r * 9 + c);
                            }
                        }
                    }
                }
            }
            for (Integer i : boxesContaining.keySet()) {
                int n = boxesContaining.get(i);
                if (n > 0) {
                    boxes[(n - (n % 9)) / 9][n % 9].setSolved(i);
                }
            }
        }
    }

    private class Solver extends Thread {
        public void run() {
            long startTime = System.currentTimeMillis();
            while (Box.numSolved < 81 && (System.currentTimeMillis() - startTime) < 5000) {
                solve();
            }
        }
    }

    public static void clear() {
        for (Box[] row : boxes) {
            for (Box b : row) {
                b.clear();
            }
        }
    }

    public static void loadFromFile(File file) {
        clear();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            int c = -1;
            int row = 0;
            int col = 0;
            while (-1 != (c = reader.read())) {
                if (c == ',') {
                    ++col;
                } else if (c == ';') {
                    ++row;
                    col = 0;
                } else if ('0' < c && '9' >= c) {
                    boxes[row][col].setText(String.valueOf(((char) c)));
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Could not find file");
        } catch (IOException e) {
            System.out.println("An IO error ocurred");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
