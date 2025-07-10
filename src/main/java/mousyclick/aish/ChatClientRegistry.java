package mousyclick.aish;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ChatClientRegistry {

    private final Map<AiProvider, ChatClient> chatClients;
    private volatile ChatClient activeChatClient;
    private volatile AiProvider activeProvider;

    public ChatClientRegistry(
            ChatClient openAiChatClient,
            ChatClient ollamaChatClient,
            @Value("${spring.ai.provider}") AiProvider defaultProvider
    ) {
        this.chatClients = Map.of(
                AiProvider.OPENAI, openAiChatClient,
                AiProvider.OLLAMA, ollamaChatClient
        );
        this.activeProvider = defaultProvider;
        this.activeChatClient = chatClients.getOrDefault(defaultProvider, openAiChatClient);
    }

    public ChatClient getActive() {
        return activeChatClient;
    }

    public void setActive(AiProvider provider) {
        var selected = chatClients.get(provider);
        if (selected == null) {
            throw new IllegalArgumentException("Unknown provider: " + provider);
        }
        this.activeProvider = provider;
        this.activeChatClient = selected;
    }

    public String getCurrentProviderName() {
        return activeProvider.name().toLowerCase();
    }

    public boolean isStreamingSupported() {
        return true;
    }
}
