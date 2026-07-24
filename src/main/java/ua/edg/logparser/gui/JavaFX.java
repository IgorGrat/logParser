package ua.edg.logparser.gui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import transimpex.logParser.TableRowDTO;
import ua.edg.conector.LoginsAccessor;
import ua.edg.logparser.parser.LocalFileRider;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class JavaFX extends Application{

	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

	private final TableColumn<TableRowDTO, LocalDateTime> tableColumnTime = new TableColumn<>("Час");
	private final TableColumn<TableRowDTO, String> tableColumnLogin = new TableColumn<>("Логін");
	private final TableColumn<TableRowDTO, String> tableColumnHost = new TableColumn<>("Хост");
	private final TableColumn<TableRowDTO, String> tableColumnClass = new TableColumn<>("Клас");
	private final TableColumn<TableRowDTO, String> tableColumnMethod = new TableColumn<>("Метод");
	private final TableColumn<TableRowDTO, String> tableColumnParam = new TableColumn<>("Параметри");
	private final TableColumn<TableRowDTO, String> tableColumnResponse = new TableColumn<>("Відповідь");
	private final TableView<TableRowDTO> tableShow = new TableView<>();
	private final TextField hint = new TextField("Ctrl + Click для вибору декількох клітинок, Ctrl + C для копіювання");

	private final ComboBox<String> loginComboBox = new ComboBox<>();
	private final ComboBox<String> methodComboBox = new ComboBox<>();
	private final Button searchButton = new Button("Шукати");
	private final TextField maskField = new TextField();

	private final TextField searchTimeFrom = new TextField("00:00");
	private final TextField searchTimeTo = new TextField("23:59");

	private final DateTimePicker dateTimePickerSince = new DateTimePicker();
	private final DateTimePicker dateTimePickerUntil = new DateTimePicker();

	private LocalDateTime since = LocalDateTime.now();
	private LocalDateTime until = LocalDateTime.now();
	private final List<TableRowDTO> tdoList = new ArrayList<>();

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

		tdoList.addAll(getTDOList(since, until));

		// layout setup
		VBox verticalLayout = new VBox(10);
		verticalLayout.setPadding(new Insets(10));
		verticalLayout.getChildren().addAll(hint, tableShow);
		verticalLayout.getChildren().add(new HBox(10, dateTimePickerSince, searchTimeFrom, dateTimePickerUntil, searchTimeTo, loginComboBox, methodComboBox, maskField, searchButton));

		// buttons and menus params
		hint.setEditable(false);
		hint.setBackground(null);

		loginComboBox.setEditable(true);
		loginComboBox.setPromptText("Логін");

		methodComboBox.setEditable(true);
		methodComboBox.setPromptText("Пошук за методом");

		maskField.setPromptText("Пошук по масці");

		searchTimeFrom.setPromptText("Початок часу");
		searchTimeFrom.setMaxSize(45, 25);
		searchTimeTo.setPromptText("Кінець часу");
		searchTimeTo.setMaxSize(45, 25);

		dateTimePickerSince.setMaxSize(100, 25);
		dateTimePickerUntil.setMaxSize(100, 25);

		dateTimePickerSince.setValue(since.toLocalDate());
		dateTimePickerUntil.setValue(until.toLocalDate());

		// table params
		tableShow.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		tableShow.getSelectionModel().setCellSelectionEnabled(true);
		tableShow.setTableMenuButtonVisible(true);
		tableShow.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

		// columns setup ------------------

		tableColumnTime.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
		tableColumnTime.setCellFactory(_ -> new TableCell<>(){
			@Override
			protected void updateItem(LocalDateTime item, boolean empty){
				super.updateItem(item, empty);
				if(item == null || empty){
					setText(null);
				}
				else{
					setText(formatter.format(item));
				}
			}
		});
		tableColumnTime.setPrefWidth(120);

		tableColumnLogin.setCellValueFactory(new PropertyValueFactory<>("login"));
		tableColumnHost.setCellValueFactory(new PropertyValueFactory<>("host"));

		tableColumnClass.setCellValueFactory(new PropertyValueFactory<>("clazz"));
		tableColumnClass.setCellFactory(_ -> new TableCell<>(){
			@Override
			protected void updateItem(String item, boolean empty){
				super.updateItem(item, empty);
				if(item == null || empty){
					setText(null);
				}
				else{
					setText(item.replace(".", ""));
				}
			}
		});

		tableColumnMethod.setCellValueFactory(new PropertyValueFactory<>("method"));
		tableColumnParam.setCellValueFactory(new PropertyValueFactory<>("param"));
		tableColumnResponse.setCellValueFactory(new PropertyValueFactory<>("serverResponse"));

		// columns setup ------------------
		tableShow.getColumns().addAll(tableColumnTime, tableColumnLogin, tableColumnHost,
				tableColumnClass, tableColumnMethod, tableColumnParam, tableColumnResponse);

		// login ComboBox
		loginComboBoxSetup();
		// method ComboBox
		methodComboBoxSetup();
		// events
//		setDateTimePickerSinceAction();
//		setDateTimePickerUntilAction();
		setLoginComboBoxAction();
		setMethodComboBoxAction();
		setMaskFieldAction();
		setSearchButtonAction();

		// scene/stage setup
		copyFromCellAction();
		Scene scene = new Scene(verticalLayout, 1200, 400);
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

	private void setTableShowItemsByHBOXValues(ActionEvent event){
		Objects.requireNonNull(event);
		if(dateTimePickerSince.getDateTimeValue() != null && dateTimePickerUntil.getDateTimeValue() != null){

			LocalDate startDate = dateTimePickerSince.getDateTimeValue().toLocalDate();
			LocalDate untilDate = dateTimePickerUntil.getDateTimeValue().toLocalDate();
			since = LocalDateTime.of(startDate, LocalTime.parse(searchTimeFrom.getText()));
			until = LocalDateTime.of(untilDate, LocalTime.parse(searchTimeTo.getText()));
			tdoList.clear();
			tdoList.addAll(getTDOList(since, until));
		}
		String[] searchValue = {loginComboBox.getEditor().getText(), methodComboBox.getEditor().getText(), maskField.getText()};
		tableShow.setItems(FXCollections.observableArrayList(tdoList.stream()
				.filter(tableRowDTO ->
						tableRowDTO.getDateTime().toEpochSecond(ZoneOffset.UTC) >= since.toEpochSecond(ZoneOffset.UTC) &&
								tableRowDTO.getDateTime().toEpochSecond(ZoneOffset.UTC) <= until.toEpochSecond(ZoneOffset.UTC))
				.filter(tableRowDTO -> tableRowDTO.getLogin().toLowerCase().contains(searchValue[0].toLowerCase()))
				.filter(tableRowDTO -> tableRowDTO.getMethod().toLowerCase().contains(searchValue[1].toLowerCase()))
				.filter(tableRowDTO -> tableRowDTO.toString().toLowerCase().contains(searchValue[2].toLowerCase()))
				.toList()));
	}

	private void setTableShowItemsByHBOXValues(KeyEvent event){
		if(event.getCode() == KeyCode.ENTER){
			setTableShowItemsByHBOXValues(new ActionEvent());
		}
	}

	private void setSearchButtonAction(){
		searchButton.setOnAction(this::setTableShowItemsByHBOXValues);
	}


	private void setMethodComboBoxAction(){
		methodComboBox.setOnKeyPressed(this::setTableShowItemsByHBOXValues);
	}

	private void setLoginComboBoxAction(){
		loginComboBox.setOnKeyPressed(this::setTableShowItemsByHBOXValues);
	}

	private void setMaskFieldAction(){
		maskField.setOnKeyPressed(this::setTableShowItemsByHBOXValues);
	}

//	private void setDateTimePickerSinceAction(){
//		dateTimePickerSince.setOnAction(event -> {
//			Objects.requireNonNull(event);
//			if(dateTimePickerSince.getDateTimeValue() != null){
//				since = dateTimePickerSince.getDateTimeValue();
//				tdoList.clear();
//				tdoList.addAll(getTDOList(since, until));
//			}
//		});
//	}
//
//	private void setDateTimePickerUntilAction(){
//		dateTimePickerUntil.setOnAction(event -> {
//			Objects.requireNonNull(event);
//			if(dateTimePickerUntil.getDateTimeValue() != null){
//				until = dateTimePickerUntil.getDateTimeValue();
//				tdoList.clear();
//				tdoList.addAll(getTDOList(since, until));
//			}
//		});
//	}


	private void methodComboBoxSetup(){
		List<String> availableMethods = new ArrayList<>();
		for(TableRowDTO tableRowDTO : tdoList){
			if(!availableMethods.contains(tableRowDTO.getMethod())){
				availableMethods.add(tableRowDTO.getMethod());
			}
		}
		methodComboBox.setItems(FXCollections.observableList(availableMethods));
	}

	private void loginComboBoxSetup(){
//		List<String> availableLogins = new ArrayList<>(Objects.requireNonNull(LoginsAccessor.parseLogins()));
//		availableLogins.sort(Comparator.naturalOrder());
//		loginComboBox.setItems(FXCollections.observableArrayList(availableLogins));
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
					cb.append(data != null ? data.toString() : "");
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
		list.removeIf(tableRowDTO -> tableRowDTO.getMethod().equals("getUserAttAction"));
		return list;
	}
}