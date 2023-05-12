package com.example.antioteldemo.service;

import com.example.antioteldemo.entities.Guest;
import com.example.antioteldemo.repo.GuestRepository;
import com.vdurmont.emoji.EmojiParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static com.example.antioteldemo.config.OptionPattern.*;

@Component
@Scope("prototype")
public class GuestRegister {

    @Autowired
    private GuestRepository guestRepo;

    public LinkedHashMap<SendMessage, Integer> registerGuest(CallbackQuery callbackQuery) {

        LinkedHashMap<SendMessage, Integer> answerData = new LinkedHashMap<>();
        SendMessage answerMessage = new SendMessage();
        Message messageIn = callbackQuery.getMessage();
        int messageId = messageIn.getMessageId();
        answerMessage.setMessageThreadId(messageId);
        long chatId = messageIn.getChatId();
        answerMessage.setChatId(chatId);
        String callbackData = callbackQuery.getData();
        Guest guest;

        if (!guestRepo.existsById(chatId)) {

            if (YES.equals(callbackData)) {

                guest = Guest.builder()
                        .chatId(chatId)
                        .firstName(messageIn.getChat().getFirstName())
                        .lastName(messageIn.getChat().getLastName())
                        .username(messageIn.getChat().getUserName())
                        .registeredAt(String.valueOf(LocalDateTime.now()))
                        .isAdmin(false)
                        .build();

                guestRepo.save(guest);

                answerMessage.setChatId(chatId);
                answerMessage.setText(EmojiParser.parseToUnicode("""
                        Добро пожаловать в бот
                        бронирования комнат
                        Антиотелей! :tada:
                        
                        :point_down: Нажмите меню."""));

            } else if (NO.equals(callbackData)) {

                answerMessage.setText("""
                        К сожалению, мы не можем
                        предоставить Вам доступ
                        к информации этого бота,
                        - она предназначена лицам
                        достигшим 18-летнего возраста.""");

            }

        } else {

            answerMessage = alreadyRegistered(chatId);
            answerMessage.setMessageThreadId(messageId);

        }

        answerData.put(answerMessage, 2);

        return answerData;

    }

    public SendMessage alreadyRegistered(Long chatId) {

        SendMessage sendMessage = new SendMessage();

        Guest guest = guestRepo.findById(chatId).get();
        LocalDateTime stamp = LocalDateTime.parse(guest.getRegisteredAt());
        LocalDate date = LocalDate.from(stamp);
        LocalTime time = LocalTime.of(stamp.getHour(), stamp.getMinute());
        String welcome = EmojiParser.parseToUnicode("Здравствуйте, " + guest.getFirstName()
                + "! " + ":wave:" + "\nВы уже зарегистрировались\n" + date.getDayOfMonth() + " "
                + MONTHS.get(date.getMonth()) + " " + date.getYear() + " в " + time + ". " + ":point_up:\n\n:point_down: Нажмите меню.");

        sendMessage.setChatId(chatId);
        sendMessage.setText(welcome);

        return sendMessage;
    }

}
