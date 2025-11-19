package com.siata.client.view;

import com.siata.client.MainApplication;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.prefs.Preferences;

public class MainShellView extends BorderPane {

    private final Map<MainPage, Node> pageContent = new EnumMap<>(MainPage.class);
    private final Map<MainPage, HBox> menuItems = new EnumMap<>(MainPage.class);

    private final VBox contentContainer = new VBox();
    private final Label pageTitle = new Label();
    private final Label pageSubtitle = new Label();

    private Optional<Runnable> onLogout = Optional.empty();
    private MainPage activePage;

    public MainShellView() {
        buildView();
        switchPage(MainPage.DASHBOARD);
    }

    private void buildView() {
        setLeft(buildSidebar());
        setTop(buildHeader());
        setCenter(buildContentContainer());
    }

    private Node buildSidebar() {
        BorderPane sidebar = new BorderPane();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(260);

        Label title = new Label("SIAD - Direktorat Angkutan Udara");
        title.getStyleClass().add("sidebar-title");
        Label subtitle = new Label("Kementerian Perhubungan");
        subtitle.getStyleClass().add("sidebar-subtitle");

        VBox branding = new VBox(6, title, subtitle);
        branding.setPadding(new Insets(28, 24, 12, 24));

        VBox menu = new VBox(10);
        for (MainPage page : MainPage.values()) {
            HBox item = createMenuItem(page);
            menu.getChildren().add(item);
            menuItems.put(page, item);
        }

        ScrollPane menuScroll = new ScrollPane(menu);
        menuScroll.setFitToWidth(true);
        menuScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        menuScroll.getStyleClass().add("sidebar-scroll");
        BorderPane.setMargin(menuScroll, new Insets(0, 12, 0, 12));

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("sidebar-logout-button");
        logoutButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setOnAction(event -> onLogout.ifPresent(Runnable::run));
        BorderPane.setMargin(logoutButton, new Insets(16, 24, 24, 24));

        sidebar.setTop(branding);
        sidebar.setCenter(menuScroll);
        sidebar.setBottom(logoutButton);
        return sidebar;
    }

    private HBox createMenuItem(MainPage page) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.getStyleClass().add("sidebar-item");

        Label iconLabel = new Label(page.icon());
        iconLabel.getStyleClass().add("sidebar-item-icon");

        Label titleLabel = new Label(page.title());
        titleLabel.getStyleClass().add("sidebar-item-text");

        item.getChildren().addAll(iconLabel, titleLabel);

        if (page.badgeText() != null && !page.badgeText().isEmpty()) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label badge = new Label(page.badgeText());
            badge.getStyleClass().add("sidebar-badge");

            item.getChildren().addAll(spacer, badge);
        }

        item.setOnMouseClicked(event -> switchPage(page));
        return item;
    }

    private Node buildHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("dashboard-header");
        header.setPadding(new Insets(16, 24, 16, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(16);

        Button menuButton = new Button("☰");
        menuButton.getStyleClass().add("ghost-button");

        VBox titleBox = new VBox(4);
        pageTitle.getStyleClass().add("dashboard-title");
        pageSubtitle.getStyleClass().add("dashboard-subtitle");
        titleBox.getChildren().addAll(pageTitle, pageSubtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label notification = new Label("🔔");
        notification.getStyleClass().add("header-icon");

        VBox userBox = new VBox(2);
        userBox.setAlignment(Pos.CENTER_RIGHT);
        Label username = new Label("admin");
        username.getStyleClass().add("header-username");
        Label role = new Label("Admin");
        role.getStyleClass().add("header-role");
        userBox.getChildren().addAll(username, role);

        Label avatar = new Label("A");
        avatar.getStyleClass().add("header-avatar");

        header.getChildren().addAll(menuButton, titleBox, spacer, notification, userBox, avatar);
        return header;
    }

    private Node buildContentContainer() {
        contentContainer.setSpacing(24);
        contentContainer.setPadding(new Insets(32));
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
        if (page == null) {
            return;
        }
        updateActiveMenu(page);
        updateHeader(page);
        // Always recreate content to ensure fresh data
        pageContent.remove(page);
        Node content = resolveContent(page);
        contentContainer.getChildren().setAll(content);
        activePage = page;
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
        pageSubtitle.setText(page.dateLabel());
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
}

