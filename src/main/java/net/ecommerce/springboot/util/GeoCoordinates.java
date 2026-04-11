package net.ecommerce.springboot.util;

/**
 * Validation des coordonnées WGS84 pour profils et livraison.
 */
public final class GeoCoordinates {

	private GeoCoordinates() {
	}

	public static void requireLatLng(Double latitude, Double longitude, String messageIfMissing) {
		if (latitude == null || longitude == null) {
			throw new IllegalStateException(messageIfMissing);
		}
		assertValidRange(latitude, longitude);
	}

	public static void assertValidRange(double latitude, double longitude) {
		if (latitude < -90.0 || latitude > 90.0 || longitude < -180.0 || longitude > 180.0) {
			throw new IllegalArgumentException(
					"Coordonnées GPS invalides : latitude entre −90 et 90, longitude entre −180 et 180.");
		}
	}
}
