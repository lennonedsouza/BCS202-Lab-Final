import java.util.Scanner;
import java.util.ArrayList;
import java.net.URL;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.text.*;
import javafx.geometry.*;
import javafx.scene.paint.Color;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.SVGPath;

//Exceptions
class ProductNotFoundException extends Exception {
    public ProductNotFoundException(String message) {
        super(message);
    }
}

class InvalidInputException extends Exception {
    public InvalidInputException(String message) {
        super(message);
    }
}

class DuplicateProductException extends Exception {
    public DuplicateProductException(String message) {
        super(message);
    }
}

class InventoryFullException extends Exception {
    public InventoryFullException(String message) {
        super(message);
    }
}

//Product Class
class Product{

    private int productId;
    private String productName;
    private double price;
    private int quantity;

    public Product(int productId, String productName, double price, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
    }

    public int getProductId(){
        return productId;
    }
    public void setProductId(int productId){
        this.productId = productId;
    }

    public String getProductName(){
        return productName;
    }
    public void setProductName(String productName){
        this.productName = productName;
    }

    public double getPrice(){
        return price;
    }
    public void setPrice(double price){
        this.price = price;
    }

    public int getQuantity(){
        return quantity;
    }
    public void setQuantity(int quantity){
        this.quantity = quantity;
    }

    public void displayProductInfo() {
        System.out.println("Product ID: " + productId);
        System.out.println("Name: " + productName);
        System.out.println("Price: " + price);
        System.out.println("Quantity: " + quantity);
    }
}

//Perishable Product Class
class PerishableProduct extends Product {
    private String expiryDate;

    public PerishableProduct(int productId, String productName, double price, int quantity, String expiryDate) {
        super(productId, productName, price, quantity);
        this.expiryDate = expiryDate;
    }

    public String getExpiryDate(){
        return expiryDate;
    }
    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void displayProductInfo() {
        super.displayProductInfo();
        System.out.println("Expiry Date: " + expiryDate);
    }
}

//Inventory Manager Class
class InventoryManager{

    private Product[] products;
    private int count;

    public InventoryManager(){
        products = new Product[50];
        count = 0;
    }

    public Product findProductNull(int id) {
        for (int i = 0; i < count; i++) {
            if (products[i].getProductId() == id) {
                return products[i];
            }
        }
        return null;
    }

    public void addProduct(Product product) throws DuplicateProductException, InventoryFullException {

        if (findProductNull(product.getProductId()) != null) {
            throw new DuplicateProductException("Product with ID " + product.getProductId() + " already exists.");
        }

        if (count >= products.length) {
            throw new InventoryFullException("Inventory is full. Cannot add more products.");
        }

        products[count++] = product;
        System.out.println("Product added successfully!");
    }

    public void viewAllProducts(){
        for (int i = 0; i < count; i++){
            System.out.println("---Product " + (i + 1) + "---");
            products[i].displayProductInfo();
            System.out.println();
        }
    }

    public Product searchProductById(int id) throws ProductNotFoundException {
        for (int i = 0; i < count; i++) {
            if (products[i].getProductId() == id) {
                return products[i];
            }
        }
        throw new ProductNotFoundException("Product with ID " + id + " not found.");
    }


    public void updateProductById(int id, Double newPrice, Integer newQty) throws ProductNotFoundException, InvalidInputException {

        Product p = searchProductById(id);

        if (newPrice != null && newPrice < 0)
            throw new InvalidInputException("Price cannot be negative.");

        if (newQty != null && newQty < 0)
            throw new InvalidInputException("Quantity cannot be negative.");

        if (newPrice != null) p.setPrice(newPrice);
        if (newQty != null) p.setQuantity(newQty);

        System.out.println("Product updated.");
    }


    public void deleteProductById(int id) throws ProductNotFoundException {

        int idx = -1;
        for (int i = 0; i < count; i++) {
            if (products[i].getProductId() == id) {
                idx = i;
                break;
            }
        }
        if (idx == -1)
            throw new ProductNotFoundException("Product with ID " + id + " not found.");

        for (int i = idx; i < count - 1; i++) {
            products[i] = products[i + 1];
        }
        products[--count] = null;
        System.out.println("Product deleted successfully.");
    }
}

//UI
class InventoryApp {
    public static void main(String[] args) {
        Application.launch(InventoryUI.class, args);
    }
}

public class InventoryUI extends Application {

    private InventoryManager manager = new InventoryManager();
    private ArrayList<Product> uiProducts = new ArrayList<>();

    private BorderPane root;
    private StackPane centerStack;

    private Pagination viewPagination;
    private ToggleGroup viewToggleGroup;
    private ScrollPane tableScrollPane;
    private VBox tableContainer = new VBox();

    public InventoryUI() {
        super();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Inventory Manager");

        root = new BorderPane();
        root.setPadding(new Insets(20));

        VBox header = new VBox();
        header.setAlignment(Pos.CENTER);
        Text title = new Text("Inventory Manager");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30));
        Text subtitle = new Text("Choose your option");
        subtitle.setFont(Font.font("Segoe UI", 14));
        subtitle.setFill(Color.GRAY);
        header.getChildren().addAll(title, subtitle);
        header.setSpacing(6);
        root.setTop(header);

        centerStack = new StackPane();
        centerStack.setPrefSize(800, 500);
        root.setCenter(centerStack);

        Pane homePane = buildHomePane();
        Pane addPane = buildAddPane();
        Pane viewPane = buildViewPane();
        Pane searchPane = buildSearchPane();
        Pane updatePane = buildUpdatePane();
        Pane deletePane = buildDeletePane();

        centerStack.getChildren().addAll(homePane, addPane, viewPane, searchPane, updatePane, deletePane);
        showPaneWithAnimation(homePane);

        Scene scene = new Scene(root, 900, 600);
        URL cssUrl = getClass().getResource("style_placeholder.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showPaneWithAnimation(Pane target) {
        for (javafx.scene.Node n : centerStack.getChildren()) n.setVisible(false);
        target.setVisible(true);
        target.setOpacity(0);

        TranslateTransition tt = new TranslateTransition(Duration.millis(300), target);
        tt.setFromY(20); tt.setToY(0);

        FadeTransition ft = new FadeTransition(Duration.millis(300), target);
        ft.setFromValue(0); ft.setToValue(1);

        ParallelTransition pt = new ParallelTransition(tt, ft);
        pt.play();
    }


    private Pane buildHomePane() {
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        box.setSpacing(25);

        HBox row1 = new HBox(20); row1.setAlignment(Pos.CENTER);
        HBox row2 = new HBox(20); row2.setAlignment(Pos.CENTER);

        Button addBtn = styledButton("Add Product", "#FF6B6B", 180, 60);
        Button viewBtn = styledButton("View Products", "#4ECDC4", 180, 60);
        Button searchBtn = styledButton("Search Product", "#45B7D1", 180, 60);
        Button updateBtn = styledButton("Update Product", "#FFBE0B", 180, 60);
        Button deleteBtn = styledButton("Delete Product", "#FF6B6B", 180, 60);
        Button exitBtn = styledButton("Exit", "#95A5A6", 180, 60);

        addBtn.setOnAction(e -> showPaneWithAnimation(lookupPaneByName("add")));
        viewBtn.setOnAction(e -> { if (viewPagination != null) refreshViewPagination(); showPaneWithAnimation(lookupPaneByName("view")); });
        searchBtn.setOnAction(e -> showPaneWithAnimation(lookupPaneByName("search")));
        updateBtn.setOnAction(e -> showPaneWithAnimation(lookupPaneByName("update")));
        deleteBtn.setOnAction(e -> showPaneWithAnimation(lookupPaneByName("delete")));
        exitBtn.setOnAction(e -> ((Stage) root.getScene().getWindow()).close());

        row1.getChildren().addAll(addBtn, viewBtn, searchBtn);
        row2.getChildren().addAll(updateBtn, deleteBtn, exitBtn);
        box.getChildren().addAll(row1, row2);
        return wrapNamedPane(box, "home");
    }


    private Pane buildAddPane() {
        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setAlignment(Pos.TOP_CENTER);

        HBox headerBox = new HBox(); headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0,0,10,0));
        Button backArrow = createBackArrow();
        backArrow.setOnAction(e -> { resetAddPane(); showPaneWithAnimation(lookupPaneByName("home")); });

        Text header = new Text("Add Product");
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        header.setFill(Color.web("#FF6B6B"));

        headerBox.getChildren().addAll(backArrow, header);
        HBox.setHgrow(headerBox, Priority.ALWAYS);
        headerBox.setAlignment(Pos.CENTER);

        VBox formContainer = new VBox(12);
        formContainer.setAlignment(Pos.CENTER); formContainer.setMaxWidth(400);

        TextField idField = new TextField(); idField.setPromptText("Product ID (integer)"); styleTextField(idField);
        TextField nameField = new TextField(); nameField.setPromptText("Product Name"); styleTextField(nameField);
        TextField priceField = new TextField(); priceField.setPromptText("Price (decimal)"); styleTextField(priceField);
        TextField qtyField = new TextField(); qtyField.setPromptText("Quantity (integer)"); styleTextField(qtyField);

        CheckBox perishable = new CheckBox("Perishable"); perishable.setTextFill(Color.web("#555555"));
        TextField expiryField = new TextField(); expiryField.setPromptText("Expiry Date (e.g. 2025-12-31)"); expiryField.setDisable(true); styleTextField(expiryField);
        perishable.selectedProperty().addListener((obs, oldV, newV) -> expiryField.setDisable(!newV));

        Button submit = styledButton("Add Product", "#FF6B6B", 160, 45);
        Label status = new Label();

        submit.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String name = nameField.getText().trim();
                if (name.isEmpty()) throw new InvalidInputException("Product name cannot be empty.");
                double price = Double.parseDouble(priceField.getText().trim());
                int qty = Integer.parseInt(qtyField.getText().trim());
                if (price < 0 || qty < 0) throw new InvalidInputException("Price or quantity cannot be negative.");

                Product p;
                if (perishable.isSelected()) {
                    String exp = expiryField.getText().trim();
                    if (exp.isEmpty()) throw new InvalidInputException("Expiry date required for perishable product.");
                    p = new PerishableProduct(id, name, price, qty, exp);
                } else {
                    p = new Product(id, name, price, qty);
                }

                try {
                    manager.addProduct(p);
                    uiProducts.add(p);
                    refreshViewPagination();
                    showStatus(status, "✓ Product added successfully!", Color.GREEN);
                    resetAddPane();
                } catch (DuplicateProductException | InventoryFullException ex) {
                    showExceptionDialog(ex.getMessage());
                }

            } catch (NumberFormatException nfe) {
                showExceptionDialog("Invalid number format.");
            } catch (InvalidInputException iie) {
                showExceptionDialog(iie.getMessage());
            } catch (Exception ex) {
                showExceptionDialog("Error: " + ex.getMessage());
            }
        });

        formContainer.getChildren().addAll(idField, nameField, priceField, qtyField, perishable, expiryField, submit, status);
        mainContainer.getChildren().addAll(headerBox, formContainer);
        return wrapNamedPane(mainContainer, "add");
    }

    private void resetAddPane() {
        Pane addPane = lookupPaneByName("add");
        if (addPane != null) {
            VBox mainContainer = (VBox) addPane;
            VBox formContainer = (VBox) mainContainer.getChildren().get(1);
            for (javafx.scene.Node node : formContainer.getChildren()) {
                if (node instanceof TextField) ((TextField) node).clear();
                else if (node instanceof CheckBox) ((CheckBox) node).setSelected(false);
            }
        }
    }


    private Pane buildViewPane() {
        VBox outer = new VBox(10); 
        outer.setPadding(new Insets(20));
        outer.setAlignment(Pos.TOP_CENTER);


        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0,0,10,0));
        Button backArrow = createBackArrow();
        backArrow.setOnAction(e -> showPaneWithAnimation(lookupPaneByName("home")));
        Text header = new Text("View Products");
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        header.setFill(Color.web("#4ECDC4"));
        headerBox.getChildren().addAll(backArrow, header);
        HBox.setHgrow(headerBox, Priority.ALWAYS);
        headerBox.setAlignment(Pos.CENTER);


        HBox toggleBox = new HBox(10);
        toggleBox.setAlignment(Pos.CENTER);
        ToggleButton pageViewBtn = new ToggleButton("Page View");
        pageViewBtn.setUserData("page");
        ToggleButton tableViewBtn = new ToggleButton("Table View");
        tableViewBtn.setUserData("table");
        viewToggleGroup = new ToggleGroup();
        pageViewBtn.setToggleGroup(viewToggleGroup);
        tableViewBtn.setToggleGroup(viewToggleGroup);
        pageViewBtn.setSelected(true);
        styleToggleButton(pageViewBtn, "#4ECDC4");
        styleToggleButton(tableViewBtn, "#4ECDC4");
        toggleBox.getChildren().addAll(pageViewBtn, tableViewBtn);


        StackPane contentArea = new StackPane();
        contentArea.setAlignment(Pos.TOP_CENTER);
        contentArea.setMinHeight(400);
        contentArea.setPrefHeight(400);
        contentArea.setMaxHeight(400);


        viewPagination = new Pagination(1);
        viewPagination.setPageFactory((index) -> createPageView(index));
        viewPagination.setMaxHeight(350);


        tableContainer = new VBox();
        tableContainer.setAlignment(Pos.TOP_CENTER);
        tableContainer.setSpacing(2);
        tableContainer.setPadding(new Insets(10));
        tableContainer.setMinHeight(350);

        tableScrollPane = new ScrollPane();
        tableScrollPane.setFitToWidth(true);
        tableScrollPane.setFitToHeight(true);
        tableScrollPane.setMinHeight(350);
        tableScrollPane.setPrefHeight(350);
        tableScrollPane.setContent(tableContainer);
        tableScrollPane.setVisible(false);
        tableScrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");


        contentArea.getChildren().addAll(viewPagination, tableScrollPane);

        viewToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                String viewType = (String) newToggle.getUserData();
                if ("page".equals(viewType)) {
                    viewPagination.setVisible(true);
                    tableScrollPane.setVisible(false);
                } else {
                    viewPagination.setVisible(false);
                    tableScrollPane.setVisible(true);
                    refreshTableView();
                }
            }
        });

        outer.getChildren().addAll(headerBox, toggleBox, contentArea);
        return wrapNamedPane(outer, "view");
    }

    private VBox createPageView(int index) {
        if (uiProducts.size() == 0) {
            VBox empty = new VBox(10); empty.setAlignment(Pos.CENTER);
            Label lbl = new Label("No products yet. Use Add Product to create some."); lbl.setTextFill(Color.GRAY);
            empty.getChildren().add(lbl); return empty;
        }
        if (index < 0 || index >= uiProducts.size()) return new VBox();
        Product p = uiProducts.get(index);
        VBox page = new VBox(12); page.setAlignment(Pos.CENTER); page.setPadding(new Insets(25));
        page.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; -fx-border-radius: 10;");

        Text idt = new Text("Product ID: " + p.getProductId()); idt.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        Text namet = new Text("Name: " + p.getProductName()); namet.setFont(Font.font(16));
        Text pricet = new Text("Price: $" + String.format("%.2f", p.getPrice())); pricet.setFont(Font.font(16));
        Text qtyt = new Text("Quantity: " + p.getQuantity()); qtyt.setFont(Font.font(16));
        page.getChildren().addAll(idt, namet, pricet, qtyt);

        if (p instanceof PerishableProduct) {
            Text exp = new Text("Expiry Date: " + ((PerishableProduct) p).getExpiryDate()); exp.setFont(Font.font(16));
            page.getChildren().add(exp);
        }

        HBox navHint = new HBox(10); navHint.setAlignment(Pos.CENTER);
        Label hint = new Label("Product " + (index+1) + " of " + uiProducts.size() + " - Use dots below to navigate"); hint.setTextFill(Color.GRAY);
        navHint.getChildren().add(hint);

        VBox container = new VBox(20, page, navHint); container.setAlignment(Pos.CENTER);
        return container;
    }
    private HBox createTableRow(String id, String name, String price, String quantity, String expiry, boolean isHeader) {
        HBox row = new HBox(10);
        row.setPadding(new Insets(10));
        row.setAlignment(Pos.CENTER);
        row.setPrefHeight(40);

        row.getChildren().addAll(
                createTableLabel(id, 80, isHeader),
                createTableLabel(name, 200, isHeader),
                createTableLabel(price, 100, isHeader),
                createTableLabel(quantity, 80, isHeader),
                createTableLabel(expiry, 120, isHeader)
        );
        return row;
    }


    private void refreshTableView() {
        tableContainer.getChildren().clear();

        if (uiProducts.size() == 0) {
            Label emptyLabel = new Label("No products yet. Use Add Product to create some.");
            emptyLabel.setTextFill(Color.GRAY);
            tableContainer.getChildren().add(emptyLabel);
            tableScrollPane.setContent(tableContainer);
            return;
        }

        HBox headerRow = createTableRow("ID", "Name", "Price", "Quantity", "Expiry Date", true);
        headerRow.setStyle("-fx-background-color: #4ECDC4; -fx-background-radius: 5;");
        tableContainer.getChildren().add(headerRow);

        for (int i = 0; i < uiProducts.size(); i++) {
            Product p = uiProducts.get(i);
            String expiryDate = (p instanceof PerishableProduct) ? ((PerishableProduct)p).getExpiryDate() : "N/A";
            HBox dataRow = createTableRow(
                    String.valueOf(p.getProductId()),
                    p.getProductName(),
                    "$"+String.format("%.2f",p.getPrice()),
                    String.valueOf(p.getQuantity()),
                    expiryDate,
                    false
            );
            dataRow.setStyle(i%2==0 ? "-fx-background-color: #f8f9fa; -fx-background-radius: 5;" : "-fx-background-color: #ffffff; -fx-background-radius: 5;");
            tableContainer.getChildren().add(dataRow);
        }


        tableContainer.setMinHeight(Region.USE_PREF_SIZE);
        tableScrollPane.setFitToHeight(true);
        tableScrollPane.setVvalue(0); 
    }

    private Label createTableLabel(String text, double width, boolean isHeader) {
        Label label = new Label(text); label.setPrefWidth(width); label.setMaxWidth(width); label.setPadding(new Insets(5)); label.setAlignment(Pos.CENTER);
        if (isHeader) { label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14)); label.setTextFill(Color.WHITE); }
        else { label.setFont(Font.font("Segoe UI", 14)); label.setTextFill(Color.BLACK); }
        return label;
    }

    private void refreshViewPagination() {
        int size = uiProducts.size();
        viewPagination.setPageCount(Math.max(size, 1));
        viewPagination.setPageFactory((index) -> createPageView(index));
        if (size > 0) viewPagination.setCurrentPageIndex(Math.min(viewPagination.getCurrentPageIndex(), size - 1));
        refreshTableView();
    }

    private void showStatus(Label label, String msg, Color color) {
        label.setText(msg);
        label.setTextFill(color);
        label.setStyle("-fx-font-weight: bold;");
        PauseTransition pt = new PauseTransition(Duration.seconds(2));
        pt.setOnFinished(e -> label.setText(""));
        pt.play();
    }

    private Pane buildSearchPane() {
        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setAlignment(Pos.TOP_CENTER);


        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 10, 0));

        Button backArrow = createBackArrow();
        backArrow.setOnAction(e -> {
            resetSearchPane();
            showPaneWithAnimation(lookupPaneByName("home"));
        });

        Text header = new Text("Search Product By ID");
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        header.setFill(Color.web("#45B7D1"));

        headerBox.getChildren().addAll(backArrow, header);
        HBox.setHgrow(headerBox, Priority.ALWAYS);
        headerBox.setAlignment(Pos.CENTER);

        VBox formContainer = new VBox(12);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setMaxWidth(400);

        TextField idField = new TextField();
        idField.setPromptText("Product ID (integer)");
        styleTextField(idField);

        Button findBtn = styledButton("Search Product", "#45B7D1", 160, 45); 
        Label result = new Label();
        result.setWrapText(true);
        result.setMaxWidth(350);

        findBtn.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                Product p = manager.findProductNull(id);
                if (p != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("ID: ").append(p.getProductId()).append("\n");
                    sb.append("Name: ").append(p.getProductName()).append("\n");
                    sb.append("Price: $").append(String.format("%.2f", p.getPrice())).append("\n");
                    sb.append("Quantity: ").append(p.getQuantity()).append("\n");
                    if (p instanceof PerishableProduct) {
                        sb.append("Expiry: ").append(((PerishableProduct)p).getExpiryDate()).append("\n");
                    }
                    result.setText(sb.toString());
                    result.setTextFill(Color.BLACK);
                } else {
                    throw new ProductNotFoundException("Product not Found");
                }
            } catch (NumberFormatException nfe) {
                showExceptionDialog("Please enter a valid integer ID.");
            } catch (ProductNotFoundException pnfe) {
                showExceptionDialog(pnfe.getMessage());
            } catch (Exception ex) {
                showExceptionDialog("Error: " + ex.getMessage());
            }
        });

        formContainer.getChildren().addAll(idField, findBtn, result);
        mainContainer.getChildren().addAll(headerBox, formContainer);
        return wrapNamedPane(mainContainer, "search");
    }

    private void resetSearchPane() {
        Pane searchPane = lookupPaneByName("search");
        if (searchPane != null) {
            VBox container = (VBox) ((VBox) searchPane).getChildren().get(1);
            for (javafx.scene.Node node : container.getChildren()) {
                if (node instanceof TextField) {
                    ((TextField) node).clear();
                } else if (node instanceof Label) {
                    ((Label) node).setText("");
                }
            }
        }
    }

    private Pane buildUpdatePane() {
        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setAlignment(Pos.TOP_CENTER);

        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 10, 0));

        Button backArrow = createBackArrow();
        backArrow.setOnAction(e -> {
            resetUpdatePane();
            showPaneWithAnimation(lookupPaneByName("home"));
        });

        Text header = new Text("Update Product");
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        header.setFill(Color.web("#FFBE0B")); 

        headerBox.getChildren().addAll(backArrow, header);
        HBox.setHgrow(headerBox, Priority.ALWAYS);
        headerBox.setAlignment(Pos.CENTER);

        VBox formContainer = new VBox(12);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setMaxWidth(400);

        TextField idField = new TextField();
        idField.setPromptText("Product ID (integer)");
        styleTextField(idField);

        TextField priceField = new TextField();
        priceField.setPromptText("New Price (leave empty to skip)");
        styleTextField(priceField);

        TextField qtyField = new TextField();
        qtyField.setPromptText("New Quantity (leave empty to skip)");
        styleTextField(qtyField);

        Button updateBtn = styledButton("Update Product", "#FFBE0B", 160, 45); 
        Label status = new Label();

        updateBtn.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                Double newPrice = null;
                Integer newQty = null;

                String pTxt = priceField.getText().trim();
                if (!pTxt.isEmpty()) {
                    try {
                        newPrice = (Double) Double.parseDouble(pTxt);
                    } catch (NumberFormatException nfe) {
                        throw new InvalidInputException("Invalid price format.");
                    }
                }

                String qTxt = qtyField.getText().trim();
                if (!qTxt.isEmpty()) {
                    try {
                        newQty = (Integer) Integer.parseInt(qTxt);
                    } catch (NumberFormatException nfe) {
                        throw new InvalidInputException("Invalid quantity format.");
                    }
                }
                try {
                    manager.updateProductById(id, newPrice, newQty);
                    for (int i = 0; i < uiProducts.size(); i++) {
                        if (uiProducts.get(i).getProductId() == id) {
                            Product p = uiProducts.get(i);
                            if (newPrice != null) p.setPrice(newPrice);
                            if (newQty != null) p.setQuantity(newQty);
                            break;
                        }
                    }
                    status.setText("✓ Product updated successfully!");
                    status.setTextFill(Color.GREEN);
                    status.setStyle("-fx-font-weight: bold;");
                    refreshViewPagination();
                } catch (ProductNotFoundException | InvalidInputException ex) {
                    showExceptionDialog(ex.getMessage());
                }

            } catch (NumberFormatException nfe) {
                showExceptionDialog("Please enter a valid integer ID.");
            } catch (InvalidInputException iie) {
                showExceptionDialog(iie.getMessage());
            } catch (Exception ex) {
                showExceptionDialog("Error: " + ex.getMessage());
            }
        });

        formContainer.getChildren().addAll(idField, priceField, qtyField, updateBtn, status);
        mainContainer.getChildren().addAll(headerBox, formContainer);
        return wrapNamedPane(mainContainer, "update");
    }

    private void resetUpdatePane() {
        Pane updatePane = lookupPaneByName("update");
        if (updatePane != null) {
            VBox mainContainer = (VBox) updatePane;
            VBox formContainer = (VBox) mainContainer.getChildren().get(1);
            for (javafx.scene.Node node : formContainer.getChildren()) {
                if (node instanceof TextField) {
                    ((TextField) node).clear();
                }
            }
        }
    }

    private Pane buildDeletePane() {
        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setAlignment(Pos.TOP_CENTER);


        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 10, 0));

        Button backArrow = createBackArrow();
        backArrow.setOnAction(e -> {
            resetDeletePane();
            showPaneWithAnimation(lookupPaneByName("home"));
        });

        Text header = new Text("Delete Product");
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        header.setFill(Color.web("#FF6B6B")); 

        headerBox.getChildren().addAll(backArrow, header);
        HBox.setHgrow(headerBox, Priority.ALWAYS);
        headerBox.setAlignment(Pos.CENTER);

        VBox formContainer = new VBox(12);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setMaxWidth(400);

        TextField idField = new TextField();
        idField.setPromptText("Product ID (integer)");
        styleTextField(idField);

        Button delBtn = styledButton("Delete Product", "#FF6B6B", 160, 45); 
        Label status = new Label();

        delBtn.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                try {
                    manager.deleteProductById(id);
                    uiProducts.removeIf(p -> p.getProductId() == id);
                    status.setText("✓ Product deleted successfully!");
                    status.setTextFill(Color.GREEN);
                    status.setStyle("-fx-font-weight: bold;");
                    refreshViewPagination();
                } catch (ProductNotFoundException pnfe) {
                    showExceptionDialog(pnfe.getMessage());
                }
            } catch (NumberFormatException nfe) {
                showExceptionDialog("Please enter a valid integer ID.");
            }
        });

        formContainer.getChildren().addAll(idField, delBtn, status);
        mainContainer.getChildren().addAll(headerBox, formContainer);
        return wrapNamedPane(mainContainer, "delete");
    }

    private void resetDeletePane() {
        Pane deletePane = lookupPaneByName("delete");
        if (deletePane != null) {
            VBox mainContainer = (VBox) deletePane;
            VBox formContainer = (VBox) mainContainer.getChildren().get(1);
            for (javafx.scene.Node node : formContainer.getChildren()) {
                if (node instanceof TextField) {
                    ((TextField) node).clear();
                }

            }
        }
    }

    private Button createBackArrow() {
        Button backArrow = new Button();
        backArrow.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-cursor: hand;");

        SVGPath arrowPath = new SVGPath();
        arrowPath.setContent("M15 18L9 12L15 6");
        arrowPath.setStroke(Color.GRAY);
        arrowPath.setStrokeWidth(2);

        backArrow.setGraphic(arrowPath);
        backArrow.setPadding(new Insets(5));

        backArrow.addEventHandler(MouseEvent.MOUSE_ENTERED, (MouseEvent e) -> {
            arrowPath.setStroke(Color.BLACK);
        });
        backArrow.addEventHandler(MouseEvent.MOUSE_EXITED, (MouseEvent e) -> {
            arrowPath.setStroke(Color.GRAY);
        });

        return backArrow;
    }

    private void styleTextField(TextField textField) {
        textField.setStyle("-fx-background-radius: 6; -fx-border-radius: 6; -fx-padding: 8 12; -fx-font-size: 14;");
        textField.setMaxWidth(300);
    }

    private void styleToggleButton(ToggleButton button, String color) {
        button.setStyle("-fx-background-radius: 6; -fx-border-radius: 6; -fx-padding: 8 16;");
        button.setTextFill(Color.web("#555555"));

        button.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                button.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 6; -fx-border-radius: 6; -fx-padding: 8 16; -fx-text-fill: white;");
            } else {
                button.setStyle("-fx-background-radius: 6; -fx-border-radius: 6; -fx-padding: 8 16; -fx-text-fill: #555555;");
            }
        });

        if (button.isSelected()) {
            button.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 6; -fx-border-radius: 6; -fx-padding: 8 16; -fx-text-fill: white;");
        }
    }

    private Pane wrapNamedPane(Pane p, String name) {
        p.setUserData(name);
        p.setVisible(false);
        return p;
    }

    private Pane lookupPaneByName(String name) {
        for (javafx.scene.Node n : centerStack.getChildren()) {
            if (n instanceof Pane) {
                Object ud = n.getUserData();
                if (ud != null && ud.equals(name)) return (Pane) n;
            }
        }
        return null;
    }

    private Button styledButton(String text, String colorHex, double width, double height) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + colorHex + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 18; -fx-font-size: 14;");
        b.setPrefSize(width, height);
        b.setCursor(javafx.scene.Cursor.HAND);
        b.setEffect(new DropShadow(6, Color.rgb(0,0,0,0.2)));
        b.addEventHandler(MouseEvent.MOUSE_ENTERED, (MouseEvent e) -> {
            b.setScaleX(1.05); b.setScaleY(1.05);
        });
        b.addEventHandler(MouseEvent.MOUSE_EXITED, (MouseEvent e) -> {
            b.setScaleX(1.0); b.setScaleY(1.0);
        });
        return b;
    }

    private void showExceptionDialog(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText("An error occurred");
        a.setContentText(msg);
        a.showAndWait();
    }
    private void showInfoDialog(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Info");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

}
