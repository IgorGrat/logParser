package ua.edg.logparser.gui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.DatePicker;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateTimePicker extends DatePicker {

    private final ObjectProperty<LocalDateTime> dateTimeValue = new SimpleObjectProperty<>(LocalDateTime.now());

    public DateTimePicker() {
        this(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    public DateTimePicker(DateTimeFormatter formatter) {

        setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate value) {
                LocalDateTime dateTime = dateTimeValue.get();
                return dateTime != null ? dateTime.format(formatter) : "";
            }

            @Override
            public LocalDate fromString(String value) {
                if (value == null || value.isEmpty()) {
                    dateTimeValue.set(null);
                    return null;
                }

                dateTimeValue.set(LocalDateTime.parse(value, formatter));
                return dateTimeValue.get().toLocalDate();
            }
        });

        valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                dateTimeValue.set(null);
            } else {
                if (dateTimeValue.get() == null) {
                    dateTimeValue.set(LocalDateTime.of(newValue, LocalTime.now()));
                } else {
                    LocalTime time = dateTimeValue.get().toLocalTime();
                    dateTimeValue.set(LocalDateTime.of(newValue, time));
                }
            }
        });

        dateTimeValue.addListener((observable, oldValue, newValue) -> {
            setValue(newValue != null ? newValue.toLocalDate() : null);
        });

        getEditor().focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                simulateEnterPressed();
            }
        });
    }

    private void simulateEnterPressed() {
        getEditor().fireEvent(new KeyEvent(
                getEditor(),
                getEditor(),
                KeyEvent.KEY_PRESSED,
                "",
                "",
                KeyCode.ENTER,
                false,
                false,
                false,
                false
        ));
    }

    public ObjectProperty<LocalDateTime> dateTimeValueProperty() {
        return dateTimeValue;
    }

    public LocalDateTime getDateTimeValue() {
        return dateTimeValue.get();
    }

    public void setDateTimeValue(LocalDateTime value) {
        dateTimeValue.set(value);
    }
}
