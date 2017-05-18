package TelegramBot;

import com.mashape.unirest.http.Headers;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.body.Body;
import com.sun.xml.internal.ws.util.StringUtils;
import org.json.JSONObject;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

public class Bot extends TelegramLongPollingBot {

    private static final String URL = "http://localhost:8080/";
    private static String login, password;
    private static Map<String, String> headersMap = new HashMap<>();

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String answerMessage = "Что-то пошло не так!";
            String userMessage = update.getMessage().getText();
            try {
                if (userMessage.startsWith("/start"))
                    answerMessage = "Привет!";
                else if (userMessage.startsWith("/login")) {
                    String[] query = userMessage.split(" ");
                    if (query.length != 3)
                        answerMessage = "Мало аргументов. Формат: /login <username> <password>";
                    else
                        answerMessage = login(query);
                } else if (userMessage.startsWith("/allMessages")) {
                    if (login != null)
                    answerMessage = getAllMessages();
                    else
                        answerMessage = "Сначала войдите!";
                }
                else if (userMessage.startsWith("/sendMessage")){
                    String[] query = userMessage.split(" ", 2);
                    if (query.length != 2){
                        answerMessage = "Мало аргументов. Формат: /sendMessage <message>";
                    }
                    else if (login != null)
                        answerMessage = addMessage(query[1]);
                    else
                        answerMessage = "Сначала войдите!";
                }
                else if (userMessage.startsWith("/help"))
                    answerMessage = "/help\n/login\n/allMessages\n/sendMessage";
            } catch (UnirestException e) {
                e.printStackTrace();
            }

            System.out.println(update.getMessage().getText());
            SendMessage message = new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText(answerMessage);
            try {
                sendMessage(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "PseudoTwitterBot";
    }

    @Override
    public String getBotToken() {
        // TODO
        return "336133315:AAGVBFDJdHZDc6VbQzf-KUWI43sBNvb8nr4";
    }

    private String getAllMessages() throws UnirestException {
        JsonNode a = Unirest.get(URL + "get_all_messages")
                .headers(headersMap)
                .asJson().getBody();
        return String.join(", ",
                a.getArray().toList().stream()
                        .map(x->((HashMap)x).get("message").toString())
                        .collect(Collectors.toList()));
    }

    private String login(String[] query) throws UnirestException {
        login = query[1];
        password = query[2];
        Unirest.post(URL + "/register")
                .field("username", query[1])
                .field("password", query[2])
                .asString();

        Headers headers = Unirest.post(URL + "/login")
                .field("username", query[1])
                .field("password", query[2])
                .asString()
                .getHeaders();
        headersMap.clear();
        for (String headerName : headers.keySet()) {
            headersMap.put(headerName, headers.getFirst(headerName));
        }
        return "Вы успешно вошли в аккаунт!";
    }

    private String addMessage(String msg) throws UnirestException {
        Unirest.post(URL + "/add_message")
                .field("msg", msg)
                .asString();
        return "Сообщение успешно отправлено!";
    }
}