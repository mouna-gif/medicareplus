package com.medicareplus.api;

import static com.medicareplus.api.DomainDtos.*;

import com.medicareplus.service.AdministrationService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController @RequestMapping("/api/admin") @RequiredArgsConstructor @PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administration", description = "Administrator-only user and physician management")
public class AdministrationController {
    private final AdministrationService service;
    @Operation(summary = "List users", description = "Returns persisted MediCare+ user accounts and roles.")
    @GetMapping("/utilisateurs") List<UtilisateurView> users() { return service.users(); }
    @Operation(summary = "Create a user", description = "Creates a database user with a BCrypt password.", responses = @ApiResponse(responseCode = "201", description = "User created"))
    @PostMapping("/utilisateurs") ResponseEntity<UtilisateurView> user(@Valid @RequestBody UtilisateurRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.createUser(request)); }
    @Operation(summary = "Deactivate a user", description = "Disables authentication and revokes the refresh token.", responses = @ApiResponse(responseCode = "204", description = "User deactivated"))
    @DeleteMapping("/utilisateurs/{id}") ResponseEntity<Void> deactivate(@PathVariable Long id) { service.deactivateUser(id); return ResponseEntity.noContent().build(); }
    @Operation(summary = "List physicians", description = "Returns physician profiles linked to MEDECIN users.")
    @GetMapping("/medecins") List<MedecinView> doctors() { return service.doctors(); }
    @Operation(summary = "Create a physician profile", description = "Links a physician profile to an existing MEDECIN user.", responses = @ApiResponse(responseCode = "201", description = "Physician created"))
    @PostMapping("/medecins") ResponseEntity<MedecinView> doctor(@Valid @RequestBody MedecinRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.createDoctor(request)); }
    @Operation(summary = "Deactivate a physician", description = "Blocks deactivation while future non-cancelled appointments exist.", responses = @ApiResponse(responseCode = "204", description = "Physician deactivated"))
    @DeleteMapping("/medecins/{id}") ResponseEntity<Void> deactivateDoctor(@PathVariable Long id) { service.deactivateDoctor(id); return ResponseEntity.noContent().build(); }
}
