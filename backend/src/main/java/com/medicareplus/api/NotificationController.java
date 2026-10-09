package com.medicareplus.api;

import static com.medicareplus.api.DomainDtos.*;

import com.medicareplus.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController @RequestMapping("/api/notifications") @RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Notifications", description = "Asynchronous notification delivery and persistent delivery history")
public class NotificationController {
    private final NotificationService service;
    @Operation(summary = "Send a notification", description = "Queues an email or SMS with retry and persistent outcome logging.", responses = @ApiResponse(responseCode = "202", description = "Notification accepted for asynchronous delivery"))
    @PostMapping ResponseEntity<Void> send(@Valid @RequestBody NotificationRequest request) {
        service.send(request);
        return ResponseEntity.accepted().build();
    }
    @Operation(summary = "List notification history", description = "Returns paginated success and failure delivery logs.")
    @GetMapping Page<NotificationView> history(@PageableDefault(size=20) Pageable pageable) {
        return service.history(pageable);
    }
}
