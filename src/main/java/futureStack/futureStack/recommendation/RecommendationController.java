package futureStack.futureStack.recommendation;

import futureStack.futureStack.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/ai")
public class RecommendationController {

    @Autowired
    private RecommendationService RecommendationService;

    @GetMapping("/daily")
    public ResponseEntity<?> getDaily(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(RecommendationService.generateDailyRecommendation(user));
    }

    @GetMapping("/weekly-summary")
    public ResponseEntity<?> getWeekly(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(RecommendationService.generateWeeklySummary(user));
    }

}
