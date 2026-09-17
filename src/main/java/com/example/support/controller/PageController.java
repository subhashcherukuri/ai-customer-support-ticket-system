package com.example.support.controller;

import com.example.support.dto.*;
import com.example.support.enums.TicketPriority;
import com.example.support.exception.AccessDeniedException;
import com.example.support.exception.ResourceNotFoundException;
import com.example.support.service.TicketService;
import com.example.support.exception.BadRequestException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.example.support.exception.DuplicateResourceException;
import com.example.support.service.UserService;
import jakarta.validation.Valid;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import com.example.support.enums.TicketStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import com.example.support.entity.User;
import com.example.support.enums.Role;

@Controller
public class PageController {

    private final UserService userService;
    private final TicketService ticketService;

    public PageController(
            UserService userService,
            TicketService ticketService) {

        this.userService = userService;
        this.ticketService = ticketService;
    }

    @GetMapping("/")
    public String home(Authentication authentication) {

        if (authentication == null) {
            return "redirect:/login";
        }

        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {

            return "redirect:/admin/dashboard";
        }

        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_AGENT"))) {

            return "redirect:/agent/dashboard";
        }

        return "redirect:/customer/dashboard";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute RegisterRequest request,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute(
                    "error",
                    result.getFieldError().getDefaultMessage()
            );
            return "register";
        }

        try {
            userService.registerCustomer(request);
        } catch (DuplicateResourceException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }

        return "redirect:/login?registered";
    }
    @GetMapping("/customer/dashboard")
    public String customerDashboard(
            Authentication authentication,
            Model model) {

        String email = authentication.getName();

        List<TicketResponse> tickets =
                ticketService.getCustomerTickets(email);

        model.addAttribute("tickets", tickets);
        model.addAttribute("loggedInUser", userService.getUserByEmail(email));

        return "customer/dashboard";
    }

    @GetMapping("/customer/tickets/create")
    public String createTicketPage(
            Authentication authentication,
            Model model) {

        String email = authentication.getName();

        model.addAttribute(
                "loggedInUser",
                userService.getUserByEmail(email)
        );

        return "customer/create-ticket";
    }

    @PostMapping("/customer/tickets")
    public String createTicket(
            @Valid @ModelAttribute CreateTicketRequest request,
            BindingResult result,
            Authentication authentication,
            Model model) {

        if (result.hasErrors()) {

            model.addAttribute(
                    "error",
                    result.getFieldError().getDefaultMessage()
            );

            String email = authentication.getName();

            model.addAttribute(
                    "loggedInUser",
                    userService.getUserByEmail(email)
            );

            return "customer/create-ticket";
        }

        String email = authentication.getName();

        ticketService.createTicket(request,email);

        return "redirect:/customer/dashboard";
    }

    @GetMapping("/customer/tickets")
    public String customerTickets(
            Authentication authentication,
            Model model) {

        String email = authentication.getName();

        List<TicketResponse> tickets =
                ticketService.getCustomerTickets(email);

        model.addAttribute("tickets", tickets);

        model.addAttribute(
                "loggedInUser",
                userService.getUserByEmail(email)
        );

        return "customer/tickets";
    }

    @GetMapping("/customer/tickets/{ticketId}")
    public String customerTicketDetails(
            @PathVariable Long ticketId,
            Authentication authentication,
            Model model) {

        String email = authentication.getName();

        TicketResponse ticket =
                ticketService.getTicket(ticketId, email);

        List<MessageResponse> messages =
                ticketService.getTicketMessages(ticketId, email);

        model.addAttribute("ticket", ticket);
        model.addAttribute("messages", messages);

        model.addAttribute(
                "loggedInUser",
                userService.getUserByEmail(email)
        );

        return "customer/ticket-details";
    }

    @PostMapping("/customer/tickets/{ticketId}/messages")
    public String sendCustomerMessage(
            @PathVariable Long ticketId,
            @Valid @ModelAttribute CreateMessageRequest request,
            BindingResult result,
            Authentication authentication) {

        if (result.hasErrors()) {
            return "redirect:/customer/tickets/" + ticketId;
        }

        String email = authentication.getName();

        ticketService.addMessage(
                ticketId,
                email,
                request
        );

        return "redirect:/customer/tickets/" + ticketId;
    }

    @GetMapping("/agent/dashboard")
    public String agentDashboard(
            Authentication authentication,
            Model model) {

        String email = authentication.getName();

        List<TicketResponse> tickets =
                ticketService.getAgentTickets(email);

        model.addAttribute("tickets", tickets);

        model.addAttribute(
                "loggedInUser",
                userService.getUserByEmail(email)
        );

        return "agent/dashboard";
    }

    @GetMapping("/agent/tickets/{ticketId}")
    public String agentTicketDetails(
            @PathVariable Long ticketId,
            Authentication authentication,
            Model model) {

        String email = authentication.getName();

        TicketResponse ticket =
                ticketService.getTicket(ticketId, email);

        List<MessageResponse> messages =
                ticketService.getTicketMessages(ticketId, email);

        Map<Long, String> suggestedReplies = new HashMap<>();

        for (MessageResponse message : messages) {

            String suggestedReply = null;

            try {
                // Generate suggestions only for customer messages
                User sender =
                        userService.getUserById(message.getSenderId());

                if (sender.getRole() == Role.CUSTOMER) {

                    suggestedReply =
                            ticketService.generateSuggestedReplyForMessage(
                                    ticketId,
                                    message.getId(),
                                    email
                            );
                }

            } catch (Exception e) {

                suggestedReply =
                        "Unable to generate a suggested reply for this message.";
            }

            if (suggestedReply != null) {
                suggestedReplies.put(
                        message.getId(),
                        suggestedReply
                );
            }
        }

        model.addAttribute("ticket", ticket);
        model.addAttribute("messages", messages);
        model.addAttribute("suggestedReplies", suggestedReplies);
        model.addAttribute(
                "loggedInUser",
                userService.getUserByEmail(email)
        );

        return "agent/ticket-details";
    }

    @PostMapping("/agent/tickets/{ticketId}/messages")
    public String sendAgentMessage(
            @PathVariable Long ticketId,
            @Valid @ModelAttribute CreateMessageRequest request,
            BindingResult result,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    result.getFieldError().getDefaultMessage()
            );

            return "redirect:/agent/tickets/" + ticketId;
        }

        try {

            String email = authentication.getName();

            ticketService.addMessage(
                    ticketId,
                    email,
                    request
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Reply sent successfully"
            );

        } catch (BadRequestException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

        } catch (AccessDeniedException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/agent/tickets/" + ticketId;
    }

    @PostMapping("/agent/tickets/{ticketId}/priority")
    public String updateAgentTicketPriority(
            @PathVariable Long ticketId,
            @RequestParam TicketPriority priority,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {

            String email = authentication.getName();

            ticketService.updatePriority(
                    ticketId,
                    priority,
                    email
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Ticket priority updated successfully"
            );

        } catch (BadRequestException | AccessDeniedException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/agent/tickets/" + ticketId;
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(
            Authentication authentication,
            Model model) {

        String email = authentication.getName();

        List<TicketResponse> tickets =
                ticketService.getAllTickets();

        long totalTickets = tickets.size();

        long openTickets = tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.OPEN)
                .count();

        long inProgressTickets = tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS)
                .count();

        long resolvedTickets = tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.RESOLVED)
                .count();
        long closedTickets = tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.CLOSED)
                .count();

        model.addAttribute(
                "totalTickets",
                totalTickets
        );

        model.addAttribute(
                "openTickets",
                openTickets
        );

        model.addAttribute(
                "inProgressTickets",
                inProgressTickets
        );

        model.addAttribute(
                "resolvedTickets",
                resolvedTickets
        );

        model.addAttribute("closedTickets", closedTickets);

        model.addAttribute(
                "loggedInUser",
                userService.getUserByEmail(email)
        );

        return "admin/dashboard";
    }

    @GetMapping("/admin/tickets")
    public String adminTickets(
            Authentication authentication,
            Model model) {

        String email = authentication.getName();

        model.addAttribute(
                "tickets",
                ticketService.getAllTickets()
        );

        model.addAttribute(
                "loggedInUser",
                userService.getUserByEmail(email)
        );

        return "admin/tickets";
    }

    @GetMapping("/admin/tickets/{ticketId}")
    public String adminTicketDetails(
            @PathVariable Long ticketId,
            Authentication authentication,
            Model model) {

        String email = authentication.getName();

        TicketResponse ticket =
                ticketService.getTicket(ticketId, email);

        List<MessageResponse> messages =
                ticketService.getTicketMessages(ticketId, email);

        model.addAttribute("ticket", ticket);

        model.addAttribute("messages", messages);

        model.addAttribute(
                "agents",
                userService.getAllAgents()
        );

        model.addAttribute(
                "loggedInUser",
                userService.getUserByEmail(email)
        );

        return "admin/ticket-details";
    }

    @PostMapping("/admin/tickets/{ticketId}/messages")
    public String sendAdminMessage(
            @PathVariable Long ticketId,
            @Valid @ModelAttribute CreateMessageRequest request,
            BindingResult result,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    result.getFieldError().getDefaultMessage()
            );

            return "redirect:/admin/tickets/" + ticketId;
        }

        try {

            String email = authentication.getName();

            ticketService.addMessage(
                    ticketId,
                    email,
                    request
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Reply sent successfully"
            );

        } catch (BadRequestException | AccessDeniedException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/admin/tickets/" + ticketId;
    }

    @PostMapping("/admin/tickets/{ticketId}/status")
    public String updateAdminTicketStatus(
            @PathVariable Long ticketId,
            @RequestParam TicketStatus status,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {

            String email = authentication.getName();

            ticketService.updateStatus(
                    ticketId,
                    status,
                    email
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Ticket status updated successfully"
            );

        } catch (BadRequestException | AccessDeniedException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/admin/tickets/" + ticketId;
    }

    @PostMapping("/admin/tickets/{ticketId}/priority")
    public String updateAdminTicketPriority(
            @PathVariable Long ticketId,
            @RequestParam TicketPriority priority,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {

            String email = authentication.getName();

            ticketService.updatePriority(
                    ticketId,
                    priority,
                    email
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Ticket priority updated successfully"
            );

        } catch (BadRequestException | AccessDeniedException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/admin/tickets/" + ticketId;
    }

    @GetMapping("/admin/agents")
    public String adminAgents(
            Authentication authentication,
            Model model) {

        String email = authentication.getName();

        model.addAttribute(
                "agents",
                userService.getAllAgents()
        );

        model.addAttribute(
                "agentRequest",
                new CreateAgentRequest()
        );

        model.addAttribute(
                "loggedInUser",
                userService.getUserByEmail(email)
        );

        return "admin/agents";
    }

    @PostMapping("/agent/tickets/{ticketId}/status")
    public String updateAgentTicketStatus(
            @PathVariable Long ticketId,
            @RequestParam TicketStatus status,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {

            String email = authentication.getName();

            ticketService.updateStatus(
                    ticketId,
                    status,
                    email
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Ticket status updated successfully"
            );

        } catch (BadRequestException |
                 AccessDeniedException |
                 ResourceNotFoundException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/agent/tickets/" + ticketId;
    }


}