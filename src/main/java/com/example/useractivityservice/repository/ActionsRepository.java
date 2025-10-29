package com.example.useractivityservice.repository;

import com.example.useractivityservice.entity.Actions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActionsRepository extends JpaRepository<Actions, Long> {

    List<Actions> findByUserId(Long id);

    List<Actions> findByUserIdAndTimestampAfter(Long userId, LocalDateTime after);
}
