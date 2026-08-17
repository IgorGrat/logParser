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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import transimpex.logParser.TableRowDTO;
import ua.edg.conector.LoginsAccessor;
import ua.edg.logparser.Models.TableRowDAO;
import ua.edg.logparser.parser.LocalFileRider;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;

public class JavaFX extends Application{

	public static final Set<String> LOGINS = new HashSet<>(LoginsAccessor.getLoginsList());
	public static final Set<String> AVAILABLE_METHODS = new HashSet<>();
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

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

	private final TextField searchTimeSince = new TextField("00:00");
	private final TextField searchTimeUntil = new TextField("23:59");

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

		// layout setup
		VBox verticalLayout = new VBox(10);
		verticalLayout.setPadding(new Insets(10));
		VBox.setVgrow(tableShow, Priority.ALWAYS);
		verticalLayout.getChildren().addAll(hint, tableShow);
		verticalLayout.getChildren().add(new HBox(10, dateTimePickerSince, searchTimeSince, dateTimePickerUntil, searchTimeUntil, loginComboBox, methodComboBox, maskField, searchButton));

		// buttons and menus params
		hint.setEditable(false);
		hint.setBackground(null);

		loginComboBox.setEditable(true);
		loginComboBox.setPromptText("Логін");

		methodComboBox.setEditable(true);
		methodComboBox.setPromptText("Пошук за методом");

		maskField.setPromptText("Пошук по масці");

		searchTimeSince.setPromptText("Початок часу");
		searchTimeSince.setMaxSize(45, 25);
		searchTimeUntil.setPromptText("Кінець часу");
		searchTimeUntil.setMaxSize(45, 25);

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
		loginComboBoxSetupLogins();

		// events
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
			LocalDate sinceDate = dateTimePickerSince.getDateTimeValue().toLocalDate();
			LocalDate untilDate = dateTimePickerUntil.getDateTimeValue().toLocalDate();
			since = LocalDateTime.of(sinceDate, LocalTime.parse(searchTimeSince.getText()));
			until = LocalDateTime.of(untilDate, LocalTime.parse(searchTimeUntil.getText()));
			LOGINS.clear();
			tdoList.clear();
//			tdoList.addAll(getTDOList(since, until));
		}
		TableRowDAO tableRowDAO = new TableRowDAO();

		String login = loginComboBox.getEditor().getText() == null ? "" : loginComboBox.getEditor().getText();
		String method = methodComboBox.getEditor().getText() == null ? "" : methodComboBox.getEditor().getText();
		String mask = maskField.getText() == null ? "" : maskField.getText();
		if(!login.isBlank() && !method.isBlank())
			tdoList.addAll(tableRowDAO.getAllByLoginAndMethod(since, until, login, method));
		else if(!login.isBlank()) tdoList.addAll(tableRowDAO.getAllByLogin(since, until, login));
		else if(!mask.isBlank()) tdoList.addAll(tableRowDAO.getAllByMask(since, until, mask));
		else tdoList.addAll(tableRowDAO.getAllByDateRange(since, until));

		Predicate<TableRowDTO> noLongPackMethod = tableRowDTO ->
				!tableRowDTO.getMethod().contains("longPack");

		List<TableRowDTO> list = tdoList.stream()
				.filter(noLongPackMethod)
				.filter(tableRowDTO -> tableRowDTO.getLogin().toLowerCase().contains(login.toLowerCase()))
				.filter(tableRowDTO -> tableRowDTO.getMethod().toLowerCase().contains(method.toLowerCase()))
				.filter(tableRowDTO -> tableRowDTO.toString().toLowerCase().contains(mask.toLowerCase()))
				.toList();
		methodComboBoxUpdateMethods(list);
		loginComboBoxUpdateLogins(list);
		tableShow.setItems(FXCollections.observableArrayList(list));
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

	private void methodComboBoxUpdateMethods(List<TableRowDTO> tdoList){
		AVAILABLE_METHODS.clear();
		for(TableRowDTO tableRowDTO : tdoList){
			AVAILABLE_METHODS.add(tableRowDTO.getMethod());
		}
		methodComboBox.setItems(FXCollections.observableList(new ArrayList<>(AVAILABLE_METHODS)));
	}

	private void loginComboBoxSetupLogins(){
		ObservableList<String> loginsList = FXCollections.observableArrayList(LoginsAccessor.getLoginsList());
		loginComboBox.setItems(loginsList);
	}

	private void loginComboBoxUpdateLogins(List<TableRowDTO> tdoList){
		LOGINS.clear();
		for(TableRowDTO tableRowDTO : tdoList){
			LOGINS.add(tableRowDTO.getLogin());
		}
		loginComboBox.setItems(FXCollections.observableList(new ArrayList<>(LOGINS)));
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
					else if(lastRow != -1) cb.append("\n");

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