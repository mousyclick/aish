package mousyclick.aish;

import org.jline.reader.EndOfFileException;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

@Command
public class ChatCommands {

    private final ShellHelper shellHelper;
    private final InputReader inputReader;
    private final ChatClientRegistry registry;

    public ChatCommands(ShellHelper shellHelper,
                        InputReader inputReader,
                        ChatClientRegistry registry) {
        this.shellHelper = shellHelper;
        this.inputReader = inputReader;
        this.registry = registry;
    }

    @Command(command = "provider", description = "Set or get the active AI provider")
    public String provider(
            @Option(required = false, description = "AI provider to use (e.g. openai, ollama)") String name
    ) {
        if (!StringUtils.hasText(name)) {
            return "Active provider is: " + registry.getCurrentProviderName();
        }

        final AiProvider provider;
        try {
            provider = AiProvider.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return String.format(
                    "Unknown provider [%s], valid options are [%s]",
                    name,
                    Arrays.stream(AiProvider.values())
                            .map(Enum::name)
                            .map(String::toLowerCase)
                            .collect(Collectors.joining(", "))
            );
        }

        registry.setActive(provider);
        return "Switched active provider to: " + provider;
    }

    private boolean isExitCommand(String input) {
        return switch (input.trim()) {
            case "exit", "quit", "bye" -> true;
            default -> false;
        };
    }

    private void processChatMessage(String conversationId, String message, MessageWindowChatMemory chatMemory) {
        var userMessage = new UserMessage(message);
        chatMemory.add(conversationId, userMessage);

        var chatClient = registry.getActive();
        var prompt = new Prompt(chatMemory.get(conversationId));
        try {
            if (registry.isStreamingSupported()) {
                var latch = new CountDownLatch(1);
                var buffer = new StringBuilder();
                chatClient.prompt(prompt)
                        .stream()
                        .chatResponse()
                        .subscribe(
                                response -> {
                                    var outputMessage = response.getResult().getOutput().getText();
                                    if (!StringUtils.hasText(outputMessage)) {
                                        return;
                                    }
                                    if (buffer.isEmpty()) {
                                        shellHelper.write(String.format("\uD83E\uDD16 %s: ", response.getMetadata().getModel()), PromptColor.MAGENTA);
                                    }
                                    buffer.append(outputMessage);
                                    shellHelper.write(outputMessage, PromptColor.CYAN);
                                },
                                error -> {
                                    shellHelper.printError("⚠️ Something went wrong: " + error.getMessage());
                                    latch.countDown();
                                },
                                () -> {
                                    shellHelper.newline();
                                    latch.countDown();
                                    chatMemory.add(conversationId, new AssistantMessage(buffer.toString()));
                                }
                        );

                try {
                    latch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    shellHelper.printError("Interrupted while waiting for stream to finish.");
                }

            } else {
                var response = chatClient.prompt(prompt).call().chatResponse();
                if (response != null) {
                    var text = response.getResult().getOutput().getText();
                    shellHelper.write(String.format("\uD83E\uDD16 %s: ", response.getMetadata().getModel()), PromptColor.MAGENTA);
                    shellHelper.printSuccess(text);
                    chatMemory.add(conversationId, response.getResult().getOutput());
                } else {
                    shellHelper.printError("⚠️ Model returned no response.");
                }
            }
        } catch (Exception e) {
            shellHelper.printError("❌ Error: " + e.getMessage());
        }
    }


    @Command(command = "chat", description = "Start interactive chat mode full example")
    public void chat() {
        shellHelper.printInfo("Entering interactive chat mode. Type 'exit' or 'quit' to leave.");

        var chatMemory = MessageWindowChatMemory.builder().build();
        var conversationId = UUID.randomUUID().toString();

//        chatMemory.add(conversationId, new SystemMessage("""
//            Take on the personality of a random celebrity
//        """));

        while (true) {
            String message;
            try {
                message = inputReader.prompt("You");
            } catch (EndOfFileException ex) {
                shellHelper.printInfo("Exiting chat.");
                break;
            }

            if (!StringUtils.hasText(message)) {
                continue;
            }

            if (isExitCommand(message)) {
                shellHelper.printInfo("Exiting chat.");
                break;
            }

            processChatMessage(conversationId, message, chatMemory);
        }
    }
}
