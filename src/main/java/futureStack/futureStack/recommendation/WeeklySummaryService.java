package futureStack.futureStack.recommendation;

import futureStack.futureStack.checkIn.CheckInModel;
import futureStack.futureStack.checkIn.CheckInRepository;
import futureStack.futureStack.users.User;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class WeeklySummaryService {

    private final ChatClient chatClient;
    private final CheckInRepository checkInRepository;

    public WeeklySummaryService(ChatClient chatClient, CheckInRepository repo) {
        this.chatClient = chatClient;
        this.checkInRepository = repo;
    }

    public String generateWeeklySummary(User user) {

        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);

        List<CheckInModel> list =
                checkInRepository.findByUser_IdAndDateAfter(user.getId(), sevenDaysAgo);

        if (list.isEmpty()) {
            return "Não encontrei check-ins suficientes para gerar um resumo semanal.";
        }

        StringBuilder data = new StringBuilder();
        for (CheckInModel c : list) {
            data.append("""
                    Dia %s:
                    humor=%d, energia=%d, sono=%d, foco=%d, carga=%d, score=%d

                    """.formatted(
                    c.getDate(),
                    c.getMood(),
                    c.getEnergy(),
                    c.getSleep(),
                    c.getFocus(),
                    c.getHoursWorked(),
                    c.getScore()
            ));
        }

        String prompt = """
Gere um resumo semanal de bem-estar analisando os dados abaixo:

%s

INSTRUÇÕES OBRIGATÓRIAS:
- Entregue um texto direto, com 4 a 6 frases.
- Sem markdown, sem títulos e sem listas.
- Não repita números detalhadamente.
- Destaque tendências gerais (melhora, queda, estabilidade).
- Inclua apenas UMA recomendação prática no final.
- Tom profissional, acolhedor e simples.

Retorne só o texto final.
""".formatted(data);


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
