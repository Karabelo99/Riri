package com.example.tutorialtest;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDate;
import javafx.geometry.Insets;
import javafx.event.ActionEvent;
import java.sql.*;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Reflection;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.collections.ListChangeListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.chart.*;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.io.InputStream;
import javafx.scene.image.Image;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.event.EventHandler;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import java.time.format.DateTimeFormatter;

enum PaymentMethod {
    CASH, CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER
}

public class HelloController extends Application {

    // UI Components
    private TextField usernameField = new TextField();
    private PasswordField passwordField = new PasswordField();
    private ComboBox<String> roleComboBox = new ComboBox<>();
    private Label messageLabel = new Label();
    private ComboBox<Customer> customerComboBox = new ComboBox<>();

    // Data storage
    private ObservableList<Vehicle> vehicles = FXCollections.observableArrayList();
    private ObservableList<Customer> customers = FXCollections.observableArrayList();
    private ObservableList<Booking> bookings = FXCollections.observableArrayList();
    private ObservableList<PaymentSlip> paymentSlips = FXCollections.observableArrayList();

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/vehicle_rental";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "123456";

    public static void main(String[] args) {
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.verbose", "true");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            showTestWindow(primaryStage);
            Platform.runLater(() -> {
                initializeSampleData();
                showLoginScreen(primaryStage);
            });
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Application failed to start", e.getMessage());
        }
    }

    private Background createBackground() {
        try {
            InputStream imageStream = getClass().getResourceAsStream("/background.jpeg");

            if (imageStream != null) {
                Image backgroundImage = new Image(imageStream);
                BackgroundImage bgImage = new BackgroundImage(
                        backgroundImage,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
                );
                return new Background(bgImage);
            } else {
                System.out.println("Background image not found. Using gradient background.");
                return createDefaultBackground();
            }
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
            return createDefaultBackground();
        }
    }

    private Background createDefaultBackground() {
        return new Background(new BackgroundFill(
                new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#3498db")),
                        new Stop(1, Color.web("#2c3e50"))),
                CornerRadii.EMPTY, Insets.EMPTY));
    }

    private void showTestWindow(Stage stage) {
        VBox testRoot = new VBox(10);
        testRoot.setPadding(new Insets(15));
        testRoot.setBackground(createBackground());
        testRoot.setAlignment(Pos.CENTER);

        Label testLabel = new Label("Initializing application...");
        testLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        testLabel.setTextFill(Color.BLACK);

        ProgressIndicator progress = new ProgressIndicator();
        progress.setStyle("-fx-progress-color: BLACK;");
        testRoot.getChildren().addAll(testLabel, progress);

        Scene testScene = new Scene(testRoot, 300, 200);
        stage.setScene(testScene);
        stage.setTitle("Loading");
        stage.centerOnScreen();
        stage.show();
    }

    private void initializeSampleData() {
        try {
            loadVehicles();
            loadCustomers();
            loadBookings();
            loadPaymentSlips();

            if (vehicles.isEmpty()) {
                initializeSampleVehicles();
            }
            if (customers.isEmpty()) {
                initializeSampleCustomers();
            }
        } catch (Exception e) {
            showErrorAlert("Initialization Error", "Failed to load data: " + e.getMessage());
        }
    }

    private void loadPaymentSlips() {
        String sql = "SELECT * FROM payment_slips";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            paymentSlips.clear();

            while (rs.next()) {
                PaymentSlip slip = new PaymentSlip(
                        rs.getString("slip_id"),
                        rs.getString("booking_id"),
                        rs.getString("slip_content"),
                        rs.getDate("generated_date").toLocalDate()
                );
                paymentSlips.add(slip);
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load payment slips: " + e.getMessage());
        }
    }

    private void initializeSampleVehicles() {
        if (!vehicleExistsInDatabase("V001")) {
            addVehicle("V001", "Toyota", "Corolla", "Car", 50.0, true);
        }
        if (!vehicleExistsInDatabase("V002")) {
            addVehicle("V002", "Honda", "Civic", "Car", 55.0, true);
        }
        if (!vehicleExistsInDatabase("V003")) {
            addVehicle("V003", "Ford", "Transit", "Van", 80.0, true);
        }
        if (!vehicleExistsInDatabase("V004")) {
            addVehicle("V004", "Harley-Davidson", "Sportster", "Bike", 60.0, true);
        }
        if (!vehicleExistsInDatabase("V005")) {
            addVehicle("V005", "Volvo", "FH16", "Truck", 120.0, true);
        }
    }

    private void initializeSampleCustomers() {
        if (!customerExistsInDatabase("C001")) {
            addCustomer("C001", "John Smith", "john.smith@example.com", "555-0101", "DL123456");
        }
        if (!customerExistsInDatabase("C002")) {
            addCustomer("C002", "Sarah Johnson", "sarah.j@example.com", "555-0102", "DL654321");
        }
        if (!customerExistsInDatabase("C003")) {
            addCustomer("C003", "Michael Brown", "michael.b@example.com", "555-0103", "DL789012");
        }
    }

    private boolean vehicleExistsInDatabase(String id) {
        String sql = "SELECT id FROM vehicles WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean customerExistsInDatabase(String id) {
        String sql = "SELECT id FROM customers WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private void loadVehicles() {
        String sql = "SELECT * FROM vehicles";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            vehicles.clear();

            while (rs.next()) {
                Vehicle vehicle = new Vehicle(
                        rs.getString("id"),
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getString("category"),
                        rs.getDouble("rental_price"),
                        rs.getBoolean("available")
                );
                vehicles.add(vehicle);
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load vehicles: " + e.getMessage());
        }
    }

    private void loadCustomers() {
        String sql = "SELECT * FROM customers";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            customers.clear();

            while (rs.next()) {
                Customer customer = new Customer(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("license_number")
                );
                customers.add(customer);
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load customers: " + e.getMessage());
        }
    }

    private void loadBookings() {
        String sql = "SELECT * FROM bookings";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            bookings.clear();

            while (rs.next()) {
                String vehicleId = rs.getString("vehicle_id");
                String customerId = rs.getString("customer_id");

                Vehicle vehicle = findVehicleById(vehicleId);
                Customer customer = findCustomerById(customerId);

                if (vehicle != null && customer != null) {
                    Booking booking = new Booking(
                            rs.getString("booking_id"),
                            vehicle,
                            customer,
                            rs.getString("start_date"),
                            rs.getString("end_date"),
                            rs.getDouble("total_price"),
                            rs.getString("status"),
                            PaymentMethod.valueOf(rs.getString("payment_method"))
                    );
                    booking.setLateFee(rs.getDouble("late_fee"));  // Correct column name
                    booking.setDamageFee(rs.getDouble("damage_fee")); // Correct column name
                    booking.setNotes(rs.getString("notes"));
                    bookings.add(booking);

                    if (booking.getStatus().equals("active")) {
                        vehicle.setAvailable(false);
                    }
                }
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load bookings: " + e.getMessage());
        }
    }

    private Vehicle findVehicleById(String id) {
        return vehicles.stream()
                .filter(v -> v.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private Customer findCustomerById(String id) {
        return customers.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
    private Label createStyledLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.BLACK);
        label.setStyle("-fx-font-weight: bold;");
        return label;
    }

    private void showRegistrationDialog(Stage ownerStage) {
        // Create the custom dialog
        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle("New Customer Registration");
        dialog.setHeaderText("Please enter your details");

        // Set the button types
        ButtonType registerButtonType = new ButtonType("Register", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);

        // Create the registration form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone");
        TextField licenseField = new TextField();
        licenseField.setPromptText("Driver's License Number");

        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("License No:"), 0, 3);
        grid.add(licenseField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the name field by default
        Platform.runLater(nameField::requestFocus);

        // Convert the result to a customer when register button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registerButtonType) {
                if (nameField.getText().isEmpty() || emailField.getText().isEmpty() ||
                        phoneField.getText().isEmpty() || licenseField.getText().isEmpty()) {
                    showAlert("Error", "Please fill all fields!");
                    return null;
                }

                String newId = "C" + String.format("%03d", customers.size() + 1);
                return new Customer(newId, nameField.getText(), emailField.getText(),
                        phoneField.getText(), licenseField.getText());
            }
            return null;
        });

        Optional<Customer> result = dialog.showAndWait();

        result.ifPresent(newCustomer -> {
            // Add to database and in-memory list
            addCustomer(newCustomer.getId(), newCustomer.getName(), newCustomer.getEmail(),
                    newCustomer.getPhone(), newCustomer.getLicenseNumber());

            showAlert("Success", "Registration successful! Your ID is: " + newCustomer.getId());
        });
    }


    private void showLoginScreen(Stage stage) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setBackground(createBackground());

        VBox loginContainer = new VBox(20);
        loginContainer.setPadding(new Insets(30));
        loginContainer.setAlignment(Pos.CENTER);
        loginContainer.setStyle("-fx-background-color: rgba(255, 255, 255, 0.85); " +
                "-fx-background-radius: 15; " +
                "-fx-border-radius: 15; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");
        loginContainer.setMaxWidth(400);

        Label titleLabel = new Label("Vehicle Rental System");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.DARKBLUE);

        // Add reflection to title
        Reflection titleReflection = new Reflection();
        titleReflection.setFraction(0.3);
        titleReflection.setTopOffset(5);
        titleReflection.setTopOpacity(0.4);
        titleReflection.setBottomOpacity(0.1);
        titleLabel.setEffect(titleReflection);

        GridPane grid = new GridPane();
        grid.setVgap(15);
        grid.setHgap(15);
        grid.setPadding(new Insets(20));

        // Add customer ID field
        TextField customerIdField = new TextField();
        customerIdField.setPromptText("Customer ID");
        styleTextField(customerIdField);
        customerIdField.setVisible(false);

        usernameField.setPromptText("Username");
        passwordField.setPromptText("Password");
        styleTextField(usernameField);
        styleTextField(passwordField);

        roleComboBox.setItems(FXCollections.observableArrayList("Admin", "Employee", "Customer"));
        roleComboBox.setPromptText("Select Role");
        roleComboBox.setStyle("-fx-background-color: white; -fx-border-color: #3498db; -fx-border-radius: 5;");

        Button loginButton = new Button("Login");
        styleButton(loginButton, "#2ecc71");
        loginButton.setOnAction(e -> handleLogin(stage, customerIdField));

        // Add register button
        Button registerButton = new Button("Register New Customer");
        styleButton(registerButton, "#3498db");
        registerButton.setOnAction(e -> showRegistrationDialog(stage));
        registerButton.setVisible(false); // Initially hidden

        // Create labels with black color and bold font
        Label usernameLabel = createStyledLabel("Username:");
        Label passwordLabel = createStyledLabel("Password:");
        Label roleLabel = createStyledLabel("Role:");
        Label customerIdLabel = createStyledLabel("Customer ID:");

        grid.add(usernameLabel, 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(passwordLabel, 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(roleLabel, 0, 2);
        grid.add(roleComboBox, 1, 2);
        grid.add(customerIdLabel, 0, 3);
        grid.add(customerIdField, 1, 3);
        grid.add(loginButton, 1, 4);
        grid.add(registerButton, 1, 5); // Add register button to grid

        messageLabel.setTextFill(Color.BLACK);
        messageLabel.setStyle("-fx-font-weight: bold;");

        loginContainer.getChildren().addAll(titleLabel, grid, messageLabel);
        root.getChildren().add(loginContainer);
        usernameField.setPromptText("Username");
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-Z]*")) {
                usernameField.setText(oldValue);
            }
        });
        styleTextField(usernameField);

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Login");
        stage.centerOnScreen();
        stage.show();

        roleComboBox.setOnAction(event -> {
            if ("Customer".equals(roleComboBox.getValue())) {
                customerIdField.setVisible(true);
                usernameField.setVisible(false);
                passwordField.setVisible(false);
                registerButton.setVisible(true);  // Show register button for customers
            } else {
                customerIdField.setVisible(false);
                usernameField.setVisible(true);
                passwordField.setVisible(true);
                registerButton.setVisible(false);  // Hide register button for others
            }
        });
    }
    private void handleLogin(Stage stage, TextField customerIdField) {
        String role = roleComboBox.getValue();

        if (role == null) {
            messageLabel.setText("Please select a role!");
            return;
        }

        if (role.equals("Customer")) {
            String customerId = customerIdField.getText().trim();
            if (customerId.isEmpty()) {
                messageLabel.setText("Please enter your customer ID!");
                return;
            }

            Customer customer = findCustomerById(customerId);
            if (customer == null) {
                messageLabel.setText("Invalid customer ID!");
                return;
            }

            showCustomerDashboard(stage, customer);
            return;
        }

        // Existing admin/employee login logic
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Username and password are required!");
            return;
        }

        if (role.equals("Admin")) {
            showAdminDashboard(stage);
        } else if (role.equals("Employee")) {
            showEmployeeDashboard(stage);
        }
    }
    private void showCustomerDashboard(Stage stage, Customer customer) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setBackground(createBackground());

        Label welcomeLabel = new Label("Welcome, " + customer.getName());
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        welcomeLabel.setTextFill(Color.DARKBLUE);

        // Add reflection to welcome label
        Reflection welcomeReflection = new Reflection();
        welcomeReflection.setFraction(0.25);
        welcomeLabel.setEffect(welcomeReflection);

        // Booking Form
        Label bookingLabel = new Label("New Booking");
        bookingLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        bookingLabel.setTextFill(Color.BLACK);

        ComboBox<Vehicle> vehicleCombo = new ComboBox<>();
        vehicleCombo.setItems(FXCollections.observableArrayList(getAvailableVehicles()));
        vehicleCombo.setPromptText("Select Vehicle");
        styleComboBox(vehicleCombo);

        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();
        ComboBox<PaymentMethod> paymentCombo = new ComboBox<>(FXCollections.observableArrayList(PaymentMethod.values()));
        paymentCombo.setPromptText("Select Payment Method");
        styleComboBox(paymentCombo);

        Button bookButton = new Button("Make Booking");
        styleButton(bookButton, "#2ecc71");
        bookButton.setOnAction(e -> {
            Vehicle selectedVehicle = vehicleCombo.getValue();
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            PaymentMethod paymentMethod = paymentCombo.getValue();

            if (selectedVehicle == null || startDate == null || endDate == null || paymentMethod == null) {
                showAlert("Error", "Please fill all fields!");
                return;
            }

            if (endDate.isBefore(startDate)) {
                showAlert("Error", "End date must be after start date!");
                return;
            }

            String bookingId = "B" + System.currentTimeMillis();
            createBooking(bookingId, selectedVehicle, customer,
                    startDate.toString(), endDate.toString(), paymentMethod);
            showAlert("Success", "Booking submitted for processing!");

            // Refresh the available vehicles
            vehicleCombo.setItems(FXCollections.observableArrayList(getAvailableVehicles()));
        });

        // Create styled labels for form
        Label vehicleLabel = createStyledLabel("Vehicle:");
        Label startDateLabel = createStyledLabel("Start Date:");
        Label endDateLabel = createStyledLabel("End Date:");
        Label paymentLabel = createStyledLabel("Payment Method:");

        // Customer's Bookings Table
        TableView<Booking> bookingsTable = new TableView<>();
        bookingsTable.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7);");

        TableColumn<Booking, String> bookingIdCol = new TableColumn<>("Booking ID");
        bookingIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookingId()));

        TableColumn<Booking, String> vehicleCol = new TableColumn<>("Vehicle");
        vehicleCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getVehicle().getBrand() + " " +
                        cellData.getValue().getVehicle().getModel()));

        TableColumn<Booking, String> periodCol = new TableColumn<>("Rental Period");
        periodCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getStartDate() + " to " +
                        cellData.getValue().getEndDate()));

        TableColumn<Booking, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));

        TableColumn<Booking, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                String.format("$%.2f", cellData.getValue().getTotalAmountDue())));

        bookingsTable.getColumns().addAll(bookingIdCol, vehicleCol, periodCol, statusCol, amountCol);
        bookingsTable.setItems(bookings.filtered(b -> b.getCustomer().getId().equals(customer.getId())));

        // Payment Slips Section
        Label slipsLabel = new Label("Your Payment Slips");
        slipsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        slipsLabel.setTextFill(Color.BLACK);

        TableView<PaymentSlip> slipsTable = new TableView<>();
        slipsTable.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7);");

        TableColumn<PaymentSlip, String> slipIdCol = new TableColumn<>("Slip ID");
        slipIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSlipId()));

        TableColumn<PaymentSlip, String> bookingIdCol2 = new TableColumn<>("Booking ID");
        bookingIdCol2.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookingId()));

        TableColumn<PaymentSlip, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getGeneratedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));

        TableColumn<PaymentSlip, Void> viewCol = new TableColumn<>("Action");
        viewCol.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("View");

            {
                viewButton.setOnAction(event -> {
                    PaymentSlip slip = getTableView().getItems().get(getIndex());
                    showPaymentSlip(slip);
                });
                styleButton(viewButton, "#3498db");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                }
            }
        });

        slipsTable.getColumns().addAll(slipIdCol, bookingIdCol2, dateCol, viewCol);
        slipsTable.setItems(paymentSlips.filtered(slip ->
                bookings.stream().anyMatch(b -> b.getBookingId().equals(slip.getBookingId()) &&
                        b.getCustomer().getId().equals(customer.getId()))));

        Button logoutButton = new Button("Logout");
        styleButton(logoutButton, "#e74c3c");
        logoutButton.setOnAction(e -> showLoginScreen(stage));

        VBox contentBox = new VBox(20,
                welcomeLabel,
                new Separator(),
                bookingLabel,
                new HBox(10, vehicleLabel, vehicleCombo),
                new HBox(10, startDateLabel, startDatePicker),
                new HBox(10, endDateLabel, endDatePicker),
                new HBox(10, paymentLabel, paymentCombo),
                bookButton,
                new Separator(),
                createStyledLabel("Your Bookings:"),
                bookingsTable,
                new Separator(),
                slipsLabel,
                slipsTable,
                logoutButton
        );
        contentBox.setPadding(new Insets(20));
        contentBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7); " +
                "-fx-background-radius: 15; " +
                "-fx-padding: 20;");

        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        root.getChildren().add(scrollPane);
        Scene scene = new Scene(root, 1000, 700);
        stage.setScene(scene);
        stage.setTitle("Customer Dashboard");
        stage.show();
    }

    private void showPaymentSlip(PaymentSlip slip) {
        TextArea slipArea = new TextArea(slip.getSlipContent());
        slipArea.setEditable(false);
        slipArea.setStyle("-fx-font-family: monospace;");

        Stage slipStage = new Stage();
        slipStage.setScene(new Scene(new StackPane(slipArea), 500, 500));
        slipStage.setTitle("Payment Slip - " + slip.getSlipId());
        slipStage.show();
    }

    private void showAdminDashboard(Stage stage) {
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: transparent;");

        Tab vehicleTab = new Tab("Vehicle Management");
        vehicleTab.setContent(createVehicleManagementPane());
        vehicleTab.setClosable(false);

        Tab customerTab = new Tab("Customer Management");
        customerTab.setContent(createCustomerManagementPane());
        customerTab.setClosable(false);

        Tab bookingTab = new Tab("Booking Management");
        bookingTab.setContent(createAdminBookingManagementPane());
        bookingTab.setClosable(false);

        Tab paymentTab = new Tab("Payment Processing");
        paymentTab.setContent(createAdminPaymentProcessingPane());
        paymentTab.setClosable(false);

        Tab reportsTab = new Tab("Reports");
        reportsTab.setContent(createReportsPane());
        reportsTab.setClosable(false);

        tabPane.getTabs().addAll(vehicleTab, customerTab, bookingTab, paymentTab, reportsTab);

        Button logoutButton = new Button("Logout");
        styleButton(logoutButton, "#e74c3c");
        logoutButton.setOnAction(e -> showLoginScreen(stage));

        BorderPane root = new BorderPane();
        root.setBackground(createBackground());
        root.setCenter(tabPane);

        HBox bottomBox = new HBox(logoutButton);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(10));
        bottomBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7); -fx-background-radius: 10;");
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.setTitle("Admin Dashboard");
        stage.show();
    }

    private ScrollPane createAdminBookingManagementPane() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));
        vbox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); -fx-background-radius: 10;");

        // All Bookings Table
        TableView<Booking> bookingsTable = createBookingsTable("all");

        Button exportButton = new Button("Export to CSV");
        styleButton(exportButton, "#3498db");
        exportButton.setOnAction(e -> exportBookingsToCSV());

        vbox.getChildren().addAll(
                createStyledLabel("All Bookings:"),
                bookingsTable,
                exportButton
        );

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scrollPane;
    }

    private ScrollPane createAdminPaymentProcessingPane() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));
        vbox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); -fx-background-radius: 10;");

        // Confirmed Bookings Table
        Label confirmedLabel = new Label("Confirmed Bookings (Ready for Processing)");
        confirmedLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        confirmedLabel.setTextFill(Color.BLACK);

        TableView<Booking> confirmedTable = new TableView<>();
        confirmedTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        confirmedTable.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7);");
        confirmedTable.setPrefHeight(300);

        // Columns for confirmed bookings
        TableColumn<Booking, String> bookingIdCol = new TableColumn<>("Booking ID");
        bookingIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookingId()));

        TableColumn<Booking, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomer().getName()));

        TableColumn<Booking, String> vehicleCol = new TableColumn<>("Vehicle");
        vehicleCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getVehicle().getBrand() + " " + cellData.getValue().getVehicle().getModel()));

        TableColumn<Booking, String> basePriceCol = new TableColumn<>("Base Price");
        basePriceCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                String.format("$%.2f", cellData.getValue().getTotalPrice())));

        confirmedTable.getColumns().addAll(bookingIdCol, customerCol, vehicleCol, basePriceCol);
        confirmedTable.setItems(bookings.filtered(b -> b.getStatus().equalsIgnoreCase("confirmed")));

        // Payment Processing Form
        Label processingLabel = new Label("Payment Processing");
        processingLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        processingLabel.setTextFill(Color.BLACK);

        GridPane paymentGrid = new GridPane();
        paymentGrid.setVgap(10);
        paymentGrid.setHgap(10);
        paymentGrid.setPadding(new Insets(15));

        // Display base price (read-only)
        TextField basePriceField = new TextField();
        basePriceField.setEditable(false);
        styleTextField(basePriceField);

        TextField lateFeeField = new TextField();
        lateFeeField.setPromptText("0.00");
        styleTextField(lateFeeField);

        TextField damageFeeField = new TextField();
        damageFeeField.setPromptText("0.00");
        styleTextField(damageFeeField);

        TextField totalPriceField = new TextField();
        totalPriceField.setEditable(false);
        styleTextField(totalPriceField);

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Enter any notes about damages or late return");
        notesArea.setPrefRowCount(3);

        // Update total price when fees change
        EventHandler<ActionEvent> feeUpdateHandler = e -> {
            try {
                double basePrice = confirmedTable.getSelectionModel().getSelectedItem() != null ?
                        confirmedTable.getSelectionModel().getSelectedItem().getTotalPrice() : 0;
                double lateFee = lateFeeField.getText().isEmpty() ? 0 : Double.parseDouble(lateFeeField.getText());
                double damageFee = damageFeeField.getText().isEmpty() ? 0 : Double.parseDouble(damageFeeField.getText());
                totalPriceField.setText(String.format("$%.2f", basePrice + lateFee + damageFee));
            } catch (NumberFormatException ex) {
                totalPriceField.setText("Invalid input");
            }
        };

        lateFeeField.setOnAction(feeUpdateHandler);
        damageFeeField.setOnAction(feeUpdateHandler);

        Button processPaymentBtn = new Button("Process Payment");
        styleButton(processPaymentBtn, "#9b59b6");

        Button generateSlipBtn = new Button("Generate Payment Slip");
        styleButton(generateSlipBtn, "#3498db");

        // Update form when booking is selected
        confirmedTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                basePriceField.setText(String.format("$%.2f", newVal.getTotalPrice()));
                lateFeeField.setText(String.format("%.2f", newVal.getLateFee()));
                damageFeeField.setText(String.format("%.2f", newVal.getDamageFee()));
                totalPriceField.setText(String.format("$%.2f",
                        newVal.getTotalPrice() + newVal.getLateFee() + newVal.getDamageFee()));
                notesArea.setText(newVal.getNotes());
            }
        });

        processPaymentBtn.setOnAction(e -> {
            Booking selected = confirmedTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    double lateFee = lateFeeField.getText().isEmpty() ? 0 : Double.parseDouble(lateFeeField.getText());
                    double damageFee = damageFeeField.getText().isEmpty() ? 0 : Double.parseDouble(damageFeeField.getText());
                    String notes = notesArea.getText();

                    // Update booking with additional charges
                    updateBookingCharges(selected.getBookingId(), lateFee, damageFee, notes);
                    updateBookingStatus(selected.getBookingId(), "completed"); // Mark as completed
                    showAlert("Success", "Payment processed and booking completed!");

                    // Refresh the tables
                    loadBookings();
                } catch (NumberFormatException ex) {
                    showAlert("Error", "Invalid fee amounts!");
                }
            }
        });

        generateSlipBtn.setOnAction(e -> {
            Booking selected = confirmedTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                generatePaymentSlip(selected);
            }
        });

        // Add components to grid with styled labels
        paymentGrid.add(createStyledLabel("Base Price:"), 0, 0);
        paymentGrid.add(basePriceField, 1, 0);
        paymentGrid.add(createStyledLabel("Late Fee:"), 0, 1);
        paymentGrid.add(lateFeeField, 1, 1);
        paymentGrid.add(createStyledLabel("Damage Fee:"), 0, 2);
        paymentGrid.add(damageFeeField, 1, 2);
        paymentGrid.add(createStyledLabel("Total Amount:"), 0, 3);
        paymentGrid.add(totalPriceField, 1, 3);
        paymentGrid.add(createStyledLabel("Notes:"), 0, 4);
        paymentGrid.add(notesArea, 1, 4);

        HBox buttonBox = new HBox(10, processPaymentBtn, generateSlipBtn);
        paymentGrid.add(buttonBox, 1, 5);

        vbox.getChildren().addAll(
                confirmedLabel,
                confirmedTable,
                new Separator(),
                processingLabel,
                paymentGrid
        );

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scrollPane;
    }

    private void generatePaymentSlip(Booking booking) {
        // Format dates nicely
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

        String slipContent = String.format(
                "=== PAYMENT SLIP ===\n\n" +
                        "Booking ID: %s\n" +
                        "Customer: %s\n" +
                        "Vehicle: %s %s\n" +
                        "Rental Period: %s to %s\n\n" +
                        "--- Charges ---\n" +
                        "Base Rental Fee: $%.2f\n" +
                        "Late Fees: $%.2f\n" +
                        "Damage Fees: $%.2f\n" +
                        "-----------------\n" +
                        "Total Amount Due: $%.2f\n\n" +
                        "Payment Method: %s\n" +
                        "Status: %s\n" +
                        "Processed Date: %s\n\n" +
                        "Notes: %s",
                booking.getBookingId(),
                booking.getCustomer().getName(),
                booking.getVehicle().getBrand(),
                booking.getVehicle().getModel(),
                LocalDate.parse(booking.getStartDate()).format(dateFormatter),
                LocalDate.parse(booking.getEndDate()).format(dateFormatter),
                booking.getTotalPrice(),
                booking.getLateFee(),
                booking.getDamageFee(),
                booking.getTotalAmountDue(),
                booking.getPaymentMethod().toString(),
                booking.getStatus(),
                LocalDate.now().format(dateFormatter),
                booking.getNotes() == null ? "None" : booking.getNotes()
        );

        // Create and show the slip dialog
        TextArea slipArea = new TextArea(slipContent);
        slipArea.setEditable(false);
        slipArea.setStyle("-fx-font-family: monospace; -fx-font-size: 14px;");

        ButtonType printButton = new ButtonType("Print", ButtonData.OK_DONE);
        ButtonType saveButton = new ButtonType("Save", ButtonData.OK_DONE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Payment Slip");
        alert.setHeaderText("Payment Slip for Booking " + booking.getBookingId());
        alert.getDialogPane().setContent(slipArea);
        alert.getButtonTypes().setAll(printButton, saveButton, ButtonType.CANCEL);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == saveButton) {
                savePaymentSlipToDatabase(booking.getBookingId(), slipContent);
                showAlert("Success", "Payment slip saved to database!");
            } else if (result.get() == printButton) {
                printSlip(slipContent);
            }
        }
    }

    private void savePaymentSlipToDatabase(String bookingId, String slipContent) {
        String sql = "INSERT INTO payment_slips (slip_id, booking_id, slip_content, generated_date) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "SLIP-" + System.currentTimeMillis());
            pstmt.setString(2, bookingId);
            pstmt.setString(3, slipContent);
            pstmt.setDate(4, Date.valueOf(LocalDate.now()));

            pstmt.executeUpdate();
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to save payment slip: " + e.getMessage());
        }
    }

    private void printSlip(String slipContent) {
        try {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(null)) {
                Text printText = new Text(slipContent);
                printText.setFont(Font.font("Monospaced", 12));

                StackPane root = new StackPane(printText);
                root.setPadding(new Insets(20));

                boolean success = job.printPage(root);
                if (success) {
                    job.endJob();
                    showAlert("Success", "Slip sent to printer!");
                }
            }
        } catch (Exception e) {
            showErrorAlert("Print Error", "Failed to print slip: " + e.getMessage());
        }
    }

    private void updateBookingCharges(String bookingId, double lateFee, double damageFee, String notes) {
        String sql = "UPDATE bookings SET late_fee = ?, damage_fee = ?, notes = ? WHERE booking_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, lateFee);
            pstmt.setDouble(2, damageFee);
            pstmt.setString(3, notes);
            pstmt.setString(4, bookingId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to update booking charges: " + e.getMessage());
        }
    }

    private ScrollPane createReportsPane() {
        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(15));
        vbox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); -fx-background-radius: 10;");

        // Revenue Summary Section
        Label revenueTitle = new Label("Revenue Summary");
        revenueTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        revenueTitle.setTextFill(Color.BLACK);

        // Calculate total revenue
        double totalRevenue = bookings.stream()
                .filter(b -> b.getStatus().equals("active") || b.getStatus().equals("completed"))
                .mapToDouble(Booking::getTotalAmountDue)
                .sum();

        // Create a styled label for total revenue
        Label revenueLabel = new Label(String.format("Total Revenue: $%.2f", totalRevenue));
        revenueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        revenueLabel.setTextFill(Color.BLACK);

        // Revenue by category chart with black text labels
        Map<String, Double> revenueByCategory = bookings.stream()
                .filter(b -> b.getStatus().equals("active") || b.getStatus().equals("completed"))
                .collect(Collectors.groupingBy(
                        b -> b.getVehicle().getCategory(),
                        Collectors.summingDouble(Booking::getTotalAmountDue)
                ));

        // Create the chart with black text styling
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setTickLabelFill(Color.BLACK);  // Black category labels
        xAxis.setTickLabelFont(Font.font("Arial", FontWeight.BOLD, 12));

        NumberAxis yAxis = new NumberAxis();
        yAxis.setTickLabelFill(Color.BLACK);  // Black value labels
        yAxis.setTickLabelFont(Font.font("Arial", FontWeight.NORMAL, 12));
        yAxis.setLabel("Revenue ($)");
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis, "$", null));

        BarChart<String, Number> categoryChart = new BarChart<>(xAxis, yAxis);
        categoryChart.setTitle("Revenue by Vehicle Category");
        Node chartTitle = categoryChart.lookup(".chart-title");
        if (chartTitle != null) {
            chartTitle.setStyle("-fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 14px;");
        }

        // Style the chart legend to use black text
        categoryChart.setLegendVisible(true);
        categoryChart.lookup(".chart-legend").setStyle("-fx-text-fill: black;");
        categoryChart.lookup(".chart-title").setStyle("-fx-text-fill: black;");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue by Category");
        revenueByCategory.forEach((category, revenue) ->
                series.getData().add(new XYChart.Data<>(category, revenue)));
        categoryChart.getData().add(series);
        categoryChart.setPrefHeight(300);

        // Style the bars and labels
        for (XYChart.Data<String, Number> data : series.getData()) {
            Node node = data.getNode();
            node.setStyle("-fx-bar-fill: #3498db;");  // Blue bars

            // Add value labels on top of bars
            data.nodeProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    Text text = new Text(String.format("$%.2f", data.getYValue()));
                    text.setFill(Color.BLACK);  // Black text for values
                    text.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                    newValue.setOnMouseEntered(event -> {
                        text.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    });
                    newValue.setOnMouseExited(event -> {
                        text.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                    });
                    StackPane.setAlignment(text, Pos.TOP_CENTER);
                    ((StackPane) newValue).getChildren().add(text);
                }
            });
        }

        // Payment Methods Pie Chart with black text
        Label paymentTitle = new Label("Payment Methods Distribution");
        paymentTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        paymentTitle.setTextFill(Color.BLACK);

        Map<PaymentMethod, Long> paymentCounts = bookings.stream()
                .filter(b -> b.getStatus().equals("active") || b.getStatus().equals("completed"))
                .collect(Collectors.groupingBy(
                        Booking::getPaymentMethod,
                        Collectors.counting()
                ));

        PieChart paymentChart = new PieChart();
        paymentChart.setTitle("Payment Methods Used");
        Node pieChartTitle = paymentChart.lookup(".chart-title");
        if (pieChartTitle != null) {
            pieChartTitle.setStyle("-fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 14px;");
        }
        // Style pie chart labels
        paymentChart.setLabelLineLength(10);
        paymentCounts.forEach((method, count) -> {
            PieChart.Data slice = new PieChart.Data(
                    method.toString() + " (" + count + ")",
                    count
            );
            paymentChart.getData().add(slice);
        });

        // Set black text for pie chart labels
        paymentChart.setLabelsVisible(true);
        paymentChart.lookupAll(".chart-pie-label").forEach(node -> {
            if (node instanceof Text) {
                ((Text) node).setFill(Color.BLACK);
                ((Text) node).setFont(Font.font("Arial", FontWeight.BOLD, 12));
            }
        });

        paymentChart.setPrefHeight(300);

        // All Customers Table
        Label customersTitle = new Label("All Customers");
        customersTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        customersTitle.setTextFill(Color.BLACK);

        TableView<Customer> customersTable = new TableView<>();
        customersTable.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7);");

        TableColumn<Customer, String> custIdCol = new TableColumn<>("Customer ID");
        custIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));

        TableColumn<Customer, String> custNameCol = new TableColumn<>("Name");
        custNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

        TableColumn<Customer, String> custEmailCol = new TableColumn<>("Email");
        custEmailCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));

        TableColumn<Customer, String> custPhoneCol = new TableColumn<>("Phone");
        custPhoneCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPhone()));

        TableColumn<Customer, String> custLicenseCol = new TableColumn<>("License");
        custLicenseCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLicenseNumber()));

        customersTable.getColumns().addAll(custIdCol, custNameCol, custEmailCol, custPhoneCol, custLicenseCol);
        customersTable.setItems(customers);
        customersTable.setPrefHeight(200);

        // Vehicle Status Table
        Label vehiclesTitle = new Label("Vehicle Status");
        vehiclesTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        vehiclesTitle.setTextFill(Color.BLACK);

        TableView<Vehicle> vehiclesTable = new TableView<>();
        vehiclesTable.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7);");

        TableColumn<Vehicle, String> vehicleIdCol = new TableColumn<>("ID");
        vehicleIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));

        TableColumn<Vehicle, String> vehicleBrandCol = new TableColumn<>("Brand");
        vehicleBrandCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBrand()));

        TableColumn<Vehicle, String> vehicleModelCol = new TableColumn<>("Model");
        vehicleModelCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getModel()));

        TableColumn<Vehicle, String> vehicleCategoryCol = new TableColumn<>("Category");
        vehicleCategoryCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory()));

        TableColumn<Vehicle, String> vehiclePriceCol = new TableColumn<>("Daily Price");
        vehiclePriceCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                String.format("$%.2f", cellData.getValue().getRentalPrice())));

        TableColumn<Vehicle, String> vehicleStatusCol = new TableColumn<>("Status");
        vehicleStatusCol.setCellValueFactory(cellData -> {
            String status = cellData.getValue().isAvailable() ? "Available" : "Booked";
            return new SimpleStringProperty(status);
        });

        vehiclesTable.getColumns().addAll(vehicleIdCol, vehicleBrandCol, vehicleModelCol,
                vehicleCategoryCol, vehiclePriceCol, vehicleStatusCol);
        vehiclesTable.setItems(vehicles);
        vehiclesTable.setPrefHeight(200);

        // Add components to VBox
        vbox.getChildren().addAll(
                revenueTitle, revenueLabel, categoryChart,
                paymentTitle, paymentChart,
                customersTitle, customersTable,
                vehiclesTitle, vehiclesTable
        );

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scrollPane;
    }
    private void showEmployeeDashboard(Stage stage) {
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: transparent;");

        Tab bookingTab = new Tab("Booking Management");
        bookingTab.setContent(createEmployeeBookingManagementPane());
        bookingTab.setClosable(false);

        Tab vehiclesTab = new Tab("Available Vehicles");
        vehiclesTab.setContent(createAvailableVehiclesPane());
        vehiclesTab.setClosable(false);

        Tab historyTab = new Tab("Rental History");
        historyTab.setContent(createRentalHistoryPane());
        historyTab.setClosable(false);

        tabPane.getTabs().addAll(bookingTab, vehiclesTab, historyTab);

        Button logoutButton = new Button("Logout");
        styleButton(logoutButton, "#e74c3c");
        logoutButton.setOnAction(e -> showLoginScreen(stage));

        BorderPane root = new BorderPane();
        root.setBackground(createBackground());
        root.setCenter(tabPane);

        HBox bottomBox = new HBox(logoutButton);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(10));
        bottomBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7); -fx-background-radius: 10;");
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 1000, 700);
        stage.setScene(scene);
        stage.setTitle("Employee Dashboard");
        stage.show();
    }

    private ScrollPane createEmployeeBookingManagementPane() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));
        vbox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); -fx-background-radius: 10;");

        // Pending Bookings Table
        Label pendingLabel = new Label("Pending Bookings");
        pendingLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        pendingLabel.setTextFill(Color.BLACK);
        TableView<Booking> pendingTable = createBookingsTable("pending");

        // Action Buttons
        HBox actionBox = new HBox(10);
        Button confirmBtn = new Button("Confirm");
        Button cancelBtn = new Button("Cancel");
        styleButton(confirmBtn, "#2ecc71");
        styleButton(cancelBtn, "#e74c3c");

        confirmBtn.setOnAction(e -> {
            Booking selected = pendingTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                updateBookingStatus(selected.getBookingId(), "confirmed");
                showAlert("Success", "Booking confirmed!");
                loadBookings(); // Refresh data
            }
        });

        cancelBtn.setOnAction(e -> {
            Booking selected = pendingTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                updateBookingStatus(selected.getBookingId(), "cancelled");
                showAlert("Success", "Booking cancelled!");
                loadBookings();
                pendingTable.refresh();
            }
        });

        actionBox.getChildren().addAll(confirmBtn, cancelBtn);

        // Confirmed Bookings Table
        Label confirmedLabel = new Label("Confirmed Bookings");
        confirmedLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        confirmedLabel.setTextFill(Color.BLACK);
        TableView<Booking> confirmedTable = createBookingsTable("confirmed");

        vbox.getChildren().addAll(
                pendingLabel, pendingTable, actionBox,
                new Separator(), confirmedLabel, confirmedTable
        );

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scrollPane;
    }

    private TableView<Booking> createBookingsTable(String filter) {
        TableView<Booking> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7);");
        TableColumn<Booking, String> lateFeeCol = new TableColumn<>("Late Fee");
        lateFeeCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                String.format("$%.2f", cellData.getValue().getLateFee())));

        TableColumn<Booking, String> damageFeeCol = new TableColumn<>("Damage Fee");
        damageFeeCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                String.format("$%.2f", cellData.getValue().getDamageFee())));

        TableColumn<Booking, String> bookingIdCol = new TableColumn<>("Booking ID");
        bookingIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookingId()));

        TableColumn<Booking, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomer().getName()));

        TableColumn<Booking, String> vehicleCol = new TableColumn<>("Vehicle");
        vehicleCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getVehicle().getBrand() + " " + cellData.getValue().getVehicle().getModel()));

        TableColumn<Booking, String> startDateCol = new TableColumn<>("Start Date");
        startDateCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStartDate()));

        TableColumn<Booking, String> endDateCol = new TableColumn<>("End Date");
        endDateCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEndDate()));

        TableColumn<Booking, String> paymentCol = new TableColumn<>("Payment Method");
        paymentCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPaymentMethod().toString()));

        TableColumn<Booking, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));

        TableColumn<Booking, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                String.format("$%.2f", cellData.getValue().getTotalAmountDue())));

        table.getColumns().addAll(bookingIdCol, customerCol, vehicleCol, startDateCol, endDateCol, paymentCol, statusCol, amountCol);

        switch (filter.toLowerCase()) {
            case "pending":
                table.setItems(bookings.filtered(b -> b.getStatus().equalsIgnoreCase("pending")));
                break;
            case "confirmed":
                table.setItems(bookings.filtered(b -> b.getStatus().equalsIgnoreCase("confirmed")));
                break;
            case "active":
                table.setItems(bookings.filtered(b -> b.getStatus().equalsIgnoreCase("active")));
                break;
            default:
                table.setItems(bookings);
        }

        return table;
    }

    private ScrollPane createRentalHistoryPane() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));
        vbox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); -fx-background-radius: 10;");

        TableView<Booking> historyTable = new TableView<>();
        historyTable.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7);");

        TableColumn<Booking, String> bookingIdCol = new TableColumn<>("Booking ID");
        bookingIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookingId()));

        TableColumn<Booking, String> vehicleCol = new TableColumn<>("Vehicle");
        vehicleCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getVehicle().getBrand() + " " +
                        cellData.getValue().getVehicle().getModel()));

        TableColumn<Booking, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomer().getName()));

        TableColumn<Booking, String> periodCol = new TableColumn<>("Rental Period");
        periodCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getStartDate() + " to " +
                        cellData.getValue().getEndDate()));

        TableColumn<Booking, String> paymentCol = new TableColumn<>("Payment Method");
        paymentCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPaymentMethod().toString()));

        TableColumn<Booking, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));

        historyTable.getColumns().addAll(bookingIdCol, vehicleCol, customerCol, periodCol, paymentCol, statusCol);
        historyTable.setItems(bookings.filtered(b ->
                b.getStatus().equalsIgnoreCase("completed") ||
                        b.getStatus().equalsIgnoreCase("cancelled")));

        Label historyLabel = new Label("Rental History");
        historyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        historyLabel.setTextFill(Color.BLACK);

        vbox.getChildren().addAll(historyLabel, historyTable);

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scrollPane;
    }

    private ScrollPane createVehicleManagementPane() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));
        vbox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); -fx-background-radius: 10;");

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(15));

        TextField idField = new TextField();
        TextField brandField = new TextField();
        TextField modelField = new TextField();
        ComboBox<String> categoryCombo = new ComboBox<>(
                FXCollections.observableArrayList("Car", "Bike", "Van", "Truck")
        );
        TextField priceField = new TextField();
        CheckBox availableCheck = new CheckBox("Available");

        styleTextField(idField);
        styleTextField(brandField);
        styleTextField(modelField);
        styleTextField(priceField);
        categoryCombo.setStyle("-fx-background-color: white; -fx-border-color: #3498db; -fx-border-radius: 5;");

        Button addButton = new Button("Add Vehicle");
        styleButton(addButton, "#2ecc71");
        addButton.setOnAction(e -> {
            try {
                if (idField.getText().isEmpty() || brandField.getText().isEmpty() ||
                        modelField.getText().isEmpty() || categoryCombo.getValue() == null ||
                        priceField.getText().isEmpty()) {
                    showAlert("Error", "Please fill all fields!");
                    return;
                }

                addVehicle(
                        idField.getText(),
                        brandField.getText(),
                        modelField.getText(),
                        categoryCombo.getValue(),
                        Double.parseDouble(priceField.getText()),
                        availableCheck.isSelected()
                );
                clearFields(idField, brandField, modelField, priceField);
                categoryCombo.getSelectionModel().clearSelection();
                availableCheck.setSelected(false);
            } catch (NumberFormatException ex) {
                showAlert("Error", "Please enter a valid price!");
            }
        });

        Button updateButton = new Button("Update Price");
        styleButton(updateButton, "#3498db");
        updateButton.setOnAction(e -> {
            try {
                if (idField.getText().isEmpty() || priceField.getText().isEmpty()) {
                    showAlert("Error", "Please enter vehicle ID and new price!");
                    return;
                }

                updateVehiclePrice(
                        idField.getText(),
                        Double.parseDouble(priceField.getText())
                );
                showAlert("Success", "Price updated successfully!");
            } catch (NumberFormatException ex) {
                showAlert("Error", "Please enter a valid price!");
            }
        });

        Button toggleStatusButton = new Button("Toggle Availability");
        styleButton(toggleStatusButton, "#f39c12");
        toggleStatusButton.setOnAction(e -> {
            if (idField.getText().isEmpty()) {
                showAlert("Error", "Please select a vehicle first!");
                return;
            }

            Vehicle vehicle = findVehicleById(idField.getText());
            if (vehicle != null) {
                boolean isBooked = bookings.stream()
                        .anyMatch(b -> b.getVehicle().getId().equals(vehicle.getId())
                                && b.getStatus().equals("active"));

                if (isBooked && vehicle.isAvailable()) {
                    showAlert("Error", "Cannot make vehicle available - it has active bookings!");
                    return;
                }

                toggleVehicleAvailability(vehicle.getId(), !vehicle.isAvailable());
                availableCheck.setSelected(!vehicle.isAvailable());
            }
        });

        Button refreshButton = new Button("Refresh");
        styleButton(refreshButton, "#3498db");
        refreshButton.setOnAction(e -> loadVehicles());

        // Add styled labels
        grid.add(createStyledLabel("Vehicle ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(createStyledLabel("Brand:"), 0, 1);
        grid.add(brandField, 1, 1);
        grid.add(createStyledLabel("Model:"), 0, 2);
        grid.add(modelField, 1, 2);
        grid.add(createStyledLabel("Category:"), 0, 3);
        grid.add(categoryCombo, 1, 3);
        grid.add(createStyledLabel("Daily Price:"), 0, 4);
        grid.add(priceField, 1, 4);
        grid.add(availableCheck, 1, 5);

        HBox buttonBox = new HBox(10, addButton, updateButton, toggleStatusButton, refreshButton);
        grid.add(buttonBox, 1, 6);

        TableView<Vehicle> vehicleTable = new TableView<>();
        vehicleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        vehicleTable.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7);");

        TableColumn<Vehicle, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));

        TableColumn<Vehicle, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBrand()));

        TableColumn<Vehicle, String> modelCol = new TableColumn<>("Model");
        modelCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getModel()));

        TableColumn<Vehicle, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory()));

        TableColumn<Vehicle, String> priceCol = new TableColumn<>("Daily Price");
        priceCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                String.format("$%.2f", cellData.getValue().getRentalPrice())));

        TableColumn<Vehicle, String> availableCol = new TableColumn<>("Status");
        availableCol.setCellValueFactory(cellData -> {
            String status = cellData.getValue().isAvailable() ? "Available" : "Booked";
            return new SimpleStringProperty(status);
        });

        vehicleTable.getColumns().addAll(idCol, brandCol, modelCol, categoryCol, priceCol, availableCol);
        vehicleTable.setItems(vehicles);

        vehicleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                idField.setText(newVal.getId());
                brandField.setText(newVal.getBrand());
                modelField.setText(newVal.getModel());
                categoryCombo.setValue(newVal.getCategory());
                priceField.setText(String.valueOf(newVal.getRentalPrice()));
                availableCheck.setSelected(newVal.isAvailable());
            }
        });

        vehicles.addListener((ListChangeListener.Change<? extends Vehicle> c) -> {
            while (c.next()) {
                if (c.wasAdded() || c.wasRemoved() || c.wasUpdated()) {
                    Platform.runLater(() -> vehicleTable.refresh());
                }
            }
        });

        vbox.getChildren().addAll(grid, createStyledLabel("All Vehicles:"), vehicleTable);

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scrollPane;
    }

    private ScrollPane createCustomerManagementPane() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));
        vbox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); -fx-background-radius: 10;");

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(15));

        TextField idField = new TextField();
        TextField nameField = new TextField();
        TextField emailField = new TextField();
        TextField phoneField = new TextField();
        TextField licenseField = new TextField();

        styleTextField(idField);
        styleTextField(nameField);
        styleTextField(emailField);
        styleTextField(phoneField);
        styleTextField(licenseField);

        Button addButton = new Button("Add Customer");
        styleButton(addButton, "#2ecc71");
        addButton.setOnAction(e -> {
            try {
                if (idField.getText().isEmpty() || nameField.getText().isEmpty() ||
                        emailField.getText().isEmpty() || phoneField.getText().isEmpty() ||
                        licenseField.getText().isEmpty()) {
                    showAlert("Error", "Please fill all fields!");
                    return;
                }

                addCustomer(
                        idField.getText(),
                        nameField.getText(),
                        emailField.getText(),
                        phoneField.getText(),
                        licenseField.getText()
                );
                showAlert("Success", "Customer added successfully!");
                clearFields(idField, nameField, emailField, phoneField, licenseField);
            } catch (Exception ex) {
                showAlert("Error", "Invalid input!");
            }
        });

        Button editButton = new Button("Edit Customer");
        styleButton(editButton, "#3498db");
        editButton.setOnAction(e -> {
            try {
                if (idField.getText().isEmpty()) {
                    showAlert("Error", "Please select a customer to edit!");
                    return;
                }

                updateCustomer(
                        idField.getText(),
                        nameField.getText(),
                        emailField.getText(),
                        phoneField.getText(),
                        licenseField.getText()
                );
                showAlert("Success", "Customer updated successfully!");
            } catch (Exception ex) {
                showAlert("Error", "Invalid input!");
            }
        });

        Button deleteButton = new Button("Delete Customer");
        styleButton(deleteButton, "#e74c3c");
        deleteButton.setOnAction(e -> {
            try {
                if (idField.getText().isEmpty()) {
                    showAlert("Error", "Please enter customer ID!");
                    return;
                }

                boolean hasActiveBookings = bookings.stream()
                        .anyMatch(b -> b.getCustomer().getId().equals(idField.getText())
                                && b.getStatus().equals("active"));

                if (hasActiveBookings) {
                    showAlert("Error", "Cannot delete customer with active bookings!");
                    return;
                }

                deleteCustomer(idField.getText());
                showAlert("Success", "Customer deleted successfully!");
                clearFields(idField, nameField, emailField, phoneField, licenseField);
            } catch (Exception ex) {
                showAlert("Error", "Invalid input!");
            }
        });

        Button refreshButton = new Button("Refresh");
        styleButton(refreshButton, "#3498db");
        refreshButton.setOnAction(e -> loadCustomers());

        // Add styled labels
        grid.add(createStyledLabel("Customer ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(createStyledLabel("Full Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(createStyledLabel("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(createStyledLabel("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(createStyledLabel("License No:"), 0, 4);
        grid.add(licenseField, 1, 4);

        HBox buttonBox = new HBox(10, addButton, editButton, deleteButton, refreshButton);
        grid.add(buttonBox, 1, 5);

        TableView<Customer> customerTable = new TableView<>();
        customerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        customerTable.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7);");

        TableColumn<Customer, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));

        TableColumn<Customer, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

        TableColumn<Customer, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));

        TableColumn<Customer, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPhone()));

        TableColumn<Customer, String> licenseCol = new TableColumn<>("License No");
        licenseCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLicenseNumber()));

        customerTable.getColumns().addAll(idCol, nameCol, emailCol, phoneCol, licenseCol);
        customerTable.setItems(customers);

        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                idField.setText(newVal.getId());
                nameField.setText(newVal.getName());
                emailField.setText(newVal.getEmail());
                phoneField.setText(newVal.getPhone());
                licenseField.setText(newVal.getLicenseNumber());
            }
        });

        customers.addListener((ListChangeListener.Change<? extends Customer> c) -> {
            while (c.next()) {
                if (c.wasAdded() || c.wasRemoved() || c.wasUpdated()) {
                    Platform.runLater(() -> customerTable.refresh());
                }
            }
        });

        vbox.getChildren().addAll(grid, createStyledLabel("All Customers:"), customerTable);

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scrollPane;
    }

    private ScrollPane createAvailableVehiclesPane() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));
        vbox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); -fx-background-radius: 10;");

        TableView<Vehicle> vehicleTable = new TableView<>();
        vehicleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        vehicleTable.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7);");

        TableColumn<Vehicle, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));

        TableColumn<Vehicle, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBrand()));

        TableColumn<Vehicle, String> modelCol = new TableColumn<>("Model");
        modelCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getModel()));

        TableColumn<Vehicle, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory()));

        TableColumn<Vehicle, String> priceCol = new TableColumn<>("Daily Price");
        priceCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                String.format("$%.2f", cellData.getValue().getRentalPrice())));

        vehicleTable.getColumns().addAll(idCol, brandCol, modelCol, categoryCol, priceCol);
        vehicleTable.setItems(FXCollections.observableArrayList(getAvailableVehicles()));

        vehicles.addListener((ListChangeListener.Change<? extends Vehicle> c) -> {
            vehicleTable.setItems(FXCollections.observableArrayList(getAvailableVehicles()));
        });

        bookings.addListener((ListChangeListener.Change<? extends Booking> c) -> {
            vehicleTable.setItems(FXCollections.observableArrayList(getAvailableVehicles()));
        });

        Label availableLabel = new Label("Available Vehicles");
        availableLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        availableLabel.setTextFill(Color.BLACK);

        vbox.getChildren().addAll(availableLabel, vehicleTable);

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scrollPane;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields(TextField... fields) {
        for (TextField field : fields) {
            field.clear();
        }
    }

    private void styleTextField(TextField textField) {
        textField.setStyle("-fx-background-color: white; " +
                "-fx-border-color: #3498db; " +
                "-fx-border-radius: 5; " +
                "-fx-padding: 5;");
    }

    private void styleComboBox(ComboBox<?> comboBox) {
        comboBox.setStyle("-fx-background-color: white; " +
                "-fx-border-color: #3498db; " +
                "-fx-border-radius: 5; " +
                "-fx-padding: 5;");
    }

    private void styleButton(Button button, String color) {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(8);
        dropShadow.setOffsetX(3);
        dropShadow.setOffsetY(3);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.5));

        button.setStyle("-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 5; " +
                "-fx-padding: 8 15 8 15;");
        button.setEffect(dropShadow);

        // Add hover effect
        button.setOnMouseEntered(e -> {
            dropShadow.setColor(Color.rgb(0, 0, 0, 0.7));
            dropShadow.setRadius(10);
        });
        button.setOnMouseExited(e -> {
            dropShadow.setColor(Color.rgb(0, 0, 0, 0.5));
            dropShadow.setRadius(8);
        });
    }

    public void addVehicle(String id, String brand, String model, String category, double price, boolean available) {
        if (findVehicleById(id) != null) {
            showAlert("Error", "Vehicle with ID " + id + " already exists!");
            return;
        }

        String sql = "INSERT INTO vehicles (id, brand, model, category, rental_price, available) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (vehicleExistsInDatabase(id)) {
                showAlert("Error", "Vehicle with ID " + id + " already exists in database!");
                return;
            }

            pstmt.setString(1, id);
            pstmt.setString(2, brand);
            pstmt.setString(3, model);
            pstmt.setString(4, category);
            pstmt.setDouble(5, price);
            pstmt.setBoolean(6, available);
            pstmt.executeUpdate();

            vehicles.add(new Vehicle(id, brand, model, category, price, available));
            showAlert("Success", "Vehicle added successfully!");
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to add vehicle: " + e.getMessage());
        }
    }

    public void updateVehiclePrice(String id, double newPrice) {
        String sql = "UPDATE vehicles SET rental_price = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newPrice);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
            vehicles.stream()
                    .filter(v -> v.getId().equals(id))
                    .forEach(v -> v.setRentalPrice(newPrice));
            showAlert("Success", "Vehicle price updated successfully!");
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to update vehicle price: " + e.getMessage());
        }
    }

    public void toggleVehicleAvailability(String id, boolean available) {
        boolean hasActiveBookings = bookings.stream()
                .anyMatch(b -> b.getVehicle().getId().equals(id)
                        && b.getStatus().equals("active"));

        if (hasActiveBookings && available) {
            showAlert("Error", "Cannot make vehicle available - it has active bookings!");
            return;
        }

        String sql = "UPDATE vehicles SET available = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, available);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
            vehicles.stream()
                    .filter(v -> v.getId().equals(id))
                    .forEach(v -> v.setAvailable(available));
            showAlert("Success", "Vehicle availability updated successfully!");
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to update vehicle availability: " + e.getMessage());
        }
    }

    public void addCustomer(String id, String name, String email, String phone, String license) {
        String sql = "INSERT INTO customers (id, name, email, phone, license_number) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, email);
            pstmt.setString(4, phone);
            pstmt.setString(5, license);
            pstmt.executeUpdate();

            customers.add(new Customer(id, name, email, phone, license));
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to register customer: " + e.getMessage());
        }
    }

    public void updateCustomer(String id, String name, String email, String phone, String license) {
        String sql = "UPDATE customers SET name = ?, email = ?, phone = ?, license_number = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.setString(4, license);
            pstmt.setString(5, id);
            pstmt.executeUpdate();

            customers.stream()
                    .filter(c -> c.getId().equals(id))
                    .findFirst()
                    .ifPresent(c -> {
                        c.setName(name);
                        c.setEmail(email);
                        c.setPhone(phone);
                        c.setLicenseNumber(license);
                    });
            showAlert("Success", "Customer updated successfully!");
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to update customer: " + e.getMessage());
        }
    }

    public void deleteCustomer(String id) {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            customers.removeIf(c -> c.getId().equals(id));
            showAlert("Success", "Customer deleted successfully!");
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to delete customer: " + e.getMessage());
        }
    }

    public void createBooking(String bookingId, Vehicle vehicle, Customer customer,
                              String startDate, String endDate, PaymentMethod paymentMethod) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(false);

            String checkSql = "SELECT available FROM vehicles WHERE id = ? FOR UPDATE";
            boolean isAvailable = false;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, vehicle.getId());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    isAvailable = rs.getBoolean("available");
                }
            }

            if (!isAvailable) {
                showAlert("Error", "Vehicle is not available for booking!");
                conn.rollback();
                return;
            }

            long days = LocalDate.parse(endDate).toEpochDay() - LocalDate.parse(startDate).toEpochDay();
            double totalPrice = days * vehicle.getRentalPrice();

            String bookingSql = "INSERT INTO bookings (booking_id, vehicle_id, customer_id, start_date, end_date, " +
                    "total_price, status, payment_method) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement bookingStmt = conn.prepareStatement(bookingSql)) {
                bookingStmt.setString(1, bookingId);
                bookingStmt.setString(2, vehicle.getId());
                bookingStmt.setString(3, customer.getId());
                bookingStmt.setString(4, startDate);
                bookingStmt.setString(5, endDate);
                bookingStmt.setDouble(6, totalPrice);
                bookingStmt.setString(7, "pending");
                bookingStmt.setString(8, paymentMethod.name());
                bookingStmt.executeUpdate();
            }

            String vehicleSql = "UPDATE vehicles SET available = false WHERE id = ?";
            try (PreparedStatement vehicleStmt = conn.prepareStatement(vehicleSql)) {
                vehicleStmt.setString(1, vehicle.getId());
                vehicleStmt.executeUpdate();
            }

            conn.commit();

            Booking booking = new Booking(bookingId, vehicle, customer, startDate, endDate,
                    totalPrice, "pending", paymentMethod);
            bookings.add(booking);
            vehicle.setAvailable(false);

            showAlert("Success", "Booking submitted for processing!");
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            showErrorAlert("Database Error", "Failed to create booking: " + e.getMessage());
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateBookingStatus(String bookingId, String newStatus) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(false);

            // Update booking status
            String bookingSql = "UPDATE bookings SET status = ? WHERE booking_id = ?";
            try (PreparedStatement bookingStmt = conn.prepareStatement(bookingSql)) {
                bookingStmt.setString(1, newStatus);
                bookingStmt.setString(2, bookingId);
                bookingStmt.executeUpdate();
            }

            // If cancelling, make vehicle available again
            if ("cancelled".equalsIgnoreCase(newStatus)) {
                String getVehicleSql = "SELECT vehicle_id FROM bookings WHERE booking_id = ?";
                String vehicleId = null;
                try (PreparedStatement getStmt = conn.prepareStatement(getVehicleSql)) {
                    getStmt.setString(1, bookingId);
                    ResultSet rs = getStmt.executeQuery();
                    if (rs.next()) {
                        vehicleId = rs.getString("vehicle_id");
                    }
                }

                if (vehicleId != null) {
                    String vehicleSql = "UPDATE vehicles SET available = true WHERE id = ?";
                    try (PreparedStatement vehicleStmt = conn.prepareStatement(vehicleSql)) {
                        vehicleStmt.setString(1, vehicleId);
                        vehicleStmt.executeUpdate();
                    }
                }
            }

            conn.commit();

            // Update in-memory data
            bookings.stream()
                    .filter(b -> b.getBookingId().equals(bookingId))
                    .findFirst()
                    .ifPresent(b -> {
                        b.setStatus(newStatus);
                        if ("cancelled".equalsIgnoreCase(newStatus)) {
                            b.getVehicle().setAvailable(true);
                        }
                    });

            showAlert("Success", "Booking status updated to " + newStatus + "!");
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            showErrorAlert("Database Error", "Failed to update booking status: " + e.getMessage());
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void exportBookingsToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Bookings CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                // Write CSV header
                writer.println("Booking ID,Customer,Vehicle,Start Date,End Date,Status,Total Price,Late Fee,Damage Fee,Payment Method");

                // Write each booking
                for (Booking booking : bookings) {
                    writer.println(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%.2f,%.2f,%.2f,\"%s\"",
                            booking.getBookingId(),
                            booking.getCustomer().getName(),
                            booking.getVehicle().getBrand() + " " + booking.getVehicle().getModel(),
                            booking.getStartDate(),
                            booking.getEndDate(),
                            booking.getStatus(),
                            booking.getTotalPrice(),
                            booking.getLateFee(),
                            booking.getDamageFee(),
                            booking.getPaymentMethod()
                    ));
                }

                showAlert("Success", "Bookings exported to CSV successfully!");
            } catch (FileNotFoundException e) {
                showErrorAlert("Error", "Failed to export bookings: " + e.getMessage());
            }
        }
    }

    public ObservableList<Vehicle> getAvailableVehicles() {
        ObservableList<Vehicle> availableVehicles = FXCollections.observableArrayList();
        for (Vehicle vehicle : vehicles) {
            if (vehicle.isAvailable()) {
                availableVehicles.add(vehicle);
            }
        }
        return availableVehicles;
    }

    class Vehicle {
        private String id;
        private String brand;
        private String model;
        private String category;
        private double rentalPrice;
        private boolean available;

        public Vehicle(String id, String brand, String model, String category, double rentalPrice, boolean available) {
            this.id = id;
            this.brand = brand;
            this.model = model;
            this.category = category;
            this.rentalPrice = rentalPrice;
            this.available = available;
        }

        public String getId() { return id; }
        public String getBrand() { return brand; }
        public String getModel() { return model; }
        public String getCategory() { return category; }
        public double getRentalPrice() { return rentalPrice; }
        public boolean isAvailable() { return available; }
        public void setRentalPrice(double price) { this.rentalPrice = price; }
        public void setAvailable(boolean available) { this.available = available; }

        @Override
        public String toString() {
            return String.format("%s %s (%s) - %s",
                    brand, model, category,
                    available ? "Available" : "Booked");
        }
    }

    class Customer {
        private String id;
        private String name;
        private String email;
        private String phone;
        private String licenseNumber;

        public Customer(String id, String name, String email, String phone, String licenseNumber) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.licenseNumber = licenseNumber;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getLicenseNumber() { return licenseNumber; }
        public void setName(String name) { this.name = name; }
        public void setEmail(String email) { this.email = email; }
        public void setPhone(String phone) { this.phone = phone; }
        public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

        @Override
        public String toString() {
            return String.format("%s (%s) - %s", name, id, email);
        }
    }

    class Booking {
        private String bookingId;
        private Vehicle vehicle;
        private Customer customer;
        private String startDate;
        private String endDate;
        private double totalPrice;
        private String status;
        private PaymentMethod paymentMethod;
        private double lateFee;
        private double damageFee;
        private String notes;

        public Booking(String bookingId, Vehicle vehicle, Customer customer,
                       String startDate, String endDate, double totalPrice,
                       String status, PaymentMethod paymentMethod) {
            this.bookingId = bookingId;
            this.vehicle = vehicle;
            this.customer = customer;
            this.startDate = startDate;
            this.endDate = endDate;
            this.totalPrice = totalPrice;
            this.status = status;
            this.paymentMethod = paymentMethod;
            this.lateFee = 0;
            this.damageFee = 0;
        }

        public String getBookingId() { return bookingId; }
        public Vehicle getVehicle() { return vehicle; }
        public Customer getCustomer() { return customer; }
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
        public double getTotalPrice() { return totalPrice; }
        public String getStatus() { return status; }
        public PaymentMethod getPaymentMethod() { return paymentMethod; }
        public double getLateFee() { return lateFee; }
        public double getDamageFee() { return damageFee; }
        public String getNotes() { return notes; }
        public void setStatus(String status) { this.status = status; }
        public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
        public void setLateFee(double lateFee) { this.lateFee = lateFee; }
        public void setDamageFee(double damageFee) { this.damageFee = damageFee; }
        public void setNotes(String notes) { this.notes = notes; }

        public double getTotalAmountDue() {
            return totalPrice + lateFee + damageFee;
        }

        @Override
        public String toString() {
            return String.format("Booking %s: %s for %s (%s to %s) - $%.2f - %s - Paid by: %s",
                    bookingId, vehicle.getBrand(), customer.getName(),
                    startDate, endDate, getTotalAmountDue(), status, paymentMethod);
        }
    }

    class PaymentSlip {
        private String slipId;
        private String bookingId;
        private String slipContent;
        private LocalDate generatedDate;

        public PaymentSlip(String slipId, String bookingId, String slipContent, LocalDate generatedDate) {
            this.slipId = slipId;
            this.bookingId = bookingId;
            this.slipContent = slipContent;
            this.generatedDate = generatedDate;
        }

        public String getSlipId() { return slipId; }
        public String getBookingId() { return bookingId; }
        public String getSlipContent() { return slipContent; }
        public LocalDate getGeneratedDate() { return generatedDate; }
    }
}