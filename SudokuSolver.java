import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
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
    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox();

        for (int r = 0; r < Box.boxes.length; ++r) {
            for (int c = 0; c < Box.boxes[0].length; ++c) {
                Box.boxes[r][c] = new Box();
            }
        }

        VBox rowHolder = new VBox();
        HBox[] rows = new HBox[9];
        for (int r = 0; r < rows.length; ++r) {
            rows[r] = new HBox();
            for (int c = 0; c < Box.boxes.length; ++c) {
                rows[r].getChildren().add(Box.boxes[r][c]);
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

        VBox ui = new VBox();
        HBox buttons = new HBox();
        Button solve = new Button("Solve");
        solve.setOnAction(actionEvent -> solve());
        Button guess = new Button("Guess");
        guess.setOnAction(actionEvent -> Box.guess(Box.minPossibilities()));
        Button clear = new Button("Clear");
        clear.setOnAction(actionEvent -> Box.clearAll());
        Button finish = new Button("Finish");
        finish.setOnAction(actionEvent -> {
            while (Box.numSolved < 81) {
                do {
                    Box.changed = false;
                    solve();
                } while (Box.changed);
                Box.guess(Box.minPossibilities());
            }
        });
        Button chooseFile = new Button("Choose File");
        chooseFile.setOnAction(actionEvent -> {
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                loadFromFile(file);
            }
        });
        buttons.getChildren().addAll(solve, guess, clear, finish, chooseFile);
        HBox fileWriting = new HBox();
        TextField fileName = new TextField();
        Button writeFile = new Button("Write to File");
        writeFile.setOnAction(actionEvent -> writeToFile(fileName.getText()));
        fileWriting.getChildren().addAll(fileName, writeFile);
        ui.getChildren().addAll(buttons, fileWriting);

        root.getChildren().addAll(rowHolder, ui);

        primaryStage.setTitle("Sudoku Solver");
        Scene scene = new Scene(root, 300, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void solve() {
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
        // rows
        for (int r = 0; r < Box.boxes.length; ++r) {
            List<NumFilled> containedList = new ArrayList<>(9);
            for (int c = 0; c < Box.boxes[0].length; ++c) { // find which values are solved
                Integer i = Box.boxes[r][c].getValue();
                if (i != null) {
                    containedList.add(new NumFilled(i));
                }
            }
            for (int i = 0; i < containedList.size(); ++i) {
                for (int c = 0; c < Box.boxes[0].length; ++c) { // cross out the solved values for all the boxes
                    if (!containedList.get(i).filled
                            && Box.boxes[r][c].isSolved()
                            && Box.boxes[r][c].getValue().equals(
                            containedList.get(i).num)) {
                        containedList.get(i).setFilled();
                    } else {
                        Integer solved = Box.boxes[r][c].crossOut(containedList.get(i).num);
                        if (solved != -1) {
                            containedList.add(new NumFilled(solved));
                        }
                    }
                }
            }
            // if only one box has a number that is needed, fill it in
            Map<Integer, Integer> boxesContaining = new HashMap<>();
            for (int c = 0; c < Box.boxes[0].length; ++c) {
                if (!Box.boxes[r][c].isSolved()) {
                    for (Integer i : Box.boxes[r][c].getPotentialValues()) {
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
                    Box.boxes[r][c].setSolved(i);
                }
            }
        }
        // columns
        for (int c = 0; c < Box.boxes[0].length; ++c) {
            List<NumFilled> containedList = new ArrayList<>(9);
            for (int r = 0; r < Box.boxes.length; ++r) {
                Integer i = Box.boxes[r][c].getValue();
                if (i != null) {
                    containedList.add(new NumFilled(i));
                }
            }
            for (int i = 0; i < containedList.size(); ++i) {
                for (int r = 0; r < Box.boxes.length; ++r) { // cross out the solved values for all the boxes
                    if (!containedList.get(i).filled
                            && Box.boxes[r][c].isSolved()
                            && Box.boxes[r][c].getValue().equals(
                            containedList.get(i).num)) {
                        containedList.get(i).setFilled();
                    } else {
                        Integer solved = Box.boxes[r][c].crossOut(containedList.get(i).num);
                        if (solved.intValue() != -1) {
                            containedList.add(new NumFilled(solved));
                        }
                    }
                }
            }
            Map<Integer, Integer> boxesContaining = new HashMap<>();
            for (int r = 0; r < Box.boxes[0].length; ++r) {
                if (!Box.boxes[r][c].isSolved()) {
                    for (Integer i : Box.boxes[r][c].getPotentialValues()) {
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
                    Box.boxes[r][c].setSolved(i);
                }
            }
        }

        // boxes
        for (int b = 0; b < 9; ++b) {
            List<NumFilled> containedList = new ArrayList<>(9);
            for (int r = (b - (b % 3)); r < b - (b % 3) + 3; ++r) {
                for (int c = (b % 3) * 3; c < ((b % 3) + 1) * 3; ++c) {
                    Integer i = Box.boxes[r][c].getValue();
                    if (i != null) {
                        containedList.add(new NumFilled(i));
                    }
                }
            }
            for (int i = 0; i < containedList.size(); ++i) {
                for (int r = (b - (b % 3)); r < b - (b % 3) + 3; ++r) {
                    for (int c = (b % 3) * 3; c < ((b % 3) + 1) * 3; ++c) {
                        if (!containedList.get(i).filled
                                && Box.boxes[r][c].isSolved()
                                && Box.boxes[r][c].getValue().equals(
                                containedList.get(i).num)) {
                            containedList.get(i).setFilled();
                        } else {
                            Integer solved = Box.boxes[r][c].crossOut(containedList.get(i).num);
                            if (solved.intValue() != -1) {
                                containedList.add(new NumFilled(solved));
                            }
                        }
                    }
                }
            }

            Map<Integer, Integer> boxesContaining = new HashMap<>();
            for (int r = (b - (b % 3)); r < b - (b % 3) + 3; ++r) {
                for (int c = (b % 3) * 3; c < ((b % 3) + 1) * 3; ++c) {
                    if (!Box.boxes[r][c].isSolved()) {
                        for (Integer i : Box.boxes[r][c].getPotentialValues()) {
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
                    Box.boxes[(n - (n % 9)) / 9][n % 9].setSolved(i);
                }
            }
        }
        Box.updateAll();
    }

    public static void writeToFile(String fileName) {
        File currentDirectory = new File(System.getProperty("user.dir"));
        if (currentDirectory.listFiles((dir, name) -> {return fileName.equals(name);}).length == 0) {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
                for (int r = 0; r < 9; ++r) {
                    for (int c = 0; c < 9; ++c) {
                        String append = Box.boxes[r][c].getText();
                        if (append.length() != 0) {
                            writer.append(Box.boxes[r][c].getText());
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
        Box.clearAll();
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
                    Box.boxes[row][col].setText(String.valueOf(((char) c)));
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
