package com.example.antioteldemo.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;

@AllArgsConstructor
@Getter
@Setter
public class KeyboardMessage {

    private long chatId;
    private String text;
    private List<LinkedHashMap<String, String>> keyboardMessage;

    public SendMessage prepareKeyboardMessage() {

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine;
        InlineKeyboardButton button;

        for (LinkedHashMap<String, String> row : keyboardMessage) {
            rowInLine = new ArrayList<>();
            for (var values : row.entrySet()) {
                button = new InlineKeyboardButton();
                button.setCallbackData(values.getKey());
                button.setText(values.getValue());
                rowInLine.add(button);
            }
            rowsInLine.add(rowInLine);
        }

        markupInLine.setKeyboard(rowsInLine);

        SendMessage message = new SendMessage();
        message.setText(text);
        message.setChatId(chatId);
        message.setReplyMarkup(markupInLine);

        return message;
    }

}
