package com.ecnu.adsmls.components.editor.modeleditor;

import com.ecnu.adsmls.components.ChooseFileButton;
import com.ecnu.adsmls.components.editor.treeeditor.impl.BehaviorRegister;
import com.ecnu.adsmls.model.MCar;
import com.ecnu.adsmls.utils.FileSystem;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 点击 New Car 显示的内容
 */
public class CarPane {
    // Project 路径
    private String projectPath;

    private GridPane gridPane = new GridPane();
    // 名称
    private TextField tfName;
    // 蓝图类型
    private ComboBox<String> cbModel;
    // 最大速度
    private TextField tfMaxSpeed;
    // 初速度
    private TextField tfInitSpeed;
    // 位置
    private ComboBox<String> cbLocation;
    // 位置参数
    private GridPane gridPaneLocationParams;
    // 朝向，和路同向或反向
    private ComboBox<String> cbHeading;
    // 偏移程度
    private TextField tfRoadDeviation;
    // 动态信息，一棵树
    private Node btDynamic;

    public CarPane(String projectPath) {
        this.projectPath = projectPath;
        this.createNode();
    }

    public MCar save() {
        MCar car = new MCar();
        car.setName(this.tfName.getText());
        car.setModel(this.cbModel.getValue());
        car.setMaxSpeed(Double.parseDouble(this.tfMaxSpeed.getText()));
        car.setInitSpeed(Double.parseDouble(this.tfInitSpeed.getText()));
        car.setLocationType(this.cbLocation.getValue());

        // TODO 参数检查
        LinkedHashMap<String, String> locationParams = new LinkedHashMap<>();
        String locationParamName = "";
        String locationParamValue = "";
        for(Node node : this.gridPaneLocationParams.getChildren()) {
            if(node instanceof Label) {
                locationParamName = ((Label) node).getText();
            }
            else if(node instanceof TextField) {
                locationParamValue = ((TextField) node).getText();
                locationParams.put(locationParamName, locationParamValue);
            }
        }
        car.setLocationParams(locationParams);

        car.setHeading(Objects.equals("same", this.cbHeading.getValue()));
        car.setRoadDeviation(Double.parseDouble(this.tfRoadDeviation.getText()));
        // 转换成相对路径
        String path = ((ChooseFileButton) this.btDynamic.getUserData()).getFile().getAbsolutePath();
        String relativePath = FileSystem.getRelativePath(this.projectPath, path);
        car.setTreePath(relativePath);
        return car;
    }

    public void load(MCar mCar) {
        this.tfName.setText(mCar.getName());
        this.cbModel.getSelectionModel().select(mCar.getModel());
        this.tfMaxSpeed.setText(String.valueOf(mCar.getMaxSpeed()));
        this.tfInitSpeed.setText(String.valueOf(mCar.getInitSpeed()));
        this.cbLocation.getSelectionModel().select(mCar.getLocationType());

        String locationParamName = "";
        for(Node node : this.gridPaneLocationParams.getChildren()) {
            if(node instanceof Label) {
                locationParamName = ((Label) node).getText();
            }
            else if(node instanceof TextField) {
                ((TextField) node).setText(mCar.getLocationParams().get(locationParamName));
            }
        }

        this.cbHeading.getSelectionModel().select(mCar.getHeading() ? "same" : "opposite");
        this.tfRoadDeviation.setText(String.valueOf(mCar.getRoadDeviation()));
        // 恢复绝对路径
        ((ChooseFileButton) this.btDynamic.getUserData()).setFile(new File(this.projectPath, mCar.getTreePath()));
    }

    private void createNode() {
        this.gridPane.setVgap(8);
        this.gridPane.setHgap(8);

        Label lbName = new Label("name: ");
        this.tfName = new TextField();

        Label lbModel = new Label("model: ");
        String[] models = {"random", "vehicle.audi.a2"};
        this.cbModel = new ComboBox<>(FXCollections.observableArrayList(models));
        this.cbModel.getSelectionModel().select(0);

        Label lbMaxSpeed = new Label("max speed: ");
        this.tfMaxSpeed = new TextField();

        Label lbInitSpeed = new Label("initial speed: ");
        this.tfInitSpeed = new TextField();

        Label lbLocation = new Label("location: ");
        List<String> behaviorNames = LocationRegister.getLocationTypes();
        this.cbLocation = new ComboBox<>(FXCollections.observableArrayList(behaviorNames));
        this.gridPaneLocationParams = new GridPane();
        gridPaneLocationParams.setPadding(new Insets(0, 0, 0, 20));
        gridPaneLocationParams.setHgap(8);
        gridPaneLocationParams.setVgap(8);
        cbLocation.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            gridPaneLocationParams.getChildren().clear();

            LinkedHashMap<String, String> paramsInfo = LocationRegister.getParams(newValue);
            // 生成界面
            int row = 0;
            for(Map.Entry<String, String> param : paramsInfo.entrySet()) {
                Label lbParamName = new Label(param.getKey());
                TextField tfParamValue = new TextField();
                gridPaneLocationParams.addRow(row++, lbParamName, tfParamValue);
            }
        });

        Label lbHeading = new Label("heading: ");
        this.cbHeading = new ComboBox<>(FXCollections.observableArrayList("same", "opposite"));
        this.cbHeading.getSelectionModel().select(0);

        Label lbRoadDeviation = new Label("road deviation: ");
        this.tfRoadDeviation = new TextField();

        // TODO 非强制 .tree
        Label lbDynamic = new Label("Dynamic: ");
        this.btDynamic = new ChooseFileButton(this.gridPane, this.projectPath).getNode();

        this.gridPane.addRow(0, lbName, this.tfName);
        this.gridPane.addRow(1, lbModel, this.cbModel);
        this.gridPane.addRow(2, lbMaxSpeed, this.tfMaxSpeed);
        this.gridPane.addRow(3, lbInitSpeed, this.tfInitSpeed);
        this.gridPane.addRow(4, lbLocation, cbLocation);
        this.gridPane.add(gridPaneLocationParams, 0, 5, 2, 1);
        this.gridPane.addRow(6, lbHeading, this.cbHeading);
        this.gridPane.addRow(7, lbRoadDeviation, this.tfRoadDeviation);
        this.gridPane.addRow(8, lbDynamic, this.btDynamic);
    }

    public Node getNode() {
        return this.gridPane;
    }
}
