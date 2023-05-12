package com.example.antioteldemo.service;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static com.example.antioteldemo.config.OptionPattern.*;
import static com.example.antioteldemo.service.DateMenu.convertLocalDateToString;

@Component
@Scope("prototype")
public class TimeMenu {

    public List<SendMessage> getTimeMenu(LocalDate dateFrom, Long chatId) {

        List<SendMessage> twoDates = new ArrayList<>();
        LocalTime timeFrom = LocalTime.parse("00:00");

        if (dateFrom.equals(LocalDate.now())) {

            LocalTime now = LocalTime.now();

            if (now.isBefore(LocalTime.parse("22:30"))) {
                timeFrom = now.getMinute() < 30 ? LocalTime.parse(getTwoDigits(now.getHour() + 1) + ":00")
                        : LocalTime.parse(getTwoDigits(now.getHour() + 2) + ":00");
            } else {
                timeFrom = LocalTime.parse("00:30");
                dateFrom = dateFrom.plusDays(1);
            }

        }

        twoDates.add(oneDayTimeMenu(chatId, dateFrom, timeFrom));
        twoDates.add(oneDayTimeMenu(chatId, dateFrom.plusDays(1), LocalTime.parse("00:00")));

        return twoDates;
    }

    public SendMessage oneDayTimeMenu(Long chatId, LocalDate dateFrom, LocalTime timeFrom) {

        String buttonData, buttonPrint, textToSend;
        LinkedHashMap<String, String> values = new LinkedHashMap<>();
        List<LinkedHashMap<String, String>> rows = new ArrayList<>();
        LocalTime timeTo = timeFrom.plusMinutes(30);
        HALF_HOURS = 48;

        LocalDateTime orderTimeStampFrom = currentOrders.get(chatId).getTimeFrom();
        LocalDateTime orderTimeStampTo = currentOrders.get(chatId).getTimeTo();

        boolean toMark;

        String mark = EMPTY_TIME_BUTTON;

        LocalDate dateFromMark, dateToMark;
        LocalTime timeMarkFrom, timeMarkTo;

        if (orderTimeStampFrom == null && orderTimeStampTo == null) {

            toMark = false;
            timeMarkFrom = null;
            timeMarkTo = null;

        } else {

            toMark = true;
            mark = MARK_TIME_BUTTON;

            dateFromMark = getDateFromTimeStamp(orderTimeStampFrom);
            timeMarkFrom = getTimeFromTimeStamp(orderTimeStampFrom);

            if (orderTimeStampTo != null) {
                dateToMark = getDateFromTimeStamp(orderTimeStampTo);
                timeMarkTo = getTimeFromTimeStamp(orderTimeStampTo);
            } else {
                dateToMark = dateFromMark;
                timeMarkTo = timeMarkFrom;
                timeMarkTo = timeMarkTo.plusMinutes(29);
            }

            if (dateFrom.equals(dateFromMark) && !dateFrom.equals(dateToMark)) {
                timeMarkTo = LocalTime.parse("23:59");
            } else if (!dateFrom.equals(dateFromMark) && dateFrom.equals(dateToMark)) {
                timeMarkFrom = LocalTime.parse("00:00");
            } else if (!dateFrom.equals(dateFromMark) && !dateFrom.equals(dateToMark)) {
                mark = EMPTY_TIME_BUTTON;
                toMark = false;
            }
        }

        if (!"00:00".equals(String.valueOf(timeFrom))) {
            if ("00:30".equals(String.valueOf(timeFrom))) {
                HALF_HOURS = 47;
            } else {
                HALF_HOURS = (24 - timeFrom.getHour()) * 2;
            }
        }

        for (int i = 1; i <= HALF_HOURS; i++) {

            if (toMark) {
                if ((timeFrom.equals(timeMarkFrom) || timeFrom.isAfter(timeMarkFrom))
                        && timeFrom.isBefore(timeMarkTo)) {
                    mark = MARK_TIME_BUTTON;
                } else {
                    mark = EMPTY_TIME_BUTTON;
                }
            }

            buttonPrint = EmojiParser.parseToUnicode(timeFrom + mark + timeTo);
            buttonData = dateFrom + "&" + timeFrom + " - " + timeTo;
            values.put(buttonData, buttonPrint);

            if (i % 3 == 0) {
                rows.add(values);
                values = new LinkedHashMap<>();
            }

            timeFrom = timeTo;
            timeTo = timeTo.plusMinutes(30);
        }

        if (!values.isEmpty()) {
            rows.add(values);
        }

        textToSend = dateFrom.getDayOfMonth() + " " + MONTHS.get(dateFrom.getMonth()) + " " + dateFrom.getYear()
                + ". " + CHOOSE_PERIOD;

        return new KeyboardMessage(0, textToSend, rows).prepareKeyboardMessage();
    }

