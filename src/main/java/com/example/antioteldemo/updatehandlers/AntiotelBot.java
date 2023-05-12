package com.example.antioteldemo.updatehandlers;

import com.example.antioteldemo.config.BotConfig;
import com.example.antioteldemo.entities.Guest;
import com.example.antioteldemo.repo.GuestRepository;
import com.example.antioteldemo.service.KeyboardMessage;
import com.vdurmont.emoji.EmojiParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.time.Month;
import java.util.*;

import static com.example.antioteldemo.config.OptionPattern.*;

@Component
public class AntiotelBot extends TelegramLongPollingBot {

    @Autowired
    private GuestRepository guestRepo;
    @Autowired
    private TextMessageHandler textMessageHandler;
    @Autowired
    private CallbackQueryHandler callbackQueryHandler;

    private final BotConfig botConfig;

    public AntiotelBot(BotConfig botConfig) {

        this.botConfig = botConfig;

        List<BotCommand> listOfCommands = new ArrayList<>();

        listOfCommands.add(new BotCommand(BOOK_ROOM, EmojiParser.parseToUnicode(":pushpin:  Забронировать комнату")));
        listOfCommands.add(new BotCommand(RULES, EmojiParser.parseToUnicode(":man_student:  Правила посещения")));

        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            System.out.println("\nError settings bot's command list: " + e.getMessage());
        }

    }

    @Override
    public void onUpdateReceived(Update update) {

        LinkedHashMap<SendMessage, Integer> answerData;
        int methodNumber;
        SendMessage message;
        long chatId;

        if (update.hasMessage() && update.getMessage().hasText()) {

            answerData = textMessageHandler.handleTextMessage(update.getMessage());

            for (var data : answerData.entrySet()) {

                methodNumber = data.getValue();
                message = data.getKey();
                chatId = Long.parseLong(message.getChatId());

                switch (methodNumber) {

                    case 1 -> sendMessage(message);
                    case 3 -> prepareTextMessage(chatId, message.getText());
                    case 4 -> sendToAllGuests(message.getText());
                    case 6 -> sendImageMessage(chatId, message.getText());

                }

            }

        } else if (update.hasCallbackQuery()) {

            answerData = callbackQueryHandler.handleCallbackQuery(update.getCallbackQuery());
            int messageId;

            for (var data : answerData.entrySet()) {

                methodNumber = data.getValue();

                if (methodNumber != 0) {

                    message = data.getKey();
                    chatId = Long.parseLong(message.getChatId());

                    switch (methodNumber) {

                        case 1 -> sendMessage(message);
                        case 2 -> {
                            messageId = message.getMessageThreadId();
                            executeEditMessage(chatId, message.getText(), messageId);
                        }
                        case 3 -> prepareTextMessage(chatId, message.getText());
                        case 5 -> editMenu(message);
                    }
                }

            }

        }

    }

    // Method #1
    private void sendMessage(SendMessage message) {

        int dayNumber = 0;

        if (message.getProtectContent() != null) {
            message.setMessageThreadId(null);
            dayNumber = message.getProtectContent() ? 1 : 2;
            message.setProtectContent(null);
        }

        if (message.getDisableNotification() != null) {

            dayNumber = 3;
            message.setDisableNotification(null);
            currentOrders.get(Long.parseLong(message.getChatId())).setFirstTimeMenu(true);
        }

        if (message.getMessageThreadId() != null && message.getMessageThreadId() == 3) {
            message.setMessageThreadId(null);
        }

        try {
            Message sentMessage = execute(message);
            long chatId = sentMessage.getChatId();

            if (currentOrders.get(chatId) != null && currentOrders.get(chatId).getFirstTimeMenu() && dayNumber != 0) {

                if (dayNumber == 1) {
                    currentOrders.get(chatId).setFirstDayMessageId(sentMessage.getMessageId());
                } else if (dayNumber == 2) {
                    currentOrders.get(chatId).setSecondDayMessageId(sentMessage.getMessageId());
                    currentOrders.get(chatId).setFirstTimeMenu(false);
                } else {
                    currentOrders.get(chatId).setCalcPanelMessageId(sentMessage.getMessageId());
                }

            }
        } catch (TelegramApiException e) {

            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }

    // Method #2
    private void executeEditMessage(long chatId, String text, Integer messageId) {

        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setText(text);
        message.setMessageId(messageId);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.getStackTrace();
        }
    }

    // Method #3
    private void prepareTextMessage(long chatId, String text) {

        SendMessage message = new SendMessage();
        message.setText(text);
        message.setChatId(chatId);

        sendMessage(message);
    }

    // Method #4
    private void sendToAllGuests(String textToSend) {

        try {
            if (guestRepo.count() != 0) {
                List<Guest> allGuests = (List<Guest>) guestRepo.findAll();
                for (int i = 0; i < allGuests.size(); i++) {
                    if (i % 20 == 0) {
                        Thread.sleep(100);
                    }
                    prepareTextMessage(Long.parseLong(String.valueOf(allGuests.get(i).getChatId())), textToSend);
                }
            }
        } catch (Exception e) {
            e.getStackTrace();
        }

    }

    // Method #5
    private void editMenu(SendMessage message) {

        try {
            long chatId = Long.parseLong(message.getChatId());
            EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();

            editMessageReplyMarkup.setChatId(chatId);
            int messageId = message.getMessageThreadId();
            editMessageReplyMarkup.setMessageId(messageId);
            editMessageReplyMarkup.setReplyMarkup((InlineKeyboardMarkup) message.getReplyMarkup());

            execute(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            e.getStackTrace();
        }

    }

    // Method #6
    private void sendImageMessage(long chatId, String imagePath) {

        File image = new File(imagePath);
        InputFile inputFile = new InputFile(image);
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setPhoto(inputFile);
        sendPhoto.setChatId(chatId);

        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.getStackTrace();
        }
    }

    @Override
    public String getBotUsername() {

        MONTHS.put(Month.JANUARY, "января");
        MONTHS.put(Month.FEBRUARY, "февраля");
        MONTHS.put(Month.MARCH, "марта");
        MONTHS.put(Month.APRIL, "апреля");
        MONTHS.put(Month.MAY, "мая");
        MONTHS.put(Month.JUNE, "июня");
        MONTHS.put(Month.JULY, "июля");
        MONTHS.put(Month.AUGUST, "августа");
        MONTHS.put(Month.SEPTEMBER, "сентября");
        MONTHS.put(Month.OCTOBER, "октября");
        MONTHS.put(Month.NOVEMBER, "ноября");
        MONTHS.put(Month.DECEMBER, "декабря");

        ROOMS_FOR_PRINT.put(SHABASH, EmojiParser.parseToUnicode(":woman_vampire: Шабаш (6500)"));
        ROOMS_FOR_PRINT.put(ABONENT, EmojiParser.parseToUnicode(":no_mobile_phones: Абонент недоступен (2600)"));
        ROOMS_FOR_PRINT.put(INFERNO, EmojiParser.parseToUnicode(":hotsprings: Пекло (3900)"));
        ROOMS_FOR_PRINT.put(SHIP_HOLD, EmojiParser.parseToUnicode(":anchor: Трюм (5400)"));
        ROOMS_FOR_PRINT.put(DEPOT, EmojiParser.parseToUnicode(":metro: Депо (3900)"));
        ROOMS_FOR_PRINT.put(F_STREET, EmojiParser.parseToUnicode(":gun: Фacking street 69 (3900)"));
        ROOMS_FOR_PRINT.put(ANIME, EmojiParser.parseToUnicode(":woman_elf: Аниме (5400)"));
        ROOMS_FOR_PRINT.put(GLADIATOR, EmojiParser.parseToUnicode(":crossed_swords: Гладиатор (6500)"));
        ROOMS_FOR_PRINT.put(NIGHT_FLIGHT, EmojiParser.parseToUnicode(":small_airplane: Ночной полет (3900)"));

        ROOM_NAMES.put(SHABASH, EmojiParser.parseToUnicode(":woman_vampire: Шабаш"));
        ROOM_NAMES.put(ABONENT, EmojiParser.parseToUnicode(":no_mobile_phones: Абонент недоступен"));
        ROOM_NAMES.put(INFERNO, EmojiParser.parseToUnicode(":hotsprings: Пекло"));
        ROOM_NAMES.put(SHIP_HOLD, EmojiParser.parseToUnicode(":anchor: Трюм"));
        ROOM_NAMES.put(DEPOT, EmojiParser.parseToUnicode(":metro: Депо"));
        ROOM_NAMES.put(F_STREET, EmojiParser.parseToUnicode(":gun: Фacking street 69"));
        ROOM_NAMES.put(ANIME, EmojiParser.parseToUnicode(":woman_elf: Аниме"));
        ROOM_NAMES.put(GLADIATOR, EmojiParser.parseToUnicode(":crossed_swords: Гладиатор"));
        ROOM_NAMES.put(NIGHT_FLIGHT, EmojiParser.parseToUnicode(":small_airplane: Ночной полет"));

        ROOM_PRICES.put(SHABASH, (6500 / 2));
        ROOM_PRICES.put(ABONENT, (2600 / 2));
        ROOM_PRICES.put(INFERNO, (3900 / 2));
        ROOM_PRICES.put(SHIP_HOLD, (5400 / 2));
        ROOM_PRICES.put(DEPOT, (3900 / 2));
        ROOM_PRICES.put(F_STREET, (3900 / 2));
        ROOM_PRICES.put(ANIME, (5400 / 2));
        ROOM_PRICES.put(GLADIATOR, (6500 / 2));
        ROOM_PRICES.put(NIGHT_FLIGHT, (3900 / 2));



        LinkedHashMap<String, String> values;
        List<LinkedHashMap<String, String>> rows = new ArrayList<>();
        LinkedHashMap<String, String> dataMap = new LinkedHashMap<>();
        String textToSend;


        // Меню выбора комнаты
        textToSend = EmojiParser.parseToUnicode("""
                Выберите комнату - в скобках
                указана максимальная цена
                в руб. за 2 часа: :point_down:""");
        for (var data : ROOMS_FOR_PRINT.entrySet()) {
            values = new LinkedHashMap<>();
            values.put(data.getKey(), data.getValue());
            rows.add(values);
        }
        chooseRoomMenu = new KeyboardMessage(0L, textToSend, rows).prepareKeyboardMessage();


        // Паттерн меню ДА/НЕТ.
        rows = new ArrayList<>();
        values = new LinkedHashMap<>();
        dataMap.put(YES, "ДА");
        dataMap.put(NO, "НЕТ");
        for (var data : dataMap.entrySet()) {
            values.put(data.getKey(), EmojiParser.parseToUnicode(data.getValue()));
        }
        rows.add(values);
        yesNoMenu = new KeyboardMessage(0L, "", rows).prepareKeyboardMessage();


        // Меню выбора даты бронирования НАЧАЛЬНОЕ
        textToSend = EmojiParser.parseToUnicode("Выберите дату заезда :calendar: :");
        rows = new ArrayList<>();
        values = new LinkedHashMap<>();
        values.put(TODAY, "сегодня");
        values.put(TOMORROW, "завтра");
        rows.add(values);
        values = new LinkedHashMap<>();
        values.put(CURRENT_MONTH, "в этом месяце");
        rows.add(values);
        values = new LinkedHashMap<>();
        values.put(NEXT_MONTH, "в следующем месяце");
        rows.add(values);
        firstDateMenu = new KeyboardMessage(0L, textToSend, rows).prepareKeyboardMessage();


        // Кнопка "Готово" - после выбора временного промежутка.
        values = new LinkedHashMap<>();
        rows = new ArrayList<>();
        values.put(DONE, DONE_PRINT);
        rows.add(values);
        DONE_BIG_BUTTON = new KeyboardMessage(0L,
                EmojiParser.parseToUnicode(":point_up_2: Выбрали временной промежуток? - Нажмите \"Готово\" :arrow_down:"),
                rows).prepareKeyboardMessage();


        // Меню выбора формы оплаты.
        values = new LinkedHashMap<>();
        rows = new ArrayList<>();
        values.put(CASH, CASH_PRINT);
        rows.add(values);
        values = new LinkedHashMap<>();
        values.put(NON_CASH, NON_CASH_PRINT);
        rows.add(values);
        paymentFormMenu = new KeyboardMessage(0L,
                EmojiParser.parseToUnicode("Выберите форму оплаты :credit_card:"),
                rows).prepareKeyboardMessage();


        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {

        return botConfig.getToken();
    }

}
