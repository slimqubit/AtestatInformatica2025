package com.example.desktopsudoku.userinterface;

import com.example.desktopsudoku.problemdomain.Coordinates;
import com.example.desktopsudoku.problemdomain.SudokuGame;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.HashMap;

public class UserInterfaceImpl implements IUserInterfaceContract.View,
        EventHandler<KeyEvent> {
    private final Stage stage;
    private final Group root;
    private HashMap<Coordinates, SudokuTextField> textFieldCoordinates;
    private IUserInterfaceContract.EventListener listener;
    private static final double WINDOW_Y = 788;
    private static final double WINDOW_X = 668;
    private static final double BOARD_PADDING = 50;
    private static final double BOARD_X_AND_Y = 576;
    private static final Color WINDOW_BACKGROUND_COLOR = Color.rgb(0, 150, 136);
    private static final Color BOARD_BACKGROUND_COLOR = Color.rgb(224, 242, 241);
    private static final String SUDOKU = "Sudoku";
    private Text timerText;
    private int elapsedSeconds = 0;
    private SudokuTextField selectedTile = null;

    public UserInterfaceImpl(Stage stage) {
        this.stage = stage;
        this.root = new Group();
        this.textFieldCoordinates = new HashMap<>();
        initializeUserInterface();
    }

    @Override
    public void setListener(IUserInterfaceContract.EventListener listener) {
        this.listener = listener;
    }

    public void initializeUserInterface() {
        // System.out.println("initializing user interface");
        drawBackground(root);
        drawTitle(root);
        drawSudokuBoard(root);
        drawTextFields(root);
        drawGridLines(root);
        drawNumberSelectionBar(root);
        stage.show();
    }

    private void drawNumberSelectionBar(Group root) {
        final int tileSize = 64;
        final int buttonSize = 56;
        final int xOrigin = 50;
        final int yOffsetBelowBoard = 576 + 50 + 10;
        for (int i = 0; i < 9; i++) {
            int number = i + 1;
            javafx.scene.control.Button button = new javafx.scene.control.Button(Integer.toString(number));
            button.setFont(new Font(28));
            button.setPrefSize(buttonSize, buttonSize);
            button.setStyle("-fx-border-color: black; -fx-border-width: 2;");
            double xPosition = xOrigin + i * tileSize + (tileSize - buttonSize) / 2.0;
            button.setLayoutX(xPosition);
            button.setLayoutY(yOffsetBelowBoard);
            button.setOnMouseClicked(e -> {
                if (selectedTile != null && !selectedTile.isDisabled()) {
                    handleInput(number, selectedTile);
                }
            });
            root.getChildren().add(button);
        }
        javafx.scene.control.Button clearButton = new javafx.scene.control.Button("Șterge");
        clearButton.setFont(new Font(20));
        clearButton.setPrefSize(buttonSize * 1.65, buttonSize);
        clearButton.setStyle("-fx-border-color: black; -fx-border-width: 2;");
        double clearButtonX = xOrigin + (tileSize - buttonSize) / 2.0;
        double clearButtonY = yOffsetBelowBoard + buttonSize + 10;
        clearButton.setLayoutX(clearButtonX);
        clearButton.setLayoutY(clearButtonY);
        clearButton.setOnMouseClicked(e -> {
            if (selectedTile != null && !selectedTile.isDisabled()) {
                handleInput(0, selectedTile);
            }
        });
        root.getChildren().add(clearButton);
        javafx.scene.control.Button solveButton = new javafx.scene.control.Button("Rezolvă");
        solveButton.setFont(new Font(20));
        solveButton.setPrefSize(buttonSize * 1.80, buttonSize);
        solveButton.setStyle("-fx-border-color: black; -fx-border-width: 2;");
        double solveButtonX = clearButtonX + clearButton.getPrefWidth() + 10;
        double solveButtonY = clearButtonY;
        solveButton.setLayoutX(solveButtonX);
        solveButton.setLayoutY(solveButtonY);
        solveButton.setOnMouseClicked(e -> solveWithVisualization());
        root.getChildren().add(solveButton);
        timerText = new Text("Timp: 00:00");
        timerText.setFont(new Font(20));
        timerText.setStyle("-fx-font-weight: bold");
        timerText.setFill(Color.WHITE);
        double timerX = solveButtonX + solveButton.getPrefWidth() + 20;
        double timerY = solveButtonY + (buttonSize / 2.0) + 6;
        timerText.setLayoutX(timerX);
        timerText.setLayoutY(timerY);
        root.getChildren().add(timerText);
        startTimer();
    }

    private void solveWithVisualization() {
        new Thread(() -> {
            int[][] board = new int[9][9];
            for (int x = 0; x < 9; x++) {
                for (int y = 0; y < 9; y++) {
                    String text = textFieldCoordinates.get(new Coordinates(x, y)).getText();
                    board[x][y] = text.isEmpty() ? 0 : Integer.parseInt(text);
                }
            }
            solveStepByStep(board, 0, 0);
        }).start();
    }

    private boolean solveStepByStep(int[][] board, int row, int col) {
        if (row == 9) return true;
        if (col == 9) return solveStepByStep(board, row + 1, 0);
        if (board[row][col] != 0) return solveStepByStep(board, row, col + 1);
        for (int num = 1; num <= 9; num++) {
            if (isValidMove(board, row, col, num)) {
                board[row][col] = num;
                int finalRow = row;
                int finalCol = col;
                int finalNum = num;
                Platform.runLater(() -> {
                    SudokuTextField tile = textFieldCoordinates.get(new Coordinates(finalRow, finalCol));
                    tile.setText(String.valueOf(finalNum));
                    tile.setStyle("-fx-border-color: green; -fx-border-width: 4;");
                });
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (solveStepByStep(board, row, col + 1)) return true;
                board[row][col] = 0;
                Platform.runLater(() -> {
                    SudokuTextField tile = textFieldCoordinates.get(new Coordinates(finalRow, finalCol));
                    tile.setText("");
                    tile.setStyle("-fx-border-color: red; -fx-border-width: 4;");
                });
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private boolean isValidMove(int[][] board, int row, int col, int num) {
        for (int i = 0; i < 9; i++) {
            if (board[row][i] == num || board[i][col] == num)
                return false;
        }
        int startRow = (row / 3) * 3;
        int startCol = (col / 3) * 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[startRow + i][startCol + j] == num)
                    return false;
            }
        }
        return true;
    }

    private void drawTextFields(Group root) {
        final int xOrigin = 50;
        final int yOrigin = 50;
        final int xAndYDelta = 64;
        for (int xIndex = 0; xIndex < 9; xIndex++) {
            for (int yIndex = 0; yIndex < 9; yIndex++) {
                int x = xOrigin + xIndex * xAndYDelta;
                int y = yOrigin + yIndex * xAndYDelta;
                SudokuTextField tile = new SudokuTextField(xIndex, yIndex);
                styleSudokuTile(tile, x, y);
                tile.setOnKeyPressed(this);
                tile.setOnMouseClicked(event -> {
                    SudokuTextField previousTile = selectedTile;
                    selectedTile = tile;
                    if (previousTile != null && previousTile != tile) {
                        clearHighlights(previousTile);
                    }
                    highlightSelection(tile);
                });
                textFieldCoordinates.put(new Coordinates(xIndex, yIndex), tile);
                root.getChildren().add(tile);
            }
        }
    }

    private void styleSudokuTile(SudokuTextField tile, double x, double y) {
        Font numberFont = new Font(40);
        tile.setFont(numberFont);
        tile.setAlignment(Pos.CENTER);
        tile.setLayoutX(x);
        tile.setLayoutY(y);
        tile.setPrefHeight(64);
        tile.setPrefWidth(64);
        tile.setBackground(Background.EMPTY);
        tile.setPadding(Insets.EMPTY);
    }

    private void highlightSelection(SudokuTextField selected) {
        int selectedX = selected.getX();
        int selectedY = selected.getY();
        selected.setStyle("-fx-border-color: black;" + "-fx-border-width: 6;" + "-fx-padding: -6 -6 -6 -6;");
        for (Coordinates coord : textFieldCoordinates.keySet()) {
            SudokuTextField tile = textFieldCoordinates.get(coord);
            int x = coord.getX();
            int y = coord.getY();
            boolean sameRowOrCol = (x == selectedX || y == selectedY);
            if (sameRowOrCol && tile != selected) {
                if (tile.isDisable()) {
                    tile.setBackground(new Background(new BackgroundFill(Color.rgb(204, 203, 159), CornerRadii.EMPTY, Insets.EMPTY)));
                } else {
                    tile.setBackground(new Background(new BackgroundFill(Color.rgb(255, 255, 155), CornerRadii.EMPTY, Insets.EMPTY)));
                }
            }
        }
        int subgridX = (selectedX / 3) * 3;
        int subgridY = (selectedY / 3) * 3;
        for (int dx = 0; dx < 3; dx++) {
            for (int dy = 0; dy < 3; dy++) {
                int x = subgridX + dx;
                int y = subgridY + dy;
                SudokuTextField tile = textFieldCoordinates.get(new Coordinates(x, y));
                if (tile.isDisable()) {
                    tile.setBackground(new Background(new BackgroundFill(Color.rgb(204, 203, 159), CornerRadii.EMPTY, Insets.EMPTY)));
                } else {
                    tile.setBackground(new Background(new BackgroundFill(Color.rgb(255, 255, 155), CornerRadii.EMPTY, Insets.EMPTY)));
                }
            }
        }
    }

    private void highlightSameNumbers(int number) {
        if (number == 0) return;
        String value = String.valueOf(number);
        for (SudokuTextField tile : textFieldCoordinates.values()) {
            if (value.equals(tile.getText())) {
                if (tile.isDisable()) {
                    tile.setBackground(new Background(new BackgroundFill(Color.rgb(176, 225, 255), CornerRadii.EMPTY, Insets.EMPTY)));
                } else {
                    tile.setBackground(new Background(new BackgroundFill(Color.rgb(176, 225, 255), CornerRadii.EMPTY, Insets.EMPTY)));
                }
            }
        }
    }

    private void clearHighlights(SudokuTextField currentSelectedTile) {
        for (SudokuTextField tile : textFieldCoordinates.values()) {
            if (tile.isDisable()) {
                tile.setBackground(new Background(new BackgroundFill(Color.rgb(204, 204, 204), CornerRadii.EMPTY, Insets.EMPTY)));
            } else {
                tile.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
            }
        }
        currentSelectedTile.setStyle("-fx-border-color: transparent;");
    }

    private void drawGridLines(Group root) {
        int xAndY = 114;
        int index = 0;
        while (index < 8) {
            int thickness;
            if (index == 2 || index == 5) {
                thickness = 3;
            } else {
                thickness = 2;
            }
            Rectangle verticalLine = getLine(
                    xAndY + 64 * index,
                    BOARD_PADDING,
                    BOARD_X_AND_Y,
                    thickness
            );
            Rectangle horizontalLine = getLine(
                    BOARD_PADDING,
                    xAndY + 64 * index,
                    thickness,
                    BOARD_X_AND_Y
            );
            root.getChildren().addAll(
                    verticalLine,
                    horizontalLine
            );
            index++;
        }
    }

    public Rectangle getLine(double x, double y, double height, double width){
        Rectangle line = new Rectangle();
        line.setX(x);
        line.setY(y);
        line.setHeight(height);
        line.setWidth(width);
        line.setFill(Color.BLACK);
        return line;
    }

    private void drawBackground(Group root) {
        Scene scene = new Scene(root, WINDOW_X, WINDOW_Y);
        scene.setFill(WINDOW_BACKGROUND_COLOR);
        stage.setScene(scene);
    }

    private void drawSudokuBoard(Group root) {
        Rectangle boardBackground = new Rectangle();
        boardBackground.setX(BOARD_PADDING);
        boardBackground.setY(BOARD_PADDING);
        boardBackground.setWidth(BOARD_X_AND_Y);
        boardBackground.setHeight(BOARD_X_AND_Y);
        boardBackground.setFill(BOARD_BACKGROUND_COLOR);
        root.getChildren().add(boardBackground);
    }

    private void drawTitle(Group root) {
        Text title = new Text(470, 740, SUDOKU);
        title.setFill(Color.WHITE);
        Font titleFont = new Font(43);
        title.setFont(titleFont);
        title.setStyle("-fx-font-weight: bold");
        root.getChildren().add(title);
    }

    @Override
    public void updateSquare(int x, int y, int input) {
        SudokuTextField tile = textFieldCoordinates.get(new Coordinates(x, y));
        String value = Integer.toString(input);
        if (value.equals("0")) value = "";
        tile.textProperty().setValue(value);
    }

    @Override
    public void updateBoard(SudokuGame game) {
        for (int xIndex = 0; xIndex < 9; xIndex++) {
            for (int yIndex = 0; yIndex < 9; yIndex++) {
                TextField tile = textFieldCoordinates.get(new Coordinates(xIndex, yIndex));
                String value = Integer.toString(game.getCopyOfGridState()[xIndex][yIndex]);
                if (value.equals("0")) value = "";
                tile.setText(value);
                if (value.equals("")) {
                    // System.out.println("draw empty tile");
                    tile.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, new Insets(0,0,0,0))));
                    tile.setStyle("-fx-opacity: 1.0;");
                    tile.setDisable(false);
                } else {
                    // System.out.println("draw blocked tile");
                    tile.setBackground(new Background(new BackgroundFill(Color.rgb(204, 204, 204), CornerRadii.EMPTY, new Insets(0,0,0,0))));
                    tile.setStyle("-fx-opacity: 1.0;");
                    tile.setDisable(true);
                }
            }
        }
    }

    @Override
    public void showDialog(String message) {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK);
        dialog.showAndWait();
        if (dialog.getResult() == ButtonType.OK) listener.onDialogClick();
    }

    @Override
    public void showError(String message) {
        Alert dialog = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        dialog.showAndWait();
    }

    @Override
    public void handle(KeyEvent event) {
        if (event.getEventType() == KeyEvent.KEY_PRESSED) {
            String keyText = event.getText();
            for (int i = 0; i <= 9; i++) {
                if (keyText.equals(String.valueOf(i))) {
                    int value = Integer.parseInt(keyText);
                    handleInput(value, event.getSource());
                    event.consume();
                    return;
                }
            }
            if (event.getCode() == KeyCode.BACK_SPACE) {
                handleInput(0, event.getSource());
            } else {
                ((TextField) event.getSource()).setText("");
            }
        }
        event.consume();
    }

    private void handleInput(int value, Object source) {
        SudokuTextField tile = (SudokuTextField) source;
        int x = tile.getX();
        int y = tile.getY();
        if (value == 0) {
            listener.onSudokuInput(x, y, 0);
            tile.setText("");
            return;
        }
        if (isConflict(x, y, value)) {
            flashRedBorder(tile);
        } else {
            listener.onSudokuInput(x, y, value);
            tile.setText(String.valueOf(value));
            clearHighlights(tile);
            highlightSelection(tile);
            highlightSameNumbers(value);
        }
    }

    private boolean isConflict(int x, int y, int value) {
        String valueStr = String.valueOf(value);
        for (int i = 0; i < 9; i++) {
            if (i != x) {
                SudokuTextField cell = textFieldCoordinates.get(new Coordinates(i, y));
                if (cell.getText().equals(valueStr)) return true;
            }
            if (i != y) {
                SudokuTextField cell = textFieldCoordinates.get(new Coordinates(x, i));
                if (cell.getText().equals(valueStr)) return true;
            }
        }
        int subgridX = (x / 3) * 3;
        int subgridY = (y / 3) * 3;
        for (int dx = 0; dx < 3; dx++) {
            for (int dy = 0; dy < 3; dy++) {
                int checkX = subgridX + dx;
                int checkY = subgridY + dy;
                if (checkX != x || checkY != y) {
                    SudokuTextField cell = textFieldCoordinates.get(new Coordinates(checkX, checkY));
                    if (cell.getText().equals(valueStr)) return true;
                }
            }
        }
        return false;
    }

    private void flashRedBorder(SudokuTextField tile) {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline();
        int flashes = 6;
        for (int i = 0; i < flashes; i++) {
            final boolean isRed = i % 2 == 0;
            javafx.animation.KeyFrame keyFrame = new javafx.animation.KeyFrame(
                    javafx.util.Duration.seconds(i * 0.3),
                    e -> tile.setStyle("-fx-border-color: " + (isRed ? "red" : "black") + "; -fx-border-width: 6;" + "; -fx-padding: -6 -6 -6 -6;")
            );
            timeline.getKeyFrames().add(keyFrame);
        }
        timeline.play();
    }

    private void startTimer() {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        javafx.util.Duration.seconds(1),
                        event -> {
                            elapsedSeconds++;
                            int minutes = elapsedSeconds / 60;
                            int seconds = elapsedSeconds % 60;
                            String timeFormatted = String.format("%02d:%02d", minutes, seconds);
                            timerText.setText("Timp: " + timeFormatted);
                        }
                )
        );
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
    }
}