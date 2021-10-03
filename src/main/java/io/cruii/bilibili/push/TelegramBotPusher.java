package io.cruii.bilibili.push;

import cn.hutool.http.HttpRequest;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author cruii
 * Created on 2021/10/03
 */
public class TelegramBotPusher implements Pusher{
    private final String token;

    private final String chatId;

    public TelegramBotPusher(String token, String chatId) {
        this.token = token;
        this.chatId = chatId;
    }

    @Override
    public void push(String content) {
        String url = UriComponentsBuilder
                .fromHttpUrl("https://api.telegram.org/bot{token}/sendMessage?chat_id={chatId}&text={content}")
                .build(token, chatId, content).toString();
        HttpRequest.post(url).execute();
    }
}
