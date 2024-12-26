package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationRepository;
import pro.sky.telegrambot.service.MessageService;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {
    private final MessageService messageService;
    private final NotificationRepository notificationRepository;

    public TelegramBotUpdatesListener(MessageService messageService, NotificationRepository notificationRepository) {
        this.messageService = messageService;
        this.notificationRepository = notificationRepository;
    }


    private final String hello = "Hello";
    private final Pattern notificationTaskPattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)");
    private final DateTimeFormatter notificationDateTimeFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);

            String massage = update.message().text();
            Long chatId = update.message().chat().id();
            Matcher massageMatcher = notificationTaskPattern.matcher(massage);

            if (massage.equals("/start")) {
                messageService.answer(chatId, hello);

            } else if (massageMatcher.matches()) {
                NotificationTask notificationTask = new NotificationTask();
                notificationTask.setChatId(chatId);
                notificationTask.setText(massageMatcher.group(3));
                notificationTask.setNotificationDateTime(LocalDateTime.parse(
                        massageMatcher.group(1),
                        notificationDateTimeFormat
                ));
                notificationRepository.save(notificationTask);
                messageService.answer(chatId, "Message is saved");
            } else {
                messageService.answer(chatId, "Invalid message");
            }

        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

}


