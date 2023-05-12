package com.example.antioteldemo.config;

import com.example.antioteldemo.pojo.CurrentOrder;
import com.vdurmont.emoji.EmojiParser;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.Month;
import java.util.*;

public class OptionPattern {

    public final static Map<Month, String> MONTHS = new HashMap<>();

    public final static String YES = "yes";
    public final static String NO = "no";

    public final static String BOOK_ROOM = "/book_room";
    public final static String RULES = "/rules";

    public final static String SHABASH = "shabah";
    public final static String ABONENT = "abonent";
    public final static String INFERNO = "inferno";
    public final static String SHIP_HOLD= "ship_hold";
    public final static String DEPOT = "depot";
    public final static String F_STREET = "f_street";
    public final static String ANIME = "anime";
    public final static String GLADIATOR = "gladiator";
    public final static String NIGHT_FLIGHT = "night_flight";

    public final static List<String> ROOMS_1905 = List.of(SHABASH, ABONENT, INFERNO, SHIP_HOLD, DEPOT, F_STREET,
            ANIME, GLADIATOR, NIGHT_FLIGHT);

    public final static Map<String, String> ROOMS_FOR_PRINT = new LinkedHashMap<>();

    public final static Map<String, String> ROOM_NAMES = new LinkedHashMap<>();

    public final static String TODAY = "today";
    public final static String TOMORROW = "tomorrow";
    public final static String CURRENT_MONTH = "current_month";
    public final static String NEXT_MONTH = "next_month";

    public final static List<String> FIRST_TIME_CHOICE = List.of(TODAY, TOMORROW, CURRENT_MONTH, NEXT_MONTH);

    public static SendMessage chooseRoomMenu = new SendMessage();
    public static SendMessage yesNoMenu = new SendMessage();
    public static SendMessage firstDateMenu = new SendMessage();
    public static SendMessage DONE_BIG_BUTTON = new SendMessage();
    public static SendMessage paymentFormMenu = new SendMessage();
    public static Map<Long, CurrentOrder> currentOrders = new HashMap<>();
    public static Map<String, Integer> ROOM_PRICES = new HashMap<>();

    public static int HALF_HOURS;


    public final static String USER_IS_NOT_EXIST = EmojiParser.parseToUnicode("""
            Вы еще не зарегистрировались.
            Сделайте это для полноценного
            доступа к боту - нажмите прямо
            в этом сообщении на /start :point_left:""");


    public final static String LOOK_CURRENT_MONTH = EmojiParser.parseToUnicode("Смотрим текущий месяц :first_quarter_moon_with_face:");
    public final static String LOOK_NEXT_MONTH = EmojiParser.parseToUnicode("Смотрим следующий месяц :first_quarter_moon_with_face:");

    public final static String DONE = "done";
    public final static String DONE_PRINT = "Готово";

    public final static String CHOOSE_PERIOD = EmojiParser.parseToUnicode("""
            Выберите период посещения
            -  внизу текущий расчет стоимости
            заказа. Далее нажмите "Готово". :saxophone:""");

    public final static String HALF_HOUR_PATTERN = "([0-1]\\d|2[0-3]):([0-5]\\d)\\s-\\s([0-1]\\d|2[0-3]):([0-5]\\d)";

    public final static String MARK_TIME_BUTTON = EmojiParser.parseToUnicode(":white_check_mark:");
    public final static String EMPTY_TIME_BUTTON = "  -  ";

    public final static String CASH = "cash";
    public final static String CASH_PRINT = "наличными при заезде";
    public final static String NON_CASH = "non_cash";
    public final static String NON_CASH_PRINT = "банковской картой";

}
