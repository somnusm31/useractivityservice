package com.example.useractivityservice.repository;

import com.example.useractivityservice.entity.Notifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationsRepository extends JpaRepository<Notifications, Long> {
}
