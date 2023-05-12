package com.example.antioteldemo.service;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Random;

import static com.example.antioteldemo.config.OptionPattern.ROOM_NAMES;
import static com.example.antioteldemo.config.OptionPattern.currentOrders;
import static com.example.antioteldemo.service.DateMenu.convertLocalDateToString;
import static com.example.antioteldemo.service.TimeMenu.getDateFromTimeStamp;
import static com.example.antioteldemo.service.TimeMenu.getTimeFromTimeStamp;

@Component
@Scope("prototype")
public class FinishBooking {

    public LinkedHashMap<SendMessage, Integer> paymentFormChosenPlug(Message messageIn, boolean cash) {

        LinkedHashMap<SendMessage, Integer> answerData = new LinkedHashMap<>();
        SendMessage answerMessage = new SendMessage();

        int messageId = messageIn.getMessageId();
        long chatId = messageIn.getChatId();
        answerMessage.setChatId(chatId);
        answerMessage.setMessageThreadId(messageId);

        if (cash) {
            answerMessage.setText("Выбрана оплата наличными.");
        } else {
            answerMessage.setText("Выбрана оплата банковской картой.");
        }
        answerData.put(answerMessage, 2);

        answerData.putAll(finishMessages(chatId, cash));

        return answerData;
    }

    public LinkedHashMap<SendMessage, Integer> finishMessages(Long chatId, boolean cash) {

        LinkedHashMap<SendMessage, Integer> answerData = new LinkedHashMap<>();
        SendMessage answerMessage = new SendMessage();
        String dateIn = convertLocalDateToString(getDateFromTimeStamp(currentOrders.get(chatId).getTimeFrom()),
                false, true);
        String startTime = String.valueOf(getTimeFromTimeStamp(currentOrders.get(chatId).getTimeFrom()));
        String endTime = String.valueOf(getTimeFromTimeStamp(currentOrders.get(chatId).getTimeTo()));
        String payment = cash ? "наличными при заезде" : "банковской картой (оплачено)";

        answerMessage.setChatId(chatId);
        answerMessage.setText(EmojiParser.parseToUnicode(":checkered_flag: БРОНЬ ОФОРМЛЕНА :trophy:"
                + "\n\n:door: Комната  -  " + ROOM_NAMES.get(currentOrders.get(chatId).getRoomName())
                + "\n\n:date: Дата заезда  -  " + dateIn
                + "\n\n:stopwatch: Время начала - окончания:  " +  startTime + " - " + endTime
                + "\n\n:hourglass_flowing_sand: Длительность квеста (часов)  -  " + currentOrders.get(chatId).getHours()
                + "\n\n:drum_with_drumsticks: Стоимость, руб.  -  " + currentOrders.get(chatId).getProfit()
                + "\n\n:credit_card: Оплата  -  " + payment));

        answerData.put(answerMessage, 1);

        Random rnd = new Random();
        int intercomCode = rnd.nextInt(10000);
        int terminalCode = rnd.nextInt(10000);
        String startEnterTime = String.valueOf(LocalTime.parse(startTime).minusMinutes(10));

        answerMessage = new SendMessage();
        answerMessage.setChatId(chatId);
        answerMessage.setText(EmojiParser.parseToUnicode(":unlock: Код домофона  -  " + intercomCode
                + "\n\n:key: Код терминала  -  " + terminalCode
                + "\n\n:arrow_forward: Коды будут активированы за\n10 минут до заезда - в  " + startEnterTime
                + "\n\n:dancer: Приятного отдыха! :tada:"));

        answerData.put(answerMessage, 1);

        return answerData;
    }

}
