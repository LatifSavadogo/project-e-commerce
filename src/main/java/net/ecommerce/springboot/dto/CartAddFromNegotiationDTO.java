package net.ecommerce.springboot.dto;

import jakarta.validation.constraints.NotNull;

public class CartAddFromNegotiationDTO {

	@NotNull
	private Integer conversationId;

	@NotNull
	private Integer messageId;

	public Integer getConversationId() {
		return conversationId;
	}

	public void setConversationId(Integer conversationId) {
		this.conversationId = conversationId;
	}

	public Integer getMessageId() {
		return messageId;
	}

	public void setMessageId(Integer messageId) {
		this.messageId = messageId;
	}
}
