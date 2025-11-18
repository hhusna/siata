package com.siata.client.controller;

import com.siata.client.view.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class MainShellController {

    @FXML private VBox menuBox;
    @FXML private Button logoutButton;
    @FXML private VBox contentContainer;
    @FXML private Label pageTitle;
    @FXML private Label pageSubtitle;

    private final Map<MainPage, Node> pageContent = new EnumMap<>(MainPage.class);
    private final Map<MainPage, HBox> menuItems = new EnumMap<>(MainPage.class);

    private Optional<Runnable> onLogout = Optional.empty();
    private MainPage activePage;

    @FXML
    private void initialize() {
        // init sidebar menu items
        for (MainPage page : MainPage.values()) {
            HBox item = createMenuItem(page);
            menuBox.getChildren().add(item);
            menuItems.put(page, item);
        }

        logoutButton.setOnAction(e -> onLogout.ifPresent(Runnable::run));
        switchPage(MainPage.DASHBOARD);
    }

    private HBox createMenuItem(MainPage page) {
        HBox item = new HBox(12);
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

        item.setOnMouseClicked(e -> switchPage(page));
        return item;
    }

    public void switchPage(MainPage page) {
        if (page == null) return;
        updateActiveMenu(page);
        updateHeader(page);
        pageContent.remove(page); // always refresh
        Node content = resolveContent(page);
        contentContainer.getChildren().setAll(content);
        activePage = page;
    }

    private Node resolveContent(MainPage page) {
        if (pageContent.containsKey(page)) return pageContent.get(page);

//        Node content = switch (page) {
//            case DASHBOARD -> new DashboardContentView();
//            case RECAPITULATION -> new RecapitulationView();
//            case ASSET_MANAGEMENT -> new AssetManagementView();
//            case EMPLOYEE_MANAGEMENT -> new EmployeeManagementView();
//            case ASSET_REQUEST -> new AssetRequestView();
//            case ASSET_APPROVAL -> new AssetApprovalView();
//            case ASSET_REMOVAL -> new AssetRemovalView();
//            case LOGBOOK -> new LogbookView();
//        };

//        Node content = switch (page) {
//            case DASHBOARD -> loadFXML("/com.siata.client/view/DashboardContentView.fxml");
//            case RECAPITULATION -> loadFXML("/com.siata.client/view/RecapitulationView.fxml");
//            case ASSET_MANAGEMENT -> loadFXML("/com.siata.client/view/AssetManagementView.fxml");
//            case EMPLOYEE_MANAGEMENT -> loadFXML("/com.siata.client/view/EmployeeManagementView.fxml");
//            case ASSET_REQUEST -> loadFXML("/com.siata.client/view/AssetRequestView.fxml");
//            case ASSET_APPROVAL -> new AssetApprovalView();
//            case ASSET_REMOVAL -> new AssetRemovalView();
//            case LOGBOOK -> new LogbookView();
//        };

        Node content = switch (page) {
            case DASHBOARD -> loadFXML("/com/siata/client/controller/DashboardContentView.fxml");
            case RECAPITULATION -> loadFXML("/com/siata/client/controller/RecapitulationView.fxml");
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

    private Node loadFXML(String path) {
        try {
            return FXMLLoader.load(getClass().getResource(path));
        } catch (IOException e) {
            e.printStackTrace();
            return new Label("Failed to load view: " + path);
        }
    }

    private void updateActiveMenu(MainPage page) {
        menuItems.forEach((p, node) -> {
            node.getStyleClass().remove("sidebar-item-active");
            if (p == page && !node.getStyleClass().contains("sidebar-item-active")) {
                node.getStyleClass().add("sidebar-item-active");
            }
        });
    }

    private void updateHeader(MainPage page) {
        pageTitle.setText(page.title());
        pageSubtitle.setText(page.dateLabel());
    }

    public void setOnLogout(Runnable onLogout) {
        this.onLogout = Optional.ofNullable(onLogout);
    }
}