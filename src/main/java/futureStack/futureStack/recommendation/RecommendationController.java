package futureStack.futureStack.recommendation;

import futureStack.futureStack.checkIn.CheckInService;
import futureStack.futureStack.users.User;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/ai")
public class RecommendationController {

    @Autowired
    private RecommendationService aiRecommendationService;

    @Autowired
    private WeeklySummaryService weeklySummaryService;

    @GetMapping("/daily")
    public ResponseEntity<?> getDaily(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(aiRecommendationService.generateDailyRecommendation(user));
    }

    @GetMapping("/weekly-summary")
    public ResponseEntity<?> getWeekly(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(weeklySummaryService.generateWeeklySummary(user));
    }

    @GetMapping("/test")
    public String test(ChatClient.Builder builder) {
        return builder.build()
                .prompt("Diga apenas OK")
                .call()
                .content();
    }
}
