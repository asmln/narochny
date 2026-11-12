package com.asmln.narochny.ingestion;

import com.asmln.narochny.domain.NotificationStatus;
import com.asmln.narochny.ingestion.dto.NotificationRequest;
import com.asmln.narochny.ingestion.dto.NotificationResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationIngestionService notificationIngestionService;

    public NotificationController(NotificationIngestionService notificationIngestionService) {
        this.notificationIngestionService = notificationIngestionService;
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @Valid @RequestBody NotificationRequest request
            ) throws Exception {
        var notification = new IngestNotification(
                idempotencyKey,
                request.userId(),
                request.channel(),
                request.priority(),
                request.body()
        );
        var result = notificationIngestionService.process(notification);
        return switch (result) {
            case NotificationIngestionService.Created(UUID id) -> ResponseEntity
                    .accepted()
                    .location(URI.create("/notifications/" + id))
                    .body(new NotificationResponse(id, NotificationStatus.ACCEPTED));
            case NotificationIngestionService.Duplicate(UUID id) -> ResponseEntity
                    .ok()
                    .location(URI.create("/notifications/" + id))
                    .body(new NotificationResponse(id, NotificationStatus.ACCEPTED));
            case NotificationIngestionService.Conflict(String expectedHash, String actualHash) -> {
                    var problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
                    problem.setTitle("Idempotency key conflict");
                    problem.setDetail("Same Idempotency-Key was used with a different request body.");
                    problem.setProperty("expected_hash", expectedHash);
                    problem.setProperty("actual_hash", actualHash);
                    yield ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
            }
        };
    }

}
