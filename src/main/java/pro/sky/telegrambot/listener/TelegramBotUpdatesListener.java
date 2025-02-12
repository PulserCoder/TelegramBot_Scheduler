package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotifTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;
    @Autowired
    private NotifTaskRepository notifTaskRepository;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            Pattern PATTERN = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})\\s+(.+)");
            DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            Matcher matcher = PATTERN.matcher(update.message().text());

            if (update.message().text().equals("/start")) {
                System.out.println(1);
                SendMessage message = new SendMessage(update.message().chat().id(), "Саламчик всем бродягам");
                telegramBot.execute(message);
            } else if (matcher.matches()) {
                LocalDateTime dateTime = LocalDateTime.parse(matcher.group(1), FORMATTER);
                String text = matcher.group(2);
                NotificationTask notificationTask = new NotificationTask();
                notificationTask.setText(text);
                notificationTask.setChatId(update.message().chat().id());
                notificationTask.setDateTime(dateTime);
                notifTaskRepository.save(notificationTask);
                SendMessage message = new SendMessage(update.message().chat().id(), "Напоминание успешно добавлено");
                telegramBot.execute(message);
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void notification() {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<NotificationTask> toSend = notifTaskRepository.findAllByDateTime(dateTime);
        toSend.forEach(notificationTask -> {
            SendMessage message = new SendMessage(notificationTask.getChatId(), notificationTask.getText());
            telegramBot.execute(message);
        });

    }

}
