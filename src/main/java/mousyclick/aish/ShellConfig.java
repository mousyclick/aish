package mousyclick.aish;

import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class ShellConfig {

    @Bean
    public InputReader inputReader(@Lazy LineReader lineReader) {
        return new InputReader(lineReader);
    }

    @Bean
    public ShellHelper shellHelper(@Lazy Terminal terminal) {
        return new ShellHelper(terminal);
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
