package net.ecommerce.springboot.service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

@Service
public class QrPngService {

	private static final int SIZE_PX = 280;

	public String encodeQrAsPngBase64(String text) {
		if (text == null || text.isBlank()) {
			throw new IllegalArgumentException("Texte QR vide.");
		}
		try {
			QRCodeWriter writer = new QRCodeWriter();
			BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, SIZE_PX, SIZE_PX);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
			return Base64.getEncoder().encodeToString(baos.toByteArray());
		} catch (Exception e) {
			throw new IllegalStateException("Génération du QR impossible.", e);
		}
	}
}