    public List<SendMessage> handleButtonPushed(CallbackQuery callbackQuery) {

        Message messageIn = callbackQuery.getMessage();
        long chatId = messageIn.getChatId();
        String callbackData = callbackQuery.getData();

        String[] dateAndTime = callbackData.split("&");
        LocalDate orderDate = LocalDate.parse(dateAndTime[0]);

        String[] twoTimes = dateAndTime[1].split(" ");
        LocalDateTime startHalf = LocalDateTime.parse(orderDate + "T" + twoTimes[0]);
        LocalDateTime endHalf = LocalDateTime.parse(orderDate + "T" + twoTimes[2]);
        LocalDateTime buffer;
        long hourDelta;

        // Если 1-й таймстамп пуст.
        if (currentOrders.get(chatId).getTimeFrom() == null) {
            currentOrders.get(chatId).setTimeFrom(startHalf);
            currentOrders.get(chatId).setHours(0);
            currentOrders.get(chatId).setProfit(0);

            // Если 1-й таймстамп непустой, а 2-й пуст;
        } else if (currentOrders.get(chatId).getTimeTo() == null) {

            int currentOrderHours;

            hourDelta = ChronoUnit.MINUTES.between(currentOrders.get(chatId).getTimeFrom(), startHalf) > 0
                    ? ChronoUnit.MINUTES.between(currentOrders.get(chatId).getTimeFrom(), endHalf)
                    : ChronoUnit.MINUTES.between(startHalf, currentOrders.get(chatId).getTimeFrom().plusMinutes(30));

            if (hourDelta % 60 == 0 && (hourDelta >= 120 || hourDelta <= -120)) {

                boolean isLastHalfHour = (orderDate + "T00:00").equals(String.valueOf(endHalf));

                // при этом если введенный таймстамп раньше 1-го;
                if (endHalf.isBefore(currentOrders.get(chatId).getTimeFrom()) && !isLastHalfHour) {
                    buffer = currentOrders.get(chatId).getTimeFrom().plusMinutes(30);
                    currentOrders.get(chatId).setTimeFrom(startHalf);
                    currentOrders.get(chatId).setTimeTo(buffer);

                    // или кнопка времени является последней в блоке даты - "XX:XX - 00:00"
                } else if (isLastHalfHour) {
                    currentOrders.get(chatId).setTimeTo(endHalf.plusDays(1));
                    hourDelta = ChronoUnit.MINUTES.between(currentOrders.get(chatId).getTimeFrom(), currentOrders.get(chatId).getTimeTo());

                    // или если введенный таймстамп позже 1-го.
                } else {
                    currentOrders.get(chatId).setTimeTo(endHalf);
                }

                currentOrderHours = (int) (hourDelta < 0 ? -hourDelta : hourDelta);
                currentOrders.get(chatId).setHours(currentOrderHours / 60);
                currentOrders.get(chatId).setProfit(currentOrders.get(chatId).getHours() * currentOrders.get(chatId).getHourPrice());

            }

        } else {
            currentOrders.get(chatId).setTimeFrom(startHalf);
            currentOrders.get(chatId).setTimeTo(null);
            currentOrders.get(chatId).setHours(0);
            currentOrders.get(chatId).setProfit(0);
        }

        List<SendMessage> twoDatesAndCalcPanel = getTimeMenu(currentOrders.get(chatId).getDateFrom(), chatId);
        twoDatesAndCalcPanel.get(0).setMessageThreadId(currentOrders.get(chatId).getFirstDayMessageId());
        twoDatesAndCalcPanel.get(0).setChatId(chatId);
        twoDatesAndCalcPanel.get(0).setProtectContent(true);
        twoDatesAndCalcPanel.get(1).setMessageThreadId(currentOrders.get(chatId).getSecondDayMessageId());
        twoDatesAndCalcPanel.get(1).setChatId(chatId);
        twoDatesAndCalcPanel.get(1).setProtectContent(false);

        LocalDate orderDateFrom = getDateFromTimeStamp(currentOrders.get(chatId).getTimeFrom());
        int profit = currentOrders.get(chatId).getProfit() == null ? 0 : currentOrders.get(chatId).getProfit();
        int hourNumber = currentOrders.get(chatId).getHours() == null ? 0 : currentOrders.get(chatId).getHours();

        SendMessage updateCalcPanel = getCalcPanel(chatId, orderDateFrom, profit, hourNumber);
        updateCalcPanel.setMessageThreadId(currentOrders.get(chatId).getCalcPanelMessageId());
        twoDatesAndCalcPanel.add(updateCalcPanel);

        return twoDatesAndCalcPanel;
    }

    public SendMessage getCalcPanel(Long chatId, LocalDate dateFrom, int profit, int hourNumber) {

        SendMessage sendMessage = new SendMessage();

        String strDateFrom = convertLocalDateToString(dateFrom, false, true);

        String startTime = currentOrders.get(chatId).getTimeFrom() == null ? "не выбрано"
                : String.valueOf(getTimeFromTimeStamp(currentOrders.get(chatId).getTimeFrom()));
        String endTime = currentOrders.get(chatId).getTimeTo() == null? "не выбрано"
                : String.valueOf(getTimeFromTimeStamp(currentOrders.get(chatId).getTimeTo()));

        String text = EmojiParser.parseToUnicode(":date: Дата заезда - " + strDateFrom
                + "\n:stopwatch: Время начала - окончания: " + startTime + " - "
                + endTime
                + "\n:hourglass_flowing_sand: Длительность квеста (часов) - " + hourNumber
                + "\n:drum_with_drumsticks: Стоимость, руб. - " + profit);

        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setMessageThreadId(currentOrders.get(chatId).getCalcPanelMessageId());

        return sendMessage;
    }

    public LinkedHashMap<SendMessage, Integer> timeScopeIsNotChosen(Long chatId, int messageId) {

        LinkedHashMap<SendMessage, Integer> answerData = new LinkedHashMap<>();
        SendMessage answerMessage = new SendMessage();

        answerMessage.setChatId(chatId);
        answerMessage.setMessageThreadId(messageId);
        answerMessage.setText(EmojiParser.parseToUnicode("""
                Временной промежуток не выбран :no_entry:
                Пожалуйста, вернитесь к меню выше :point_up:"""));

        answerData.put(answerMessage, 2);

        answerMessage = DONE_BIG_BUTTON;
        answerMessage.setChatId(chatId);

        answerData.put(answerMessage, 1);

        return answerData;
    }

    public static String getTwoDigits(int value) {

        return value < 10 ? "0" + value : String.valueOf(value);
    }

    public static LocalDate getDateFromTimeStamp(LocalDateTime stamp) {

        String strStamp = String.valueOf(stamp);

        return LocalDate.parse(strStamp.substring(0, strStamp.indexOf("T")));
    }

    public static LocalTime getTimeFromTimeStamp(LocalDateTime stamp) {

        String strStamp = String.valueOf(stamp);

        return LocalTime.parse(strStamp.substring(strStamp.indexOf("T") + 1));
    }

}
