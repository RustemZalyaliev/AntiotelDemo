package com.example.antioteldemo.updatehandlers;

import org.jvnet.hk2.annotations.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.LinkedHashMap;

@Service
public interface CallbackQueryHandler {

    LinkedHashMap<SendMessage, Integer> handleCallbackQuery(CallbackQuery callbackQuery);

}
