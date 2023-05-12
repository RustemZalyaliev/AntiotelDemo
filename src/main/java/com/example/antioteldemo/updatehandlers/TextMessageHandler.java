package com.example.antioteldemo.updatehandlers;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.LinkedHashMap;

@Service
public interface TextMessageHandler {

    LinkedHashMap<SendMessage, Integer> handleTextMessage(Message message);

}
