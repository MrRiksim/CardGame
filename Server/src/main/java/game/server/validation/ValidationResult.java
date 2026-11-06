package game.server.validation;

/**
 * What a validation chain decided. reason is null for an accepted play.
 */
public record ValidationResult(boolean accepted, String reason) {

    public static ValidationResult ok() {
        return new ValidationResult(true, null);
    }

    public static ValidationResult rejected(String reason) {
        return new ValidationResult(false, reason);
    }
}
