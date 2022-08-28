package com.ecnu.adsmls.views.codepage;

import com.alibaba.fastjson.JSON;
import com.ecnu.adsmls.components.editor.Editor;
import com.ecnu.adsmls.components.editor.modeleditor.ModelEditor;
import com.ecnu.adsmls.components.editor.treeeditor.TreeEditor;
import com.ecnu.adsmls.components.editor.weathereditor.WeatherEditor;
import com.ecnu.adsmls.components.modal.*;
import com.ecnu.adsmls.components.mutileveldirectory.MultiLevelDirectory;
import com.ecnu.adsmls.model.*;
import com.ecnu.adsmls.router.Route;
import com.ecnu.adsmls.router.Router;
import com.ecnu.adsmls.router.params.CodePageParams;
import com.ecnu.adsmls.router.params.Global;
import com.ecnu.adsmls.service.SimulatorService;
import com.ecnu.adsmls.utils.FileSystem;
import com.ecnu.adsmls.utils.SimulatorConstant;
import com.ecnu.adsmls.utils.SimulatorTypeObserver;
import com.ecnu.adsmls.utils.log.MyStaticOutputStreamAppender;
import com.ecnu.adsmls.utils.register.impl.BehaviorRegister;
import com.ecnu.adsmls.utils.register.impl.LocationRegister;
import com.ecnu.adsmls.utils.register.impl.WeatherRegister;
import com.ecnu.adsmls.verifier.Verifier;
import hprose.client.HproseHttpClient;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.*;


public class CodePageController implements Initializable, Route {
    @FXML
    private AnchorPane rootLayout;
    @FXML
    private MenuBar menuBar;
    @FXML
    private StackPane directoryWrapper;
    // 左侧目录
    private MultiLevelDirectory multiLevelDirectory;
    // 目录的上下文菜单
    private List<MenuItem> multiLevelDirectoryMenu = new ArrayList<>();
    @FXML
    private AnchorPane infoPane;
    @FXML
    private TextArea infoArea;

    // 项目配置
    private MConfig mConfig;

    @FXML
    private TabPane tabPane;
    // 正在被访问的文件
    private Map<File, Tab> filesOpened = new HashMap<>();

    // 项目路径
    private String directory;
    // 项目名
    private String projectName;

    private HproseHttpClient client = new HproseHttpClient();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("init");
        new BehaviorRegister().init();
        new LocationRegister().init();
        new WeatherRegister().init();
        this.initMenu();

        OutputStream os = new TextAreaOutputStream(this.infoArea);
        MyStaticOutputStreamAppender.setStaticOutputStream(os);
    }

    @Override
    public void loadParams() {
        this.directory = CodePageParams.directory;
        this.projectName = CodePageParams.projectName;
        // 更新页面
        Global.clear();
        this.updateProject();
        this.infoArea.clear();
    }

    private void initMenu() {
        this.initMenuBar();
        this.initMultiLevelDirectoryMenu();
    }

    private void initMenuBar() {
        Menu fileMenu = new Menu("File");

        Menu newMenu = new Menu("New");
        MenuItem newDirectory = new MenuItem("Directory");
        newDirectory.setOnAction(e -> {
            this.newFile(FileSystem.Suffix.DIR.value);
        });
        MenuItem newModel = new MenuItem("Model");
        newModel.setOnAction(e -> {
            this.newFile(FileSystem.Suffix.MODEL.value);
        });
        MenuItem newTree = new MenuItem("Tree");
        newTree.setOnAction(e -> {
            this.newFile(FileSystem.Suffix.TREE.value);
        });
        MenuItem newWeather = new MenuItem("Weather");
        newWeather.setOnAction(e -> {
            this.newFile(FileSystem.Suffix.WEATHER.value);
        });
        newMenu.getItems().addAll(newDirectory, newModel, newTree, newWeather);

        // 设置
        MenuItem setting = new MenuItem("Settings");
        setting.setOnAction(e -> {
            SettingsModal sm = new SettingsModal(this.mConfig);
            sm.getWindow().showAndWait();
            if (!sm.isConfirm()) {
                return;
            }
            // 写入配置文件
            this.mConfig.setSimulatorType(Global.simulatorType.value);
            this.mConfig.setSimulationPort(Global.simulationPort);

            String config = JSON.toJSONString(mConfig);
            System.out.println(config);
            String projectPath = FileSystem.concatAbsolutePath(this.directory, this.projectName);
            FileSystem.JSONWriter(new File(FileSystem.concatAbsolutePath(projectPath, ".adsml"), "config" + FileSystem.Suffix.JSON.value), config);

            this.notifyEditors();
        });

        MenuItem closeProject = new MenuItem("Close Project");
        closeProject.setOnAction(e -> {
            // TODO 重置界面
            this.tabPane.getTabs().clear();
            this.filesOpened.clear();
            Router.back();
        });

        fileMenu.getItems().addAll(newMenu, setting, closeProject);

        Menu editMenu = new Menu("Edit");
        MenuItem delete = new MenuItem("Delete");
        editMenu.getItems().add(delete);

        Menu helpMenu = new Menu("Help");

        this.menuBar.getMenus().addAll(fileMenu, editMenu, helpMenu);
    }

    private void initMultiLevelDirectoryMenu() {
        Menu newMenu = new Menu("New");
        MenuItem newDirectory = new MenuItem("Directory");
        newDirectory.setOnAction(e -> {
            this.newFile(FileSystem.Suffix.DIR.value);
        });
        MenuItem newModel = new MenuItem("Model");
        newModel.setOnAction(e -> {
            this.newFile(FileSystem.Suffix.MODEL.value);
        });
        MenuItem newTree = new MenuItem("Tree");
        newTree.setOnAction(e -> {
            this.newFile(FileSystem.Suffix.TREE.value);
        });
        newMenu.getItems().addAll(newDirectory, newModel, newTree);

        MenuItem delete = new MenuItem("Delete");
        delete.setOnAction(e -> {
            this.deleteFile();
        });

        this.multiLevelDirectoryMenu.add(newMenu);
        this.multiLevelDirectoryMenu.add(delete);
    }

    // 加载环境配置
    private void loadEnvConfig() {
        System.out.println("load env configurations");
        File dir = new File(FileSystem.concatAbsolutePath(this.directory, this.projectName), ".adsml");
        if (!dir.exists()) {
            FileSystem.createDir(dir.getAbsolutePath());
            FileSystem.createFile(dir, "config" + FileSystem.Suffix.JSON.value);
            this.mConfig = new MConfig(Global.simulatorType.value, Global.simulationPort);
            FileSystem.JSONWriter(new File(dir, "config" + FileSystem.Suffix.JSON.value), JSON.toJSONString(this.mConfig));
        }
        String config = FileSystem.JSONReader(new File(dir, "config" + FileSystem.Suffix.JSON.value));
        this.mConfig = JSON.parseObject(config, MConfig.class);
        if (this.mConfig == null) {
            this.mConfig = new MConfig();
            return;
        }
        Global.simulatorType = SimulatorConstant.getSimulatorTypeByValue(this.mConfig.getSimulatorType());
        Global.simulationPort = this.mConfig.getSimulationPort();
    }

    private void updateProject() {
        System.out.println("Initialize Project");

        // 新建并加载 .adsml/
        this.loadEnvConfig();

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        AnchorPane anchorPane = new AnchorPane();

        // 重置左侧目录
        this.multiLevelDirectory = new MultiLevelDirectory(new File(this.directory + '/' + this.projectName));
        this.multiLevelDirectory.setMenu(this.multiLevelDirectoryMenu);

        this.multiLevelDirectory.getTreeView().setOnMouseClicked(e -> {
            TreeItem<File> selectedItem = this.multiLevelDirectory.getTreeView().getSelectionModel().getSelectedItem();
            if (selectedItem == null || !selectedItem.getValue().isFile()) {
                return;
            }
            if (e.getClickCount() == 2) {
                String suffix = FileSystem.getSuffix(selectedItem.getValue());
                this.openFile(selectedItem.getValue(), suffix);
            }
        });

        anchorPane.getChildren().add(this.multiLevelDirectory.getNode());
        AnchorPane.setTopAnchor(this.multiLevelDirectory.getNode(), 0.0);
        AnchorPane.setRightAnchor(this.multiLevelDirectory.getNode(), 0.0);
        AnchorPane.setBottomAnchor(this.multiLevelDirectory.getNode(), 0.0);
        AnchorPane.setLeftAnchor(this.multiLevelDirectory.getNode(), 0.0);

        scrollPane.setContent(anchorPane);
        this.directoryWrapper.getChildren().add(scrollPane);
    }

    // Global.simulatorType 改变时通知 Editor
    private void notifyEditors() {
        for(Map.Entry<File, Tab> entry: this.filesOpened.entrySet()) {
            Editor editor = (Editor) entry.getValue().getUserData();
            if(editor instanceof SimulatorTypeObserver) {
                ((SimulatorTypeObserver) editor).updateSimulatorType();
            }
        }
    }

    private void showInfo(String info) {
        this.infoArea.clear();
        this.infoArea.setText(info);
    }

    private void appendInfo(String info) {
//        StringBuilder text = new StringBuilder(this.infoArea.getText());
//        text.append('\n');
//        text.append(info);
//        this.showInfo(text.toString());
        this.infoArea.appendText(info);
    }

    // 将 model 和 tree 拼在一起
    @FXML
    protected void preprocess() {
        System.out.println("preprocessing");
        boolean modelOpened = true; // 是否打开了 model 文件
        if (this.tabPane.getTabs().size() == 0) {
            modelOpened = false;
        }
        // 当前显示的 tab
        Editor editor = (Editor) this.tabPane.getSelectionModel().getSelectedItem().getUserData();
        File file = editor.getFile();
        if (!Objects.equals(FileSystem.getSuffix(file), FileSystem.Suffix.MODEL.value)) {
            modelOpened = false;
        }

        if (!modelOpened) {
            this.showInfo("Please open model files first");
            return;
        }

        try {
            editor.check();
        } catch (Exception e) {
            this.showInfo(e.getMessage());
            return;
        }
        this.infoArea.clear();


        String model = FileSystem.JSONReader(file);
        MModel mModel = JSON.parseObject(model, MModel.class);
        if (mModel == null) {
            return;
        }
        System.out.println(model);

        String projectPath = FileSystem.concatAbsolutePath(this.directory, this.projectName);
        if(Objects.equals(mModel.getWeatherType(), "custom")) {
            String weatherPath = mModel.getWeather();
            String weather = FileSystem.JSONReader(new File(projectPath, weatherPath));
            MWeather mWeather = JSON.parseObject(weather, MWeather.class);
            if (mWeather == null) {
                return;
            }
            System.out.println(weather);
            mModel.setMWeather(mWeather);
        }

        for (MCar mCar : mModel.getCars()) {
            String treePath = mCar.getTreePath();
            MTree mTree = null;
            if (!treePath.isEmpty()) {
                String tree = FileSystem.JSONReader(new File(projectPath, treePath));
                mTree = JSON.parseObject(tree, MTree.class);
                if (mTree == null) {
                    return;
                }
                System.out.println(tree);
            }
            if (!mTree.getErrMsg().isEmpty()) {
                this.showInfo(mTree.getErrMsg());
                return;
            }
            mCar.setMTree(mTree);
        }

        model = JSON.toJSONString(mModel);
        System.out.println(model);
        String outFilename = FileSystem.removeSuffix(file) + FileSystem.Suffix.ADSML.value;
        FileSystem.JSONWriter(new File(outFilename), model);
        // 更新同级目录
        this.multiLevelDirectory.updateSameLevel(file);
    }

    @FXML
    private void verify() {
        System.out.println("verifying");

        if (this.tabPane.getTabs().size() == 0) {
            this.showInfo("Please open model files first");
            return;
        }
        // 当前显示的 tab
        File file = ((Editor) this.tabPane.getSelectionModel().getSelectedItem().getUserData()).getFile();

        String model = FileSystem.JSONReader(file);
        MModel mModel = JSON.parseObject(model, MModel.class);
        if (mModel == null) {
            return;
        }
        System.out.println(model);

        // 用于验证的 requirements
        VerifyRequirementsModal vrm = new VerifyRequirementsModal(mModel);
        vrm.getWindow().showAndWait();
        if (!vrm.isConfirm()) {
            return;
        }

        this.infoArea.clear();

        List<String> requirements = vrm.getRequirements();
        System.out.println(requirements);

        // 存储 requirements
        mModel.setRequirements(requirements);

        model = JSON.toJSONString(mModel);
        System.out.println(model);
        FileSystem.JSONWriter(file, model);

        String projectPath = FileSystem.concatAbsolutePath(this.directory, this.projectName);
        String outputPath = vrm.getOutputPath();
        if (outputPath.isEmpty()) {
            outputPath = FileSystem.removeSuffix(file.getAbsolutePath());
        }
        File outputFile = new File(outputPath);
        if (!outputFile.isAbsolute()) {
            outputPath = FileSystem.concatAbsolutePath(file.getParent(), outputPath);
        }
        System.out.println(outputPath);
        if (!Objects.equals(FileSystem.getSuffix(outputPath), FileSystem.Suffix.XML.value)) {
            outputPath = outputPath + FileSystem.Suffix.XML.value;
        }
        // TODO 所有的 log 都会在该函数完成时全部出现，可能得开个线程来 log
        Verifier.verify(new String[]{projectPath + File.separator,
                FileSystem.getRelativePath(projectPath, FileSystem.removeSuffix(file) + FileSystem.Suffix.ADSML.value),
                outputPath});

        // 更新同级目录
        this.multiLevelDirectory.updateSameLevel(file);
    }

    // 仿真
    @FXML
    private void simulate() {
        System.out.println("simulating");

        boolean modelOpened = true; // 是否打开了 model 文件
        if (this.tabPane.getTabs().size() == 0) {
            modelOpened = false;
        }
        // 当前显示的 tab
        Editor editor = (Editor) this.tabPane.getSelectionModel().getSelectedItem().getUserData();
        File file = editor.getFile();
        if (!Objects.equals(FileSystem.getSuffix(file), FileSystem.Suffix.MODEL.value)) {
            modelOpened = false;
        }

        if (!modelOpened) {
            this.showInfo("Please open model files first");
            return;
        }

        // 模拟选项
        SimulateModal sm = new SimulateModal();
        sm.getWindow().showAndWait();
        if (!sm.isConfirm()) {
            return;
        }

        this.infoArea.clear();

        Map<String, String> params = new HashMap<>();
        params.put("path", FileSystem.removeSuffix(file) + FileSystem.Suffix.ADSML.value);
        if (sm.isScene()) {
            params.put("scene", sm.getScenarioNum());
        }

        this.client.close();
        this.client = new HproseHttpClient();
        client.setKeepAlive(false);
        client.useService("http://127.0.0.1:" + Global.simulationPort + "/RPC");
        System.out.println("Connecting to port " + Global.simulationPort);

        SimulatorService service = client.useService(SimulatorService.class);
        service.run(params);
        System.out.println("Simulation finished");
    }

    private void openFile(File file, String suffix) {
        if (this.filesOpened.containsKey(file)) {
            System.out.println("This file has already been opened");
            // 选择对应的 tab
            for (Tab tab : this.tabPane.getTabs()) {
                if (Objects.equals(file, ((Editor) tab.getUserData()).getFile())) {
                    this.tabPane.getSelectionModel().select(tab);
                    break;
                }
            }
            return;
        }

        Tab tab = new Tab(file.getName());
        tab.setOnClosed(e -> {
            System.out.println(tab.getText() + " closed");
            // 关闭 tab 自动保存
            ((Editor) tab.getUserData()).save();
            this.filesOpened.remove(file);
        });

        if (Objects.equals(suffix, FileSystem.Suffix.MODEL.value)) {
            this.openModel(tab, file);
        } else if (Objects.equals(suffix, FileSystem.Suffix.TREE.value)) {
            this.openTree(tab, file);
        }
        else if (Objects.equals(suffix, FileSystem.Suffix.WEATHER.value)) {
            this.openWeather(tab, file);
        } else {
            this.showInfo("Unsupported file");
            return;
        }

        this.tabPane.getTabs().add(tab);
        // 选择该 tab
        this.tabPane.getSelectionModel().select(tab);
        this.filesOpened.put(file, tab);
    }

    private void newFile(String suffix) {
        NewFileModal nfm;
        if (Objects.equals(suffix, FileSystem.Suffix.MODEL.value)) {
            nfm = new NewModelModal();
        } else if (Objects.equals(suffix, FileSystem.Suffix.TREE.value)) {
            nfm = new NewTreeModal();
        } else if (Objects.equals(suffix, FileSystem.Suffix.WEATHER.value)) {
            nfm = new NewWeatherModal();
        } else if (Objects.equals(suffix, FileSystem.Suffix.DIR.value)) {
            nfm = new NewDirectoryModal();
        } else {
            this.showInfo("Unsupported file");
            return;
        }

        nfm.setDirectory(this.multiLevelDirectory.getTreeView().getFocusModel().getFocusedItem().getValue());
        nfm.getWindow().showAndWait();
        if (!nfm.isConfirm()) {
            return;
        }
        if (!nfm.isSucceed()) {
            this.showInfo("File or directory already exists");
            return;
        }
        this.multiLevelDirectory.newFile();

        if (!Objects.equals(suffix, FileSystem.Suffix.DIR.value)) {
            this.openFile(new File(nfm.getDirectory(), nfm.getFilename() + suffix), suffix);
        }
    }

    /**
     * 删除文件夹时递归关闭涉及到的已经打开的文件
     *
     * @param file 文件/文件夹
     */
    private void closeFile(File file) {
        if (file.isFile()) {
            if (this.filesOpened.containsKey(file)) {
                System.out.println("opened");
                Tab tab = this.filesOpened.get(file);
                Event.fireEvent(tab, new Event(Tab.CLOSED_EVENT));
                this.tabPane.getTabs().remove(tab);
            }
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    this.closeFile(f);
                }
            }
        }
    }

    private void deleteFile() {
        System.out.println("delete");
        TreeItem<File> itemDeleted = this.multiLevelDirectory.getTreeView().getFocusModel().getFocusedItem();

        File file = itemDeleted.getValue();

        // 如果文件已被打开，先将其关闭（递归关闭）
        this.closeFile(file);

        if (file.isFile()) {
            System.out.println("file " + FileSystem.deleteFile(file));
        } else if (file.isDirectory()) {
            System.out.println("dir " + FileSystem.deleteDirectory(file));
        }

        this.multiLevelDirectory.deleteFile(itemDeleted);
    }

    private void openModel(Tab tab, File file) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        String projectPath = FileSystem.concatAbsolutePath(this.directory, this.projectName);
        ModelEditor modelEditor = new ModelEditor(projectPath, file);
        modelEditor.load();

        scrollPane.setContent(modelEditor.getNode());
        scrollPane.setUserData(modelEditor);

        tab.setContent(scrollPane);
        tab.setUserData(modelEditor);
    }

    // TODO refactor 和 openModel 合并
    private void openWeather(Tab tab, File file) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        String projectPath = FileSystem.concatAbsolutePath(this.directory, this.projectName);
        WeatherEditor weatherEditor = new WeatherEditor(projectPath, file);
        weatherEditor.load();

        scrollPane.setContent(weatherEditor.getNode());
        scrollPane.setUserData(weatherEditor);

        tab.setContent(scrollPane);
        tab.setUserData(weatherEditor);
    }

    private void openTree(Tab tab, File file) {
        AnchorPane anchorPane = new AnchorPane();

        String projectPath = FileSystem.concatAbsolutePath(this.directory, this.projectName);
        TreeEditor treeEditor = new TreeEditor(projectPath, file);
        treeEditor.load();
        Node node = treeEditor.getNode();
        ((SplitPane) node).prefWidthProperty().bind(anchorPane.widthProperty());
        ((SplitPane) node).prefHeightProperty().bind(anchorPane.heightProperty());

        anchorPane.getChildren().add(node);

        tab.setContent(anchorPane);
        tab.setUserData(treeEditor);
    }

    /**
     * 用于将 log 输出到 InfoArea
     */
    private static class TextAreaOutputStream extends OutputStream {

        private TextArea textArea;

        public TextAreaOutputStream(TextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(int b) throws IOException {
            textArea.appendText(String.valueOf((char) b));
        }
    }
}