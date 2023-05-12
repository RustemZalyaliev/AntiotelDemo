package com.example.antioteldemo.service;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static com.example.antioteldemo.config.OptionPattern.*;
import static com.example.antioteldemo.service.TimeMenu.getTwoDigits;

@Component
@Scope("prototype")
public class DateMenu {

    @Autowired
    private TimeMenu timeMenu;

    public LinkedHashMap<SendMessage, Integer> handleFirstDateMenu(CallbackQuery callbackQuery) {

        LinkedHashMap<SendMessage, Integer> answerData = new LinkedHashMap<>();
        SendMessage answerMessage = new SendMessage();
        int methodNumber = 0;
        Message messageIn = callbackQuery.getMessage();
        int messageId = messageIn.getMessageId();
        answerMessage.setMessageThreadId(messageId);
        long chatId = messageIn.getChatId();
        answerMessage.setChatId(chatId);
        String callbackData = callbackQuery.getData();
        LocalDate dateFrom;

        switch (callbackData) {

            case TODAY -> {
                currentOrders.get(chatId).setMonthOfBooking(LocalDate.now().getMonthValue());
                dateFrom = LocalDate.now();
                currentOrders.get(chatId).setDateFrom(dateFrom);
                answerData = prepareTimeMenu(answerMessage, dateFrom);
                answerMessage.setText(EmojiParser.parseToUnicode("Смотрим сегодня и завтра :arrow_down:"));
                methodNumber = 2;
            }

            case TOMORROW -> {
                currentOrders.get(chatId).setMonthOfBooking(LocalDate.now().getMonthValue());
                dateFrom = LocalDate.now().plusDays(1);
                currentOrders.get(chatId).setDateFrom(dateFrom);
                answerData = prepareTimeMenu(answerMessage, dateFrom);
                answerMessage.setText(EmojiParser.parseToUnicode("Смотрим завтра и послезавтра :arrow_down:"));
                methodNumber = 2;
            }

            case CURRENT_MONTH -> {
                currentOrders.get(chatId).setMonthOfBooking(LocalDate.now().getMonthValue());
                answerMessage.setText(LOOK_CURRENT_MONTH);
                answerData.put(answerMessage, 2);
                dateFrom = LocalDate.now().plusDays(2);

                if (!dateFrom.getMonth().equals(LocalDate.now().getMonth())) {
                    dateFrom = getDateFromForNextMonthMenu(chatId);
                    answerMessage.setText(LOOK_NEXT_MONTH);
                }
                answerMessage = getMonthCalendar(chatId, dateFrom);
                methodNumber = 1;
            }

            case NEXT_MONTH -> {

                answerMessage.setText(LOOK_NEXT_MONTH);
                answerData.put(answerMessage, 2);

                dateFrom = getDateFromForNextMonthMenu(chatId);
                answerMessage = getMonthCalendar(chatId, dateFrom);
                methodNumber = 1;

            }
        }

        answerData.put(answerMessage, methodNumber);

        return answerData;
    }

    public LocalDate getDateFromForNextMonthMenu(Long chatId) {

        LocalDate now = LocalDate.now();
        int nextMonth = now.getMonthValue() + 1;
        currentOrders.get(chatId).setMonthOfBooking(nextMonth);
        String strNextMonth = getTwoDigits(nextMonth);

        return LocalDate.parse(now.getYear() + "-" + strNextMonth + "-01");
    }

    public LinkedHashMap<SendMessage, Integer> prepareTimeMenu(SendMessage answerMessage, LocalDate dateFrom) {

        LinkedHashMap<SendMessage, Integer> answerData = new LinkedHashMap<>();
        List<SendMessage> twoDays;
        Long chatId = Long.valueOf(answerMessage.getChatId());
        twoDays = timeMenu.getTimeMenu(dateFrom, chatId);
        int counter = 0;
        for (SendMessage sm : twoDays) {
            ++counter;
            sm.setChatId(chatId);
            sm.setProtectContent(counter == 1);
            answerData.put(sm, 1);
        }

        int profit = currentOrders.get(chatId).getProfit() == null ? 0 : currentOrders.get(chatId).getProfit();
        int hourNumber = currentOrders.get(chatId).getHours() == null ? 0 : currentOrders.get(chatId).getHours();

        SendMessage calcPanel = timeMenu.getCalcPanel(chatId, dateFrom, profit, hourNumber);
        calcPanel.setDisableNotification(true);

        answerData.put(calcPanel, 1);

        SendMessage doneBigButton = DONE_BIG_BUTTON;
        doneBigButton.setChatId(chatId);

        answerData.put(doneBigButton, 1);

        currentOrders.get(chatId).setFirstTimeMenu(true);

        return answerData;
    }

    public LinkedHashMap<SendMessage, Integer> handleDateDigit(CallbackQuery callbackQuery, int intDateFrom) {

        LinkedHashMap<SendMessage, Integer> answerData = new LinkedHashMap<>();
        SendMessage answerMessage = new SendMessage();
        Message messageIn = callbackQuery.getMessage();
        int messageId = messageIn.getMessageId();
        answerMessage.setMessageThreadId(messageId);
        long chatId = messageIn.getChatId();
        answerMessage.setChatId(chatId);
        String callbackData = callbackQuery.getData();

        int intMonthFrom = currentOrders.get(chatId).getMonthOfBooking();
        int yearFrom = intMonthFrom < LocalDate.now().getMonthValue() ? LocalDate.now().getYear() + 1 : LocalDate.now().getYear();
        String monthFrom = intMonthFrom < 10 ? "0" + intMonthFrom : String.valueOf(intMonthFrom);
        String dayFrom = intDateFrom < 10 ? "0" + callbackData : callbackData;

        LocalDate dateFrom = LocalDate.parse(yearFrom + "-" + monthFrom + "-" + dayFrom);
        currentOrders.get(chatId).setDateFrom(dateFrom);

        answerMessage.setText(EmojiParser.parseToUnicode(":calendar: Смотрим "
                + convertLocalDateToString(dateFrom, true, false) + " и "
                + convertLocalDateToString(dateFrom.plusDays(1), true, false)
                + " :arrow_down:"));
        answerData.put(answerMessage, 2);

        answerMessage = new SendMessage();
        answerMessage.setChatId(chatId);

        answerData.putAll(prepareTimeMenu(answerMessage, dateFrom));

        return answerData;
    }

    public SendMessage getMonthCalendar(Long chatId, LocalDate dateFrom) {

        String textToSend = EmojiParser.parseToUnicode("Выберите нужную Вам дату " + MONTHS.get(dateFrom.getMonth()) + ": :date:");

        LinkedHashMap<String, String> values = new LinkedHashMap<>();
        List<LinkedHashMap<String, String>> rows = new ArrayList<>();
        String dateDigit;
        int counter = 0;

        for (int i = dateFrom.getDayOfMonth(); i <= dateFrom.lengthOfMonth(); i++) {
            ++counter;
            dateDigit = String.valueOf(i);
            values.put(dateDigit, dateDigit);
            if (counter % 5 == 0) {
                rows.add(values);
                values = new LinkedHashMap<>();
            }
        }
        if (!values.isEmpty()) {
            rows.add(values);
        }

        return new KeyboardMessage(chatId, textToSend, rows).prepareKeyboardMessage();
    }

    public SendMessage closeRoomsMenu(Long chatId, String roomForPrint) {

        SendMessage answerMessage = new SendMessage();
        answerMessage.setChatId(chatId);
        answerMessage.setText(EmojiParser.parseToUnicode("Вы выбрали комнату\n") + roomForPrint);

        return answerMessage;
    }

    public static String convertLocalDateToString(LocalDate date, boolean withMonth, boolean withMonthAndYear) {

        StringBuilder result = new StringBuilder();
        String day, month, year;

        day = String.valueOf(date.getDayOfMonth());
        month = " " + MONTHS.get(date.getMonth());
        year = " " + date.getYear();

        result.append(day);

        if (withMonth) {
            result.append(month);
        }

        if (withMonthAndYear) {
            result.append(month);
            result.append(year);
        }

        return String.valueOf(result);
    }

}
