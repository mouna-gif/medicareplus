package com.medicareplus.api;

import static com.medicareplus.api.DomainDtos.*;

import com.medicareplus.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController @RequestMapping("/api/patients") @RequiredArgsConstructor
@Tag(name = "Patients", description = "Patient lifecycle and logical archive operations")
public class PatientController {
    private final PatientService service;
    @Operation(summary = "Search active patients", description = "Returns a paginated role-scoped patient list with filtering and sorting.")
    @GetMapping public Page<PatientView> search(@RequestParam(defaultValue="") String q, @PageableDefault(size=20,sort="nom") Pageable pageable) { return service.search(q,pageable); }
    @Operation(summary = "Get a patient", description = "Returns one patient if the authenticated role has access.")
    @GetMapping("/{id}") public PatientView get(@PathVariable Long id) { return service.get(id); }
    @Operation(summary = "Create a patient", description = "Creates a patient and medical record atomically.", responses = @ApiResponse(responseCode = "201", description = "Patient and medical record created"))
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE')") public ResponseEntity<PatientView> create(@Valid @RequestBody PatientRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request)); }
    @Operation(summary = "Update a patient", description = "Updates a non-archived patient while preserving unique email rules.")
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE')") public PatientView update(@PathVariable Long id,@Valid @RequestBody PatientRequest request) { return service.update(id,request); }
    @Operation(summary = "Archive a patient", description = "Performs a logical archive without deleting medical history.", responses = @ApiResponse(responseCode = "204", description = "Patient archived"))
    @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE')") public ResponseEntity<Void> archive(@PathVariable Long id) { service.archive(id); return ResponseEntity.noContent().build(); }
}
