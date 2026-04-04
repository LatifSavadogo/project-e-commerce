package net.ecommerce.springboot.dto;

import java.util.ArrayList;
import java.util.List;

public class ChatbotReplyDTO {

	private String reply;
	private List<String> intentsDetected = new ArrayList<>();
	private List<String> suggestions = new ArrayList<>();
	private boolean transferSuggested;

	public String getReply() {
		return reply;
	}

	public void setReply(String reply) {
		this.reply = reply;
	}

	public List<String> getIntentsDetected() {
		return intentsDetected;
	}

	public void setIntentsDetected(List<String> intentsDetected) {
		this.intentsDetected = intentsDetected;
	}

	public List<String> getSuggestions() {
		return suggestions;
	}

	public void setSuggestions(List<String> suggestions) {
		this.suggestions = suggestions;
	}

	public boolean isTransferSuggested() {
		return transferSuggested;
	}

	public void setTransferSuggested(boolean transferSuggested) {
		this.transferSuggested = transferSuggested;
	}
}
