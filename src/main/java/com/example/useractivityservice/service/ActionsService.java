package com.example.useractivityservice.service;

import com.example.useractivityservice.dto.ActionsDto;
import com.example.useractivityservice.dto.NotificationsDto;
import com.example.useractivityservice.entity.Actions;
import com.example.useractivityservice.entity.Notifications;
import com.example.useractivityservice.repository.ActionsRepository;
import com.example.useractivityservice.repository.NotificationsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActionsService {
    private final ActionsRepository actionsRepository;
    private final NotificationsRepository notificationsRepository;

    public void addAction(ActionsDto dto) {
        Actions action = new Actions();
        action.setUserId(dto.getUserId());
        action.setActionType(dto.getActionType());
        action.setTimestamp(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now());
        actionsRepository.save(action);

        analyzePatterns(dto.getUserId());
    }

    private void analyzePatterns(Long userId) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minus(7, ChronoUnit.DAYS);
        List<Actions> recentActions = actionsRepository.findByUserIdAndTimestampAfter(userId, oneWeekAgo);

        // Trigger 1: Кажется вы сова (3 ночи подряд пишет комменты)
        Map<LocalDate, Long> nightComments = recentActions.stream()
                .filter(a -> "comment".equals(a.getActionType()))
                .filter(a -> a.getTimestamp().getHour() >= 0 && a.getTimestamp().getHour() < 6)
                .collect(Collectors.groupingBy(a -> a.getTimestamp().toLocalDate(), Collectors.counting()));

        boolean isNightOwl = nightComments.keySet().stream()
                .sorted()
                .mapToInt(date -> nightComments.getOrDefault(date, 0L).intValue())
                .skip(Math.max(0, nightComments.size() - 3))
                .sum() >= 3; // Как минимум 3 за последние 3 дня, но упрощено до суммы >=3 для демонстрации

        if (isNightOwl) {
            saveNotification(userId, "Кажется, вы сова 🦉");
        }

        // Trigger 2: Покупает кофе в Пн, Ср, Пт -> предложить скидку во Вт
        Map<DayOfWeek, Long> coffeeDays = recentActions.stream()
                .filter(a -> "purchase_coffee".equals(a.getActionType()))
                .collect(Collectors.groupingBy(a -> a.getTimestamp().getDayOfWeek(), Collectors.counting()));

        boolean coffeePattern = coffeeDays.getOrDefault(DayOfWeek.MONDAY, 0L) > 0 &&
                coffeeDays.getOrDefault(DayOfWeek.WEDNESDAY, 0L) > 0 &&
                coffeeDays.getOrDefault(DayOfWeek.FRIDAY, 0L) > 0;

        if (coffeePattern && LocalDate.now().getDayOfWeek() == DayOfWeek.TUESDAY) {
            saveNotification(userId, "Предлагаем скидку на кофе во вторник!");
        }

        // Original Trigger 3: Рутинное обнаружение (низкая разница во времени выполнения, например, действия, сгруппированные в течение 1 часа по 5 действиям
        List<LocalDateTime> timestamps = recentActions.stream()
                .map(Actions::getTimestamp)
                .sorted()
                .toList();

        if (timestamps.size() >= 5) {
            double averageHour = timestamps.stream().mapToInt(t -> t.getHour()).average().orElse(0);
            double variance = timestamps.stream().mapToDouble(t -> Math.pow(t.getHour() - averageHour, 2)).average().orElse(0);
            if (variance < 1.0) { // Low variance, routine
                saveNotification(userId, "Вы предсказуемы в рутине? Вот сюрприз для разнообразия!");
            }
        }
    }

    private boolean alreadySentToday(Long userId, String message) {
        return notificationsRepository.findAll().stream()
                .anyMatch(n -> n.getUserId().equals(userId)
                        && n.getMessage().equals(message)
                        && n.getCreatedAt().toLocalDate().equals(LocalDate.now()));
    }


    private void saveNotification(Long userId, String message) {
        if (!alreadySentToday(userId, message)) {
            notificationsRepository.save(new Notifications(message, userId));
        }
    }

    public List<NotificationsDto> getAllNotifications() {
        return notificationsRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private NotificationsDto toDto(Notifications entity) {
        NotificationsDto dto = new NotificationsDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setMessage(entity.getMessage());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
