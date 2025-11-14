package futureStack.futureStack.recommendation;

public record RecommendationResponseDTO(
        String message,
        int score,
        String date
) {}