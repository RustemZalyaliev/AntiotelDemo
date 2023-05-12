package com.example.antioteldemo.updatehandlers.impl;

import com.example.antioteldemo.config.BotConfig;
import com.example.antioteldemo.repo.GuestRepository;
import com.example.antioteldemo.service.GuestRegister;
import com.example.antioteldemo.updatehandlers.TextMessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.LinkedHashMap;

import static com.example.antioteldemo.config.OptionPattern.*;

@Component
@Scope("prototype")
public class TextMessageHandlerImpl implements TextMessageHandler {

    @Autowired
    private GuestRepository guestRepo;
    @Autowired
    private GuestRegister guestRegister;

    private final BotConfig botConfig;

    public TextMessageHandlerImpl(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public LinkedHashMap<SendMessage, Integer> handleTextMessage(Message message) {

        String messageText = message.getText();
        long chatId = message.getChatId();
        LinkedHashMap<SendMessage, Integer> answerData = new LinkedHashMap<>();
        int methodNumber = 0;
        SendMessage answerMessage = new SendMessage();
        answerMessage.setChatId(chatId);

        if (!guestRepo.existsById(chatId)) {

            if ("/start".equals(messageText)) {

            methodNumber = 1;
            answerMessage = yesNoMenu;
            answerMessage.setChatId(chatId);
            answerMessage.setText("""
                    В соответствии с требованиями
                    федерального законодательства,
                    мы должны применить технические
                    меры по защите детей от информации,
                    запрещенной для распространения
                    среди них. К таковой относится
                    часть содержания этого бота,
                    поэтому мы спрашиваем:
                    
                    Вам есть 18 лет?""");

            } else {

                methodNumber = 1;
                answerMessage.setText(USER_IS_NOT_EXIST);

            }

        } else {

            switch (messageText) {
                case "/start" -> {
                    methodNumber = 1;
                    answerMessage = guestRegister.alreadyRegistered(chatId);
                }
                case BOOK_ROOM -> {
                    methodNumber = 1;
                    answerMessage = chooseRoomMenu;
                    answerMessage.setChatId(chatId);
                }
                case RULES -> {
                    methodNumber = 1;
                    answerMessage.setText("""
                            Условия и Правила посещения
                            Антиотеля - подробно по ссылке:
                            
                            """ + botConfig.getHotelRules());
                }
            }

        }

        answerData.put(answerMessage, methodNumber);

        return answerData;
    }

}
