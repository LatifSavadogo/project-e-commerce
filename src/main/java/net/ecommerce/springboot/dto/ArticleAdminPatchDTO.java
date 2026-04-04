package net.ecommerce.springboot.dto;

public class ArticleAdminPatchDTO {

	private Boolean blocked;
	private String warningMessage;
	/** Si true : supprime tout message d'avertissement (prioritaire sur {@link #warningMessage}). */
	private Boolean clearWarning;

	public Boolean getBlocked() {
		return blocked;
	}

	public void setBlocked(Boolean blocked) {
		this.blocked = blocked;
	}

	public String getWarningMessage() {
		return warningMessage;
	}

	public void setWarningMessage(String warningMessage) {
		this.warningMessage = warningMessage;
	}

	public Boolean getClearWarning() {
		return clearWarning;
	}

	public void setClearWarning(Boolean clearWarning) {
		this.clearWarning = clearWarning;
	}
}
