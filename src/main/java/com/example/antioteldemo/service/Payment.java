package com.example.antioteldemo.service;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.LinkedHashMap;

import static com.example.antioteldemo.config.OptionPattern.currentOrders;
import static com.example.antioteldemo.config.OptionPattern.paymentFormMenu;

@Component
@Scope("prototype")
public class Payment {

    public LinkedHashMap<SendMessage, Integer> paymentFormMenu(Message messageIn) {

        LinkedHashMap<SendMessage, Integer> answerData = new LinkedHashMap<>();
        SendMessage answerMessage = new SendMessage();

        int messageId = messageIn.getMessageId();
        long chatId = messageIn.getChatId();
        answerMessage.setChatId(chatId);

        answerMessage.setMessageThreadId(currentOrders.get(chatId).getFirstDayMessageId());
        answerMessage.setText(EmojiParser.parseToUnicode("Время начала выбрано :round_pushpin:"));
        answerData.put(answerMessage, 2);

        answerMessage = new SendMessage();
        answerMessage.setChatId(chatId);
        answerMessage.setMessageThreadId(currentOrders.get(chatId).getSecondDayMessageId());
        answerMessage.setText(EmojiParser.parseToUnicode("Время окончания выбрано :round_pushpin:"));
        answerData.put(answerMessage, 2);

        answerMessage = new SendMessage();
        answerMessage.setChatId(chatId);
        answerMessage.setMessageThreadId(messageId);
        answerMessage.setText(EmojiParser.parseToUnicode("Заказ сформирован :weight_lifter:"));
        answerData.put(answerMessage, 2);

        answerMessage = paymentFormMenu;
        answerMessage.setChatId(chatId);
        answerData.put(answerMessage, 1);

        return answerData;
    }

}
