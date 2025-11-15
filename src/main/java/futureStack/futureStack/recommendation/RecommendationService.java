package futureStack.futureStack.recommendation;

import futureStack.futureStack.checkIn.CheckInRepository;
import futureStack.futureStack.users.User;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

@Service
public class RecommendationService {

    private final ChatClient chatClient;
    private final CheckInRepository checkInRepository;

    public RecommendationService(ChatClient chatClient, CheckInRepository repo) {
        this.chatClient = chatClient;
        this.checkInRepository = repo;
    }

    public String generateDailyRecommendation(User user) {

        var lastCheckin = checkInRepository
                .findTopByUser_IdOrderByDateDesc(user.getId())
                .orElse(null);

        if (lastCheckin == null) {
            return "Ainda não encontrei check-ins seus. Faça seu primeiro check-in para gerar uma recomendação personalizada.";
        }

        String prompt = """
Você é um assistente especializado em bem-estar corporativo. Gere uma recomendação curta, clara e motivadora baseada nos dados:

Humor: %d
Energia: %d
Sono: %d
Foco: %d
Carga de Trabalho: %d
Score: %d

INSTRUÇÕES OBRIGATÓRIAS:
- Responda em texto corrido, com 3 a 5 frases.
- Não use tabelas, listas, tópicos, negrito, títulos ou markdown.
- Não repita os valores numéricos informados (como 7/10 ou 8/10).
- Mantenha tom profissional, simples e acolhedor.
- Dê apenas UMA recomendação prática no final.
- Retorne somente o texto final, nada além disso.

Agora gere a resposta.
""".formatted(
                lastCheckin.getMood(),
                lastCheckin.getEnergy(),
                lastCheckin.getSleep(),
                lastCheckin.getFocus(),
                lastCheckin.getHoursWorked(),
                lastCheckin.getScore()
        );



        return chatClient
                .prompt()
                .user(prompt)
                .options(OpenAiChatOptions.builder()
                        .model("openai/gpt-oss-20b")
                        .build())
                .call()
                .content();

    }
}
