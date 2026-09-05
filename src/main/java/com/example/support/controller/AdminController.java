package com.example.support.controller;

import com.example.support.dto.CreateAgentRequest;
import com.example.support.dto.UserResponse;
import com.example.support.entity.User;
import com.example.support.enums.Role;
import com.example.support.exception.AccessDeniedException;
import com.example.support.exception.BadRequestException;
import com.example.support.exception.DuplicateResourceException;
import com.example.support.exception.ResourceNotFoundException;
import com.example.support.repository.UserRepository;
import com.example.support.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TicketService ticketService;

    public AdminController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TicketService ticketService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.ticketService = ticketService;
    }

    @PostMapping("/agents")
    @PreAuthorize("hasRole('ADMIN')")
    public String createAgent(
            @Valid @ModelAttribute CreateAgentRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    result.getFieldError().getDefaultMessage()
            );

            return "redirect:/admin/agents";
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Email already registered"
            );

            return "redirect:/admin/agents";
        }

        User agent = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.AGENT)
                .build();

        userRepository.save(agent);

        redirectAttributes.addFlashAttribute(
                "success",
                "Agent created successfully"
        );

        return "redirect:/admin/agents";
    }

    @PostMapping("/tickets/{ticketId}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public String assignAgent(
            @PathVariable Long ticketId,
            @RequestParam Long agentId,
            RedirectAttributes redirectAttributes) {

        try {

            ticketService.assignAgent(ticketId, agentId);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Agent assigned successfully"
            );

        } catch (BadRequestException | AccessDeniedException |
                 ResourceNotFoundException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/admin/tickets/" + ticketId;
    }
}