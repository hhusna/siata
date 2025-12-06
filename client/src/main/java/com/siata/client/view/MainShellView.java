package com.siata.client.view;

import com.siata.client.MainApplication;
import com.siata.client.session.LoginSession;
import com.siata.client.util.AnimationUtils;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.prefs.Preferences;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MainShellView extends BorderPane {

    private static final double SIDEBAR_EXPANDED_WIDTH = 220;
    private static final double SIDEBAR_COLLAPSED_WIDTH = 75;

    private final Map<MainPage, Node> pageContent = new EnumMap<>(MainPage.class);
    private final Map<MainPage, HBox> menuItems = new EnumMap<>(MainPage.class);
    private final java.util.List<Label> menuTextLabels = new java.util.ArrayList<>();

    private final VBox contentContainer = new VBox();
    private final Label pageTitle = new Label();
    private final Label pageSubtitle = new Label();
    private BorderPane sidebar;
    private VBox brandingBox;
    private Label collapsedBrandingLabel;
    private Button logoutButton;
    private boolean sidebarExpanded = true;
    private boolean isLoading = false;
    private boolean isAnimating = false;

    private Optional<Runnable> onLogout = Optional.empty();
    private MainPage activePage;

    public MainShellView() {
        buildView();
        switchPage(MainPage.DASHBOARD);
    }

    private void buildView() {
        sidebar = (BorderPane) buildSidebar();
        
        // Create right side container with header on top and content below
        VBox rightSide = new VBox();
        rightSide.getChildren().addAll(buildHeader(), buildContentContainer());
        VBox.setVgrow(rightSide.getChildren().get(1), Priority.ALWAYS);
        
        setLeft(sidebar);
        setCenter(rightSide);
    }

    private Node buildSidebar() {
        BorderPane sidebar = new BorderPane();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(SIDEBAR_EXPANDED_WIDTH);
        sidebar.setMinWidth(SIDEBAR_COLLAPSED_WIDTH);

        // Logo icon using PNG image in white rounded container
        StackPane logoContainer = createLogoContainer();
        
        // Title and subtitle on the right
        Label title = new Label("SIADA");
        title.getStyleClass().add("sidebar-title");
        Label subtitle = new Label("Direktorat Angud");
        subtitle.getStyleClass().add("sidebar-subtitle");
        
        VBox textBox = new VBox(2, title, subtitle);
        textBox.setAlignment(Pos.CENTER_LEFT);
        
        // HBox with logo and text
        HBox brandingContent = new HBox(12, logoContainer, textBox);
        brandingContent.setAlignment(Pos.CENTER_LEFT);
        
        brandingBox = new VBox(brandingContent);
        brandingBox.setPadding(new Insets(16, 16, 10, 16));
        
        // Collapsed branding - show only logo when collapsed
        StackPane collapsedLogoContainer = createLogoContainer();
        
        collapsedBrandingLabel = new Label();
        collapsedBrandingLabel.setGraphic(collapsedLogoContainer);
        collapsedBrandingLabel.setPadding(new Insets(16, 16, 10, 16));
        collapsedBrandingLabel.setVisible(false);
        collapsedBrandingLabel.setOpacity(0);
        
        // Stack both brandings
        StackPane brandingStack = new StackPane(brandingBox, collapsedBrandingLabel);
        brandingStack.setAlignment(Pos.TOP_LEFT);

        // Separator line between branding and menu (centered, low opacity white)
        Region separatorLine = new Region();
        separatorLine.setStyle("-fx-background-color: rgba(255, 255, 255, 0.4);");
        separatorLine.setMinHeight(1);
        separatorLine.setPrefHeight(1);
        separatorLine.setMaxHeight(1);
        separatorLine.setPrefWidth(180);
        separatorLine.setMinWidth(180);
        
        HBox separatorContainer = new HBox(separatorLine);
        separatorContainer.setAlignment(Pos.CENTER);
        separatorContainer.setPadding(new Insets(12, 16, 12, 16));

        VBox menu = new VBox(10);

        // --- LOGIKA ROLE BASE DI SINI ---
        String role = LoginSession.getRole();
        if (role == null) role = ""; // Antisipasi null

        // 1. Semua user bisa akses Dashboard
        addMenuToSidebar(menu, MainPage.DASHBOARD);

        // 2. Cek Role menggunakan If-Else
        if (role.equals("TIM_MANAJEMEN_ASET")) {
            // Admin Aset: Akses Penuh Pengelolaan
            addMenuToSidebar(menu, MainPage.RECAPITULATION);
            addMenuToSidebar(menu, MainPage.EMPLOYEE_MANAGEMENT);
            addMenuToSidebar(menu, MainPage.ASSET_MANAGEMENT);
            addMenuToSidebar(menu, MainPage.ASSET_REQUEST);
            addMenuToSidebar(menu, MainPage.ASSET_APPROVAL);
            addMenuToSidebar(menu, MainPage.ASSET_REMOVAL);
            addMenuToSidebar(menu, MainPage.LOGBOOK);

        } else if (role.equals("PPBJ") || role.equals("PPK") || role.equals("DIREKTUR")) {
            // Tim Approval: Hanya melihat rekap, persetujuan, dan log
            addMenuToSidebar(menu, MainPage.RECAPITULATION);
            addMenuToSidebar(menu, MainPage.ASSET_APPROVAL);
            addMenuToSidebar(menu, MainPage.LOGBOOK);

        } else {
            // Role Lainnya (Pegawai Biasa): Hanya bisa mengajukan aset
            addMenuToSidebar(menu, MainPage.ASSET_REQUEST);
        }

        ScrollPane menuScroll = new ScrollPane(menu);
        menuScroll.setFitToWidth(true);
        menuScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        menuScroll.getStyleClass().add("sidebar-scroll");
        BorderPane.setMargin(menuScroll, new Insets(0, 10, 0, 10));

        logoutButton = new Button("🚪 Logout");
        logoutButton.getStyleClass().add("sidebar-logout-button");
        logoutButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setOnAction(event -> onLogout.ifPresent(Runnable::run));
        BorderPane.setMargin(logoutButton, new Insets(12, 16, 16, 16));

        // Combine branding and separator in a VBox for top section
        VBox topSection = new VBox();
        topSection.getChildren().addAll(brandingStack, separatorContainer);
        
        sidebar.setTop(topSection);
        sidebar.setCenter(menuScroll);
        sidebar.setBottom(logoutButton);
        return sidebar;
    }

    // Helper method untuk menambahkan menu agar kode lebih rapi
    private void addMenuToSidebar(VBox menu, MainPage page) {
        HBox item = createMenuItem(page);
        menu.getChildren().add(item);
        menuItems.put(page, item);
    }

    private HBox createMenuItem(MainPage page) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.getStyleClass().add("sidebar-item");

        Label iconLabel = new Label(page.icon());
        iconLabel.getStyleClass().add("sidebar-item-icon");
        iconLabel.setMinWidth(40);
        iconLabel.setWrapText(false);

        Label titleLabel = new Label(page.title());
        titleLabel.getStyleClass().add("sidebar-item-text");
        menuTextLabels.add(titleLabel); // Track for collapse animation

        item.getChildren().addAll(iconLabel, titleLabel);

        if (page.badgeText() != null && !page.badgeText().isEmpty()) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label badge = new Label(page.badgeText());
            badge.getStyleClass().add("sidebar-badge");
            menuTextLabels.add(badge); // Track badge too

            item.getChildren().addAll(spacer, badge);
        }

        // Add hover scale animation
        AnimationUtils.addHoverScaleEffect(item, 1.03);
        
        item.setOnMouseClicked(event -> switchPage(page));
        return item;
    }

    private Node buildHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("dashboard-header");
        header.setPadding(new Insets(12, 20, 12, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(16);

        Button menuButton = new Button("☰");
        menuButton.getStyleClass().add("ghost-button");
        menuButton.setOnAction(e -> toggleSidebar());

        // Page title and subtitle next to hamburger menu
        VBox titleBox = new VBox(2);
        pageTitle.getStyleClass().add("dashboard-title");
        pageSubtitle.getStyleClass().add("dashboard-subtitle");
        titleBox.getChildren().addAll(pageTitle, pageSubtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox userBox = new VBox(2);
        userBox.setAlignment(Pos.CENTER_RIGHT);

        // Update Header dengan Data Session yang Real
        String usernameText = LoginSession.getUsername() != null ? LoginSession.getUsername() : "User";
        String roleText = LoginSession.getRole() != null ? LoginSession.getRole().replace("_", " ") : "Guest";

        Label username = new Label(usernameText);
        username.getStyleClass().add("header-username");
        Label role = new Label(roleText);
        role.getStyleClass().add("header-role");
        userBox.getChildren().addAll(username, role);

        Button avatar = new Button(usernameText.substring(0, 1).toUpperCase());
        avatar.getStyleClass().add("header-avatar");
        avatar.setOnAction(e -> showProfileMenu(avatar));

        // Order: menuButton, titleBox on left, spacer pushes rest to right
        header.getChildren().addAll(menuButton, titleBox, spacer, userBox, avatar);
        return header;
    }

    private Node buildContentContainer() {
        contentContainer.setSpacing(20);
        contentContainer.setPadding(new Insets(24));
        contentContainer.getStyleClass().add("dashboard-content");

        ScrollPane scrollPane = new ScrollPane(contentContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("dashboard-scroll");
        return scrollPane;
    }

    private Node resolveContent(MainPage page) {
        if (pageContent.containsKey(page)) {
            return pageContent.get(page);
        }

        Node content = switch (page) {
            case DASHBOARD -> new DashboardContentView();
            case RECAPITULATION -> new RecapitulationView();
            case ASSET_MANAGEMENT -> new AssetManagementView();
            case EMPLOYEE_MANAGEMENT -> new EmployeeManagementView();
            case ASSET_REQUEST -> new AssetRequestView();
            case ASSET_APPROVAL -> new AssetApprovalView();
            case ASSET_REMOVAL -> new AssetRemovalView();
            case LOGBOOK -> new LogbookView();
        };

        pageContent.put(page, content);
        return content;
    }

    public void switchPage(MainPage page) {
        if (page == null || isLoading) {
            return;
        }
        
        updateActiveMenu(page);
        updateHeader(page);
        
        // Use async loading with fade animation for all pages
        if (!contentContainer.getChildren().isEmpty()) {
            isLoading = true;
            Node currentContent = contentContainer.getChildren().get(0);
            
            // Fade out current content (opacity to 0.4)
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentContent);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.4);
            fadeOut.play();
            
            // Load data in background thread
            Task<Node> loadTask = new Task<>() {
                @Override
                protected Node call() {
                    // This runs in background thread - just get the view (no UI updates here)
                    return resolveContent(page);
                }
            };
            
            loadTask.setOnSucceeded(event -> {
                Node newContent = loadTask.getValue();
                
                // Refresh data on FX thread
                if (page == MainPage.DASHBOARD && newContent instanceof DashboardContentView) {
                    ((DashboardContentView) newContent).refreshDashboard();
                } else if (page == MainPage.RECAPITULATION && newContent instanceof RecapitulationView) {
                    ((RecapitulationView) newContent).refreshData();
                }
                
                // Set new content with initial low opacity
                newContent.setOpacity(0.4);
                contentContainer.getChildren().setAll(newContent);
                
                // Fade in new content
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newContent);
                fadeIn.setFromValue(0.4);
                fadeIn.setToValue(1.0);
                fadeIn.setOnFinished(e -> isLoading = false);
                fadeIn.play();
                
                activePage = page;
            });
            
            loadTask.setOnFailed(event -> {
                isLoading = false;
                currentContent.setOpacity(1.0);
            });
            
            new Thread(loadTask).start();
        } else {
            // For lighter pages, load synchronously
            Node content = resolveContent(page);
            
            if (page == MainPage.DASHBOARD && content instanceof DashboardContentView) {
                ((DashboardContentView) content).refreshDashboard();
            } else if (page == MainPage.RECAPITULATION && content instanceof RecapitulationView) {
                ((RecapitulationView) content).refreshData();
            }
            
            contentContainer.getChildren().setAll(content);
            AnimationUtils.pageTransitionIn(content);
            activePage = page;
        }
    }

    /**
     * Navigate to a page and trigger a search with the given query.
     * Used for cross-page navigation (e.g., clicking employee name in Asset Management)
     */
    public void navigateToPageWithSearch(MainPage page, String searchQuery) {
        // First, force recreate the page to ensure we have a fresh instance
        pageContent.remove(page);
        
        // Navigate to the page
        switchPage(page);
        
        // After navigation, find the search field and set the query
        Node content = pageContent.get(page);
        if (content instanceof EmployeeManagementView empView) {
            // Trigger search in EmployeeManagementView
            empView.searchAndHighlight(searchQuery);
        }
    }

    private void updateActiveMenu(MainPage page) {
        menuItems.forEach((mainPage, node) -> {
            node.getStyleClass().remove("sidebar-item-active");
            if (mainPage == page) {
                if (!node.getStyleClass().contains("sidebar-item-active")) {
                    node.getStyleClass().add("sidebar-item-active");
                }
            }
        });
    }

    private void updateHeader(MainPage page) {
        pageTitle.setText(page.title());
        // Dynamic current date in Indonesian format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", new Locale("id", "ID"));
        pageSubtitle.setText(LocalDate.now().format(formatter));
    }

    public void setOnLogout(Runnable onLogout) {
        try {
            Preferences prefs = Preferences.userNodeForPackage(MainApplication.class);
            prefs.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.onLogout = Optional.ofNullable(onLogout);
    }

    private void toggleSidebar() {
        if (isAnimating) return;
        isAnimating = true;
        
        double targetWidth = sidebarExpanded ? SIDEBAR_COLLAPSED_WIDTH : SIDEBAR_EXPANDED_WIDTH;
        double targetOpacity = sidebarExpanded ? 0.0 : 1.0;
        
        // Animate sidebar width
        Timeline widthTimeline = new Timeline(
            new KeyFrame(Duration.millis(200),
                new KeyValue(sidebar.prefWidthProperty(), targetWidth)
            )
        );
        
        // Animate text opacity (fade out when collapsing, fade in when expanding)
        FadeTransition brandingFade = new FadeTransition(Duration.millis(150), brandingBox);
        brandingFade.setToValue(targetOpacity);
        
        // Animate collapsed branding (plane icon) - opposite of regular branding
        FadeTransition collapsedBrandingFade = new FadeTransition(Duration.millis(150), collapsedBrandingLabel);
        collapsedBrandingFade.setToValue(sidebarExpanded ? 1.0 : 0.0); // Show when collapsing, hide when expanding
        
        FadeTransition logoutFade = new FadeTransition(Duration.millis(150), logoutButton);
        logoutFade.setToValue(targetOpacity);
        
        // Fade menu text labels
        for (Label label : menuTextLabels) {
            FadeTransition fade = new FadeTransition(Duration.millis(150), label);
            fade.setToValue(targetOpacity);
            fade.play();
        }
        
        // Update logout button text and branding visibility
        if (sidebarExpanded) {
            // Collapsing - change to icon only, show plane
            collapsedBrandingLabel.setVisible(true);
            brandingFade.setOnFinished(e -> brandingBox.setVisible(false));
            logoutFade.setOnFinished(e -> logoutButton.setText("🚪"));
        } else {
            // Expanding - show full text, hide plane
            brandingBox.setVisible(true);
            collapsedBrandingFade.setOnFinished(e -> collapsedBrandingLabel.setVisible(false));
            logoutButton.setText("🚪 Logout");
        }
        
        brandingFade.play();
        collapsedBrandingFade.play();
        logoutFade.play();
        
        widthTimeline.setOnFinished(e -> {
            sidebarExpanded = !sidebarExpanded;
            isAnimating = false;
        });
        
        widthTimeline.play();
    }

    private void showProfileMenu(Button avatar) {
        javafx.stage.Stage popupStage = new javafx.stage.Stage();
        popupStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        popupStage.initModality(javafx.stage.Modality.NONE);
        
        VBox popup = new VBox(8);
        popup.setPadding(new Insets(16));
        popup.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");
        popup.setMinWidth(250);
        
        // User info section
        VBox userInfo = new VBox(4);
        Label nameLabel = new Label(LoginSession.getUsername() != null ? LoginSession.getUsername() : "User");
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label emailLabel = new Label(LoginSession.getPegawaiDto() != null && LoginSession.getPegawaiDto().getNama() != null 
            ? LoginSession.getPegawaiDto().getNama() : "");
        emailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        Label roleLabel = new Label(LoginSession.getRole() != null ? LoginSession.getRole().replace("_", " ") : "Guest");
        roleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2563eb; -fx-padding: 4 8; -fx-background-color: #eff6ff; -fx-background-radius: 4;");
        
        userInfo.getChildren().addAll(nameLabel, emailLabel, roleLabel);
        
        // Separator
        javafx.scene.control.Separator separator = new javafx.scene.control.Separator();
        separator.setPadding(new Insets(8, 0, 8, 0));
        
        // Menu buttons
        Button viewProfileBtn = new Button("👤 Lihat Profile");
        viewProfileBtn.setMaxWidth(Double.MAX_VALUE);
        viewProfileBtn.setAlignment(Pos.CENTER_LEFT);
        viewProfileBtn.setStyle("-fx-background-color: transparent; -fx-padding: 8; -fx-cursor: hand;");
        viewProfileBtn.setOnMouseEntered(e -> viewProfileBtn.setStyle("-fx-background-color: #f3f4f6; -fx-padding: 8; -fx-cursor: hand; -fx-background-radius: 4;"));
        viewProfileBtn.setOnMouseExited(e -> viewProfileBtn.setStyle("-fx-background-color: transparent; -fx-padding: 8; -fx-cursor: hand;"));
        viewProfileBtn.setOnAction(e -> {
            showProfileDetail();
            popupStage.close();
        });
        
        Button settingsBtn = new Button("⚙ Pengaturan");
        settingsBtn.setMaxWidth(Double.MAX_VALUE);
        settingsBtn.setAlignment(Pos.CENTER_LEFT);
        settingsBtn.setStyle("-fx-background-color: transparent; -fx-padding: 8; -fx-cursor: hand;");
        settingsBtn.setOnMouseEntered(e -> settingsBtn.setStyle("-fx-background-color: #f3f4f6; -fx-padding: 8; -fx-cursor: hand; -fx-background-radius: 4;"));
        settingsBtn.setOnMouseExited(e -> settingsBtn.setStyle("-fx-background-color: transparent; -fx-padding: 8; -fx-cursor: hand;"));
        settingsBtn.setOnAction(e -> {
            showSettings();
            popupStage.close();
        });
        
        popup.getChildren().addAll(userInfo, separator, viewProfileBtn, settingsBtn);
        
        // Position popup below avatar button
        javafx.geometry.Bounds bounds = avatar.localToScreen(avatar.getBoundsInLocal());
        popupStage.setX(bounds.getMinX() - 200);
        popupStage.setY(bounds.getMaxY() + 8);
        
        javafx.scene.Scene scene = new javafx.scene.Scene(popup);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        popupStage.setScene(scene);
        
        // Close popup when clicking outside
        popupStage.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                popupStage.close();
            }
        });
        
        popupStage.show();
    }

    private void showProfileDetail() {
        javafx.stage.Stage modalStage = new javafx.stage.Stage();
        modalStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        modalStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        modalStage.setTitle("Profile Pengguna");

        VBox modalContent = new VBox(0);
        modalContent.setPrefWidth(480);
        modalContent.setMaxWidth(480);
        modalContent.getStyleClass().add("modal-content");

        // Header with close button
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(24, 24, 16, 24));
        HBox.setHgrow(headerBox, Priority.ALWAYS);

        VBox titleBox = new VBox(4);
        Label title = new Label("Profile Pengguna");
        title.getStyleClass().add("modal-title");
        Label subtitle = new Label("Informasi akun Anda");
        subtitle.getStyleClass().add("modal-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeButton = new Button("✕");
        closeButton.getStyleClass().add("modal-close-button");
        closeButton.setOnAction(e -> modalStage.close());

        headerBox.getChildren().addAll(titleBox, spacer, closeButton);

        // Profile content
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setVgap(16);
        grid.setHgap(16);
        grid.setPadding(new Insets(0, 24, 24, 24));

        int row = 0;
        addProfileRow(grid, row++, "Username", LoginSession.getUsername() != null ? LoginSession.getUsername() : "-");
        
        if (LoginSession.getPegawaiDto() != null) {
            addProfileRow(grid, row++, "NIP", String.valueOf(LoginSession.getPegawaiDto().getNip()));
            addProfileRow(grid, row++, "Nama Lengkap", LoginSession.getPegawaiDto().getNama() != null ? LoginSession.getPegawaiDto().getNama() : "-");
            addProfileRow(grid, row++, "Subdirektorat", LoginSession.getPegawaiDto().getNamaSubdir() != null ? LoginSession.getPegawaiDto().getNamaSubdir() : "-");
            addProfileRow(grid, row++, "Jabatan", LoginSession.getPegawaiDto().getJabatan() != null ? LoginSession.getPegawaiDto().getJabatan() : "-");
        }
        
        addProfileRow(grid, row++, "Role", LoginSession.getRole() != null ? LoginSession.getRole().replace("_", " ") : "-");

        modalContent.getChildren().addAll(headerBox, grid);

        javafx.scene.Scene scene = new javafx.scene.Scene(modalContent);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        modalStage.setScene(scene);
        
        // Setup smooth modal animation
        AnimationUtils.setupModalAnimation(modalStage, modalContent);
        
        modalStage.showAndWait();
    }

    private void addProfileRow(javafx.scene.layout.GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("form-label");
        Label valueNode = new Label(value == null || value.isEmpty() ? "-" : value);
        valueNode.setStyle("-fx-font-weight: 600; -fx-font-size: 14px;");
        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    private void showSettings() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Pengaturan");
        alert.setHeaderText("Pengaturan Aplikasi");
        alert.setContentText("Fitur pengaturan akan segera tersedia.\n\nAnda dapat mengatur:\n• Preferensi tampilan\n• Keamanan akun\n• Dan lainnya");
        alert.showAndWait();
    }
    
    private StackPane createLogoContainer() {
        StackPane container = new StackPane();
        container.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        container.setMinSize(40, 40);
        container.setMaxSize(40, 40);
        container.setAlignment(Pos.CENTER);
        
        try {
            Image planeImage = new Image(getClass().getResourceAsStream("/plane_icon.png"));
            ImageView imageView = new ImageView(planeImage);
            imageView.setFitWidth(24);
            imageView.setFitHeight(24);
            imageView.setPreserveRatio(true);
            container.getChildren().add(imageView);
        } catch (Exception e) {
            // Fallback to text if image fails
            Label fallback = new Label("✈");
            fallback.setStyle("-fx-font-size: 18px; -fx-text-fill: #1e3a5f;");
            container.getChildren().add(fallback);
        }
        
        return container;
    }
}