package com.anpr.parking.common;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final JdbcTemplate jdbc;

    public AuditService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public void log(String eventType, Long actorUserId, String targetType, Object targetId,
                    String result, String reason) {
        jdbc.update("""
                INSERT INTO audit_logs (event_type, actor_user_id, target_type, target_id, result, reason)
                VALUES (?, ?, ?, ?, ?, ?)""",
                eventType, actorUserId, targetType,
                targetId == null ? null : String.valueOf(targetId), result, reason);
    }
}
