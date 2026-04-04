package net.ecommerce.springboot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import net.ecommerce.springboot.dto.ChatbotReplyDTO;
import net.ecommerce.springboot.dto.ChatbotRequestDTO;
import net.ecommerce.springboot.service.ChatbotService;

@RestController
@RequestMapping("/api/v1/chatbot")
public class ChatbotController {

	private final ChatbotService chatbotService;

	public ChatbotController(ChatbotService chatbotService) {
		this.chatbotService = chatbotService;
	}

	@PostMapping("/reply")
	public ResponseEntity<ChatbotReplyDTO> reply(@Valid @RequestBody ChatbotRequestDTO body) {
		return ResponseEntity.ok(chatbotService.reply(body.getMessage()));
	}
}
