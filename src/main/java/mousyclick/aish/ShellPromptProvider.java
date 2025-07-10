package mousyclick.aish;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

@Component
public class ShellPromptProvider implements PromptProvider {

    private final ChatClientRegistry registry;

    public ShellPromptProvider(ChatClientRegistry registry) {
        this.registry = registry;
    }

    @Override
    public AttributedString getPrompt() {
        var activeProvider = registry.getCurrentProviderName();
        return new AttributedString(
                String.format("%s $ ", activeProvider),
                AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW)
        );
    }
}
