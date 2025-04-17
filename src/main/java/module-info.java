module com.example.tutorialtest {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.tutorialtest to javafx.fxml;
    exports com.example.tutorialtest;
}