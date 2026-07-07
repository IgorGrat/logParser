package ua.edg.logparser.gui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import transimpex.logParser.TableRowDTO;
import ua.edg.conector.LoginsAccessor;
import ua.edg.logparser.parser.LocalFileRider;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;

public class JavaFX extends Application{
  private final TableColumn<TableRowDTO, String> tableColumnTime = new TableColumn<>("Час");
  private final TableColumn<TableRowDTO, String> tableColumnLogin = new TableColumn<>("Логін");
  private final TableColumn<TableRowDTO, String> tableColumnHost = new TableColumn<>("Хост");
  //    private TableColumn<TableRowDTO, Integer> tableColumnSession = new TableColumn<>("Session");
  private final TableColumn<TableRowDTO, String> tableColumnClass = new TableColumn<>("Клас");
  private final TableColumn<TableRowDTO, String> tableColumnMethod = new TableColumn<>("Метод");
  private final TableColumn<TableRowDTO, String> tableColumnParam = new TableColumn<>("Параметри");
  private final TableColumn<TableRowDTO, String> tableColumnResponse = new TableColumn<>("Відповідь");
  private final TableView<TableRowDTO> tableShow = new TableView<>();
  private final TextField hint = new TextField("Ctrl + Click для вибору декількох клітинок, Ctrl + C для копіювання");

  private final ComboBox<String> searchByLogin = new ComboBox<>();
  private final Button search = new Button("Шукати");
  private final TextField method = new TextField();

  private final TextField searchTimeFrom = new TextField("00:00");
  private final TextField searchTimeTo = new TextField("23:59");

  private final DateTimePicker dateTimePickerSince = new DateTimePicker();
  private final DateTimePicker DateTimePickerUntil = new DateTimePicker();

  private LocalDateTime since = LocalDateTime.now().minusDays(1);
  private LocalDateTime until = LocalDateTime.now();

  /**
   * Starts the JavaFX application by initializing and setting up the primary stage,
   * including the layout, table configuration, user input fields, and event handling.
   *
   * @param primaryStage the primary stage for the application, provided by the JavaFX runtime.
   */
  @Override
  public void start(Stage primaryStage){
    List<String> rawArgs = getParameters().getRaw();
    if(rawArgs != null && !rawArgs.isEmpty()){
      Panel.PATH = rawArgs.getFirst();
    }
    else return;
    // layout setup
    VBox verticalLayout = new VBox(10);
    verticalLayout.setPadding(new Insets(10));
    verticalLayout.getChildren().addAll(hint, tableShow);
    verticalLayout.getChildren().add(new HBox(10, dateTimePickerSince, searchTimeFrom, DateTimePickerUntil, searchTimeTo, searchByLogin, method, search));

    // buttons and menus params
    hint.setEditable(false);
    hint.setBackground(null);
    searchByLogin.setPromptText("Логін");
    method.setPromptText("Метод для пошуку");
    searchTimeFrom.setPromptText("Початок часу");
    searchTimeFrom.setMaxSize(45, 25);
    searchTimeTo.setPromptText("Кінець часу");
    searchTimeTo.setMaxSize(45, 25);
    dateTimePickerSince.setMaxSize(100, 25);
    DateTimePickerUntil.setMaxSize(100, 25);
    dateTimePickerSince.setValue(since.toLocalDate());
    DateTimePickerUntil.setValue(until.toLocalDate());

    // table params
    tableShow.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    tableShow.getSelectionModel().setCellSelectionEnabled(true);
    tableShow.setTableMenuButtonVisible(true);
    tableShow.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

    // table hydration with yesterday period
    ObservableList<TableRowDTO> tdoList = FXCollections.observableArrayList(getTDOList(since, until));
    tableShow.setItems(tdoList);

    // columns setup ------------------
    tableColumnTime.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
    tableColumnLogin.setCellValueFactory(new PropertyValueFactory<>("login"));
    tableColumnHost.setCellValueFactory(new PropertyValueFactory<>("host"));
//        tableColumnSession.setCellValueFactory(new PropertyValueFactory<>("session"));
    tableColumnClass.setCellValueFactory(new PropertyValueFactory<>("clazz"));
    tableColumnMethod.setCellValueFactory(new PropertyValueFactory<>("method"));
    tableColumnParam.setCellValueFactory(new PropertyValueFactory<>("param"));
    tableColumnResponse.setCellValueFactory(new PropertyValueFactory<>("serverResponse"));

    // columns setup ------------------
    //noinspection unchecked
    tableShow.getColumns().addAll(tableColumnTime, tableColumnLogin, tableColumnHost,
        tableColumnClass, tableColumnMethod, tableColumnParam, tableColumnResponse);

    // login dropMenu
    searchByLogin.setEditable(true);
    List<String> availableLogins = new ArrayList<>(LoginsAccessor.parseLogins());
    availableLogins.add("Все");
    availableLogins.add(availableLogins.getFirst().replace("(logins:", ""));
    availableLogins.removeFirst();
    availableLogins.removeLast();
    availableLogins.sort(Comparator.naturalOrder());
    searchByLogin.setItems(FXCollections.observableArrayList(availableLogins));
    searchByLogin.setEditable(true);
    this.searchByLogin.setValue("Все");

    // search press event
    searchButtonAction(tdoList);


    // tooltip over cell
//        tableShow.setOnMouseClicked(event -> {
//            if (event.getClickCount() == 1) {
//                TablePosition<?, ?> pos = tableShow.getSelectionModel().getSelectedCells().getFirst();
//                Object data = pos.getTableColumn().getCellData(pos.getRow());
//                Tooltip tooltip = new Tooltip(data != null ? data.toString() : "");
//                tooltip.show(tableShow, event.getScreenX(), event.getScreenY());
//            }
//        });

    copyFromCellAction();
    Scene scene = new Scene(verticalLayout, 1000, 400);
    try{
      scene.getStylesheets().add("style.css");
    }
    catch(NullPointerException e){
      System.err.println("Could not load stylesheet: " + e.getMessage());
    }


    primaryStage.setScene(scene);
    primaryStage.setTitle("Log Parser");
    primaryStage.show();
  }

  private void searchButtonAction(ObservableList<TableRowDTO> tdoList){
    search.setOnAction(event -> {
      try{
        tdoList.clear();

        // basic pass tests
        if(since.isAfter(until)){
          Alert alert = new Alert(Alert.AlertType.ERROR, "Початок часу не може бути пізніше кінця часу");
          alert.showAndWait();
          return;
        }
        if(searchTimeFrom.getText().isEmpty() || searchTimeFrom.getText().isBlank()){
          searchTimeFrom.setText("00:00");
        }
        if(searchTimeTo.getText().isEmpty() || searchTimeTo.getText().isBlank()) searchTimeTo.setText("23:59");

        // local variables
        since = LocalDateTime.of(dateTimePickerSince.getValue(), LocalTime.parse(searchTimeFrom.getText()));
        until = LocalDateTime.of(DateTimePickerUntil.getValue(), LocalTime.parse(searchTimeTo.getText()));

        // display logic
        if(searchByLogin.getValue().equals("Все") &&
            method.getText().isEmpty()){
          tdoList.addAll(getTDOList(since, until));
          tableShow.setItems(tdoList);

        }
        else if(method.getText().isEmpty()){

          tdoList.addAll(getTDOList(since, until));
          tableShow.setItems(FXCollections.observableArrayList(tdoList.stream()
              .filter(tdo -> tdo.getLogin().contains(searchByLogin.getValue()))
              .toList()));

        }
        else{
          tdoList.addAll(getTDOList(since, until));
          tableShow.setItems(tdoList);
        }
      }
      catch(DateTimeParseException e){
        Alert alert = new Alert(Alert.AlertType.ERROR, "Невірний формат часу: ");
        alert.showAndWait();
      }
    });
  }

  private void copyFromCellAction(){
    tableShow.setOnKeyPressed(event -> {
      if(event.isControlDown() && event.getCode() == KeyCode.C){
        StringBuilder cb = new StringBuilder();
        ObservableList<TablePosition> cells = tableShow.getSelectionModel().getSelectedCells();

        // Loop through cells and format as tab-separated values
        int lastRow = -1;
        for(TablePosition<?, ?> pos : cells){
          if(lastRow != -1 && lastRow != pos.getRow()) cb.append("\n");
          else if(lastRow != -1) cb.append("\t");

          Object data = pos.getTableColumn().getCellData(pos.getRow());
          cb.append(data != null? data.toString() : "");
          lastRow = pos.getRow();
        }

        // Copy to clipboard
        ClipboardContent content = new ClipboardContent();
        content.putString(cb.toString());
        Clipboard.getSystemClipboard().setContent(content);
      }
    });
  }

  private List<TableRowDTO> getTDOList(LocalDateTime parsedDateTimeFrom, LocalDateTime parsedDateTimeTo){
    List<TableRowDTO> list = new ArrayList<>();
    new LocalFileRider(parsedDateTimeFrom, parsedDateTimeTo){
      @Override
      protected void addItemToScope(TableRowDTO rowDTO){
        if(Objects.nonNull(rowDTO)) list.add(rowDTO);
      }
    }.doAction();
    return list;
  }
}
