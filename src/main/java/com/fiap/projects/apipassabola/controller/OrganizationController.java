package com.fiap.projects.apipassabola.controller;

import com.fiap.projects.apipassabola.dto.request.OrganizationRequest;
import com.fiap.projects.apipassabola.dto.response.OrganizationResponse;
import com.fiap.projects.apipassabola.dto.response.PlayerResponse;
import com.fiap.projects.apipassabola.dto.response.SpectatorResponse;
import com.fiap.projects.apipassabola.entity.Organization;
import com.fiap.projects.apipassabola.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrganizationController {
    
    private final OrganizationService organizationService;
    
    @GetMapping
    public ResponseEntity<Page<OrganizationResponse>> getAllOrganizations(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<OrganizationResponse> organizations = organizationService.findAll(pageable);
        return ResponseEntity.ok(organizations);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> getOrganizationById(@PathVariable Long id) {
        OrganizationResponse organization = organizationService.findById(id);
        return ResponseEntity.ok(organization);
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<OrganizationResponse> getOrganizationByUsername(@PathVariable String username) {
        OrganizationResponse organization = organizationService.findByUsername(username);
        return ResponseEntity.ok(organization);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<OrganizationResponse>> searchOrganizationsByName(
            @RequestParam String name,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<OrganizationResponse> organizations = organizationService.findByName(name, pageable);
        return ResponseEntity.ok(organizations);
    }
    
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<OrganizationResponse> updateOrganization(
            @PathVariable Long id,
            @Valid @RequestBody OrganizationRequest request) {
        OrganizationResponse updatedOrganization = organizationService.update(id, request);
        return ResponseEntity.ok(updatedOrganization);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<Void> deleteOrganization(@PathVariable Long id) {
        organizationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
