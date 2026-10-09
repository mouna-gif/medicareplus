package com.medicareplus.api;

import static com.medicareplus.api.DomainDtos.*;

import com.medicareplus.service.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController @RequestMapping("/api/prescriptions") @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MEDECIN')")
@Tag(name = "Prescriptions", description = "Transactional prescription and medication-stock workflows")
public class PrescriptionController {
    private final PrescriptionService service;
    @Operation(summary = "Validate a prescription", description = "Checks appointment status, stock, allergies, and explicit overrides before decrementing stock.", responses = @ApiResponse(responseCode = "201", description = "Prescription validated"))
    @PostMapping public ResponseEntity<PrescriptionView> create(@Valid @RequestBody PrescriptionRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.validate(request)); }
    @Operation(summary = "Replace a prescription", description = "Restores previous stock and atomically validates and applies replacement lines.")
    @PutMapping("/{id}") public PrescriptionView replace(@PathVariable Long id,@Valid @RequestBody PrescriptionRequest request) { return service.replace(id,request); }
    @Operation(summary = "Delete a prescription", description = "Deletes a prescription and restores all medication stock.", responses = @ApiResponse(responseCode = "204", description = "Prescription deleted and stock restored"))
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
