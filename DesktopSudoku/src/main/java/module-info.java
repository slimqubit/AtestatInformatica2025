module com.example.desktopsudoku {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.desktopsudoku to javafx.fxml;
    exports com.example.desktopsudoku;
    exports com.example.desktopsudoku.buildlogic;
    opens com.example.desktopsudoku.buildlogic to javafx.fxml;
}