package com.example.useractivityservice.service;

import com.example.useractivityservice.dto.ActionsDto;
import com.example.useractivityservice.entity.Actions;
import com.example.useractivityservice.entity.Notifications;
import com.example.useractivityservice.repository.ActionsRepository;
import com.example.useractivityservice.repository.NotificationsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActionServiceTest {

    @Mock
    private ActionsRepository actionRepository;

    @Mock
    private NotificationsRepository notificationRepository;

    @InjectMocks
    private ActionsService actionService;

    private Long userId = 1L;


    @Test
    void testNightOwlTrigger_ThreeNightsInARow() {
        LocalDateTime now = LocalDateTime.now();
        List<Actions> recentActions = Arrays.asList(
                createAction("comment", now.minusDays(2).withHour(2).withMinute(0)), // Night day -2
                createAction("comment", now.minusDays(1).withHour(3).withMinute(30)), // Night day -1
                createAction("comment", now.withHour(4).withMinute(45)) // Night today
        );

        when(actionRepository.findByUserIdAndTimestampAfter(eq(userId), any(LocalDateTime.class)))
                .thenReturn(recentActions);


        actionService.addAction(createDto("comment", now));

        ArgumentCaptor<Notifications> captor = ArgumentCaptor.forClass(Notifications.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals("Кажется, вы сова 🦉", captor.getValue().getMessage());
    }

    @Test
    void testNightOwlTrigger_NotTriggered_LessThanThree() {
        LocalDateTime now = LocalDateTime.now();
        List<Actions> recentActions = Arrays.asList(
                createAction("comment", now.minusDays(1).withHour(3).withMinute(30)),
                createAction("comment", now.withHour(4).withMinute(45))
        );

        when(actionRepository.findByUserIdAndTimestampAfter(userId, now.minus(7, ChronoUnit.DAYS)))
                .thenReturn(recentActions);

        actionService.addAction(createDto("comment", now));

        verify(notificationRepository, never()).save(any(Notifications.class));
    }

    @Test
    void testNightOwlTrigger_NotTriggered_DaytimeComments() {
        LocalDateTime now = LocalDateTime.now();
        List<Actions> recentActions = Arrays.asList(
                createAction("comment", now.minusDays(2).withHour(12).withMinute(0)),
                createAction("comment", now.minusDays(1).withHour(14).withMinute(30)),
                createAction("comment", now.withHour(15).withMinute(45))
        );

        when(actionRepository.findByUserIdAndTimestampAfter(eq(userId), any(LocalDateTime.class)))
                .thenReturn(recentActions);

        actionService.addAction(createDto("comment", now));

        verify(notificationRepository, never()).save(any(Notifications.class));
    }



    @Test
    void testCoffeePatternTrigger_NoPattern() {
        LocalDateTime now = LocalDateTime.now();
        List<Actions> recentActions = Arrays.asList(
                createAction("purchase_coffee", now.with(DayOfWeek.TUESDAY)),
                createAction("purchase_coffee", now.with(DayOfWeek.THURSDAY)),
                createAction("purchase_coffee", now.with(DayOfWeek.SATURDAY))
        );

        when(actionRepository.findByUserIdAndTimestampAfter(eq(userId), any(LocalDateTime.class)))
                .thenReturn(recentActions);

        actionService.addAction(createDto("purchase_coffee", now));

        verify(notificationRepository, never()).save(any(Notifications.class));
    }

    @Test
    void testRoutineTrigger_LowVariance() {
        LocalDateTime now = LocalDateTime.now();
        List<Actions> recentActions = Arrays.asList(
                createAction("any", now.minusDays(4).withHour(10)),
                createAction("any", now.minusDays(3).withHour(10)),
                createAction("any", now.minusDays(2).withHour(11)),
                createAction("any", now.minusDays(1).withHour(10)),
                createAction("any", now.withHour(11))
        );

        when(actionRepository.findByUserIdAndTimestampAfter(userId, now.minus(7, ChronoUnit.DAYS)))
                .thenReturn(recentActions);
        actionService.addAction(createDto("any", now));

        ArgumentCaptor<Notifications> captor = ArgumentCaptor.forClass(Notifications.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals("Вы предсказуемы в рутине? Вот сюрприз для разнообразия!", captor.getValue().getMessage());
    }

    @Test
    void testRoutineTrigger_HighVariance() {
        LocalDateTime now = LocalDateTime.now();
        List<Actions> recentActions = Arrays.asList(
                createAction("any", now.minusDays(4).withHour(2)),
                createAction("any", now.minusDays(3).withHour(8)),
                createAction("any", now.minusDays(2).withHour(14)),
                createAction("any", now.minusDays(1).withHour(20)),
                createAction("any", now.withHour(23))
        );

        when(actionRepository.findByUserIdAndTimestampAfter(eq(userId), any(LocalDateTime.class)))
                .thenReturn(recentActions);

        actionService.addAction(createDto("any", now));

        verify(notificationRepository, never()).save(any(Notifications.class));
    }

    @Test
    void testRoutineTrigger_LessThanFiveActions() {
        LocalDateTime now = LocalDateTime.now();
        List<Actions> recentActions = Arrays.asList(
                createAction("any", now.minusDays(3).withHour(10)),
                createAction("any", now.minusDays(2).withHour(10)),
                createAction("any", now.minusDays(1).withHour(10)),
                createAction("any", now.withHour(10))
        );

        when(actionRepository.findByUserIdAndTimestampAfter(eq(userId), any(LocalDateTime.class)))
                .thenReturn(recentActions);


        actionService.addAction(createDto("any", now));

        verify(notificationRepository, never()).save(any(Notifications.class));
    }

    private Actions createAction(String type, LocalDateTime timestamp) {
        Actions action = new Actions();
        action.setUserId(userId);
        action.setActionType(type);
        action.setTimestamp(timestamp);
        return action;
    }

    private ActionsDto createDto(String type, LocalDateTime timestamp) {
        ActionsDto dto = new ActionsDto();
        dto.setUserId(userId);
        dto.setActionType(type);
        dto.setTimestamp(timestamp);
        return dto;
    }
}
