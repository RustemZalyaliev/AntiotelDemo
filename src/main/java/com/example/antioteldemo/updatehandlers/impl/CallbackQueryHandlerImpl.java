package com.example.antioteldemo.updatehandlers.impl;

import com.example.antioteldemo.pojo.CurrentOrder;
import com.example.antioteldemo.repo.GuestRepository;
import com.example.antioteldemo.service.*;
import com.example.antioteldemo.updatehandlers.CallbackQueryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDateTime;
import java.util.*;

import static com.example.antioteldemo.config.OptionPattern.*;

@Component
public class CallbackQueryHandlerImpl implements CallbackQueryHandler {

    @Autowired
    private GuestRepository guestRepo;
    @Autowired
    private GuestRegister guestRegister;
    @Autowired
    private DateMenu dateMenu;
    @Autowired
    private TimeMenu timeMenu;
    @Autowired
    private Payment payment;
    @Autowired
    private FinishBooking finishBooking;

    @Override
    public LinkedHashMap<SendMessage, Integer> handleCallbackQuery(CallbackQuery callbackQuery) {

        LinkedHashMap<SendMessage, Integer> answerData = new LinkedHashMap<>();
        int methodNumber = 0;
        SendMessage answerMessage = new SendMessage();
        Message messageIn = callbackQuery.getMessage();
        int messageId = messageIn.getMessageId();
        answerMessage.setMessageThreadId(messageId);
        Long chatId = messageIn.getChatId();
        answerMessage.setChatId(chatId);
        String callbackData = callbackQuery.getData();

        if (guestRepo.existsById(chatId)) {

            try {

                int digit = Integer.parseInt(callbackData);
                if (digit > 0 && digit < 32) {
                    answerData = dateMenu.handleDateDigit(callbackQuery, digit);

                    int counter = 0;
                    for (var sm : answerData.entrySet()) {
                        if (sm.getKey().getMessageThreadId() == null) {
                            ++counter;
                            sm.getKey().setMessageThreadId(counter);
                        }
                    }
                }

            } catch (Exception e) {

                if (callbackData.contains("&")) {

                    List<SendMessage> twoDates = timeMenu.handleButtonPushed(callbackQuery);
                    answerData.put(twoDates.get(0), 5);
                    answerData.put(twoDates.get(1), 5);
                    answerData.put(twoDates.get(2), 2);

                } else if (ROOMS_1905.contains(callbackData)) {

                    currentOrders.remove(chatId);
                    CurrentOrder order = CurrentOrder.builder()
                            .chatId(chatId)
                            .roomName(callbackData)
                            .firstTimeMenu(false)
                            .hourPrice(ROOM_PRICES.get(callbackData))
                            .startOrderTime(LocalDateTime.now())
                            .build();
                    currentOrders.put(chatId, order);

                    answerMessage = dateMenu.closeRoomsMenu(chatId, ROOMS_FOR_PRINT.get(callbackData));
                    answerMessage.setMessageThreadId(messageId);

                    answerData.put(answerMessage, 2);

                    answerMessage = firstDateMenu;
                    answerMessage.setChatId(chatId);
                    methodNumber = 1;

                } else if (FIRST_TIME_CHOICE.contains(callbackData)) {

                    answerData = dateMenu.handleFirstDateMenu(callbackQuery);

                } else {

                    switch (callbackData) {
                        case DONE -> {
                            if (currentOrders.get(chatId).getTimeFrom() != null && currentOrders.get(chatId).getTimeTo() != null) {
                                answerData = payment.paymentFormMenu(messageIn);
                            } else {
                                answerData = timeMenu.timeScopeIsNotChosen(chatId, messageId);
                            }
                        }
                        case CASH -> {
                            currentOrders.get(chatId).setPrepay(false);
                            answerData = finishBooking.paymentFormChosenPlug(messageIn, true);
                        }
                        case NON_CASH -> {
                            currentOrders.get(chatId).setPrepay(true);
                            answerData = finishBooking.paymentFormChosenPlug(messageIn, false);
                        }
                    }

                }



            }

        } else {

            answerData = guestRegister.registerGuest(callbackQuery);

        }

        answerData.put(answerMessage, methodNumber);

        return answerData;
    }

}
