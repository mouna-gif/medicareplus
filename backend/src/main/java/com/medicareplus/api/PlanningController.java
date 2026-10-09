package com.medicareplus.api;

import static com.medicareplus.api.DomainDtos.*;

import com.medicareplus.service.PlanningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController @RequiredArgsConstructor
@Tag(name = "Planning", description = "Availability and appointment scheduling workflows")
public class PlanningController {
    private final PlanningService service;
    @Operation(summary = "Create an appointment", description = "Validates future date, availability, role scope, and doctor/patient conflicts.", responses = @ApiResponse(responseCode = "201", description = "Appointment created"))
    @PostMapping("/api/rendez-vous") @PreAuthorize("hasAnyRole('ADMIN','MEDECIN','SECRETAIRE')")
    ResponseEntity<RendezVousView> create(@Valid @RequestBody RendezVousRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request)); }
    @Operation(summary = "Change appointment status", description = "Applies lifecycle and late-cancellation rules.")
    @PatchMapping("/api/rendez-vous/{id}/statut") @PreAuthorize("hasAnyRole('ADMIN','MEDECIN','SECRETAIRE')")
    RendezVousView status(@PathVariable Long id,@Valid @RequestBody StatutRendezVousRequest request) { return service.changeStatus(id,request.statut()); }
    @Operation(summary = "Create doctor availability", description = "Creates a non-overlapping weekly availability range.", responses = @ApiResponse(responseCode = "201", description = "Availability created"))
    @PostMapping("/api/disponibilites") @PreAuthorize("hasAnyRole('ADMIN','MEDECIN','SECRETAIRE')")
    ResponseEntity<DisponibiliteView> availability(@Valid @RequestBody DisponibiliteRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.addAvailability(request)); }
}
