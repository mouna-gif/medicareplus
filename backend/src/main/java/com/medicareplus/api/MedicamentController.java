package com.medicareplus.api;

import static com.medicareplus.api.DomainDtos.*;

import com.medicareplus.service.MedicamentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController @RequestMapping("/api/medicaments") @RequiredArgsConstructor
@Tag(name = "Medications", description = "Medication catalog, prices, stock, and low-stock state")
public class MedicamentController {
    private final MedicamentService service;
    @Operation(summary = "List medications", description = "Returns medication stock and low-stock indicators.")
    @GetMapping List<MedicamentView> all() { return service.all(); }
    @Operation(summary = "Create a medication", description = "Creates an administrator-managed medication.", responses = @ApiResponse(responseCode = "201", description = "Medication created"))
    @PostMapping @PreAuthorize("hasRole('ADMIN')") ResponseEntity<MedicamentView> create(@Valid @RequestBody MedicamentRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request)); }
    @Operation(summary = "Update a medication", description = "Locks and updates medication metadata, pricing, and stock.")
    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") MedicamentView update(@PathVariable Long id,@Valid @RequestBody MedicamentRequest request) { return service.update(id,request); }
}
