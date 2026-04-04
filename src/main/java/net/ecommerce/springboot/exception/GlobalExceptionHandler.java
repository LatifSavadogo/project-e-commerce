package net.ecommerce.springboot.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
		Map<String, String> fields = ex.getBindingResult().getFieldErrors().stream()
				.collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a + "; " + b));
		Map<String, Object> body = new HashMap<>();
		body.put("error", "Données invalides");
		body.put("code", "VALIDATION_ERROR");
		body.put("fields", fields);
		return ResponseEntity.badRequest().body(body);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, String>> handleUnreadable(HttpMessageNotReadableException ex) {
		return ResponseEntity.badRequest()
				.body(Map.of("error", "Corps JSON invalide ou incomplet", "code", "BAD_REQUEST"));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		return ResponseEntity.badRequest().body(Map.of("error",
				"Paramètre invalide : " + ex.getName(), "code", "TYPE_MISMATCH"));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
		String msg = ex.getMessage() != null ? ex.getMessage() : "Requête invalide";
		return ResponseEntity.badRequest().body(Map.of("error", msg, "code", "BAD_REQUEST"));
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
		String msg = ex.getMessage() != null ? ex.getMessage() : "Règle métier non respectée";
		if (msg.contains("déjà utilisée") || msg.contains("Conflit")) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", msg, "code", "CONFLICT"));
		}
		if (msg.contains("non autorisé") || msg.contains("Accès non autorisé")) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", msg, "code", "FORBIDDEN"));
		}
		if (msg.contains("Non authentifié")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", msg, "code", "UNAUTHORIZED"));
		}
		return ResponseEntity.badRequest().body(Map.of("error", msg, "code", "BUSINESS_RULE"));
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
		String msg = ex.getMessage() != null ? ex.getMessage() : "Ressource introuvable";
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", msg, "code", "NOT_FOUND"));
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<Map<String, String>> handleMissingParam(MissingServletRequestParameterException ex) {
		return ResponseEntity.badRequest()
				.body(Map.of("error", "Paramètre requis manquant : " + ex.getParameterName(), "code", "BAD_REQUEST"));
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<Map<String, String>> handleMaxUpload(MaxUploadSizeExceededException ex) {
		return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
				.body(Map.of("error",
						"Fichier ou formulaire trop volumineux (max 10 Mo par fichier, 12 Mo pour la requête).",
						"code", "PAYLOAD_TOO_LARGE"));
	}

	@ExceptionHandler(MultipartException.class)
	public ResponseEntity<Map<String, String>> handleMultipart(MultipartException ex) {
		Throwable c = ex.getCause();
		String detail = c != null && c.getMessage() != null ? c.getMessage() : ex.getMessage();
		return ResponseEntity.badRequest().body(Map.of(
				"error",
				"Envoi de fichier invalide ou incomplet." + (detail != null ? " " + detail : ""),
				"code", "MULTIPART_ERROR"));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<Map<String, String>> handleDataIntegrity(DataIntegrityViolationException ex) {
		String root = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : "";
		String lower = root.toLowerCase();
		if (lower.contains("duplicate") || lower.contains("unique")) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("error", "Donnée en doublon (contrainte unique).", "code", "DUPLICATE"));
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(Map.of("error", "Contrainte base de données non respectée.", "code", "DATA_INTEGRITY"));
	}
}
