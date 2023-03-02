package io.cruii;

import io.cruii.push.pusher.impl.TelegramBotPusher;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;


@Log4j2
public class PushTest {



    @Test
    public void testTelegram() {
        TelegramBotPusher telegramBotPusher = new TelegramBotPusher("123", "123");
        telegramBotPusher.push("123\n123");
    }
}
