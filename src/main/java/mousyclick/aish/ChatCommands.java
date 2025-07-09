package mousyclick.aish;

import org.jline.reader.EndOfFileException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.shell.command.annotation.Command;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Command
public class ChatCommands {

    private final ShellHelper shellHelper;
    private final InputReader inputReader;
    private final ChatClient chatClient;

    public ChatCommands(ShellHelper shellHelper, InputReader inputReader, ChatClient chatClient) {
        this.shellHelper = shellHelper;
        this.inputReader = inputReader;
        this.chatClient = chatClient;
    }

    @Command(command = "chat", description = "Start interactive chat mode full example")
    public void chat() {
        shellHelper.printInfo("Entering interactive chat mode. Type 'exit' or 'quit' to leave.");

        var chatMemory = MessageWindowChatMemory.builder().build();
        var conversationId = UUID.randomUUID().toString();

        while (true) {
            String message = null;
            try {
                message = inputReader.prompt("You");
            } catch (EndOfFileException ex) {
                shellHelper.printInfo("Exiting chat.");
                break;
            }

            if (!StringUtils.hasText(message)) {
                continue;
            }

            if (message.equalsIgnoreCase("exit") || message.equalsIgnoreCase("quit")) {
                shellHelper.printInfo("Exiting chat.");
                break;
            }

            var userMessage = new UserMessage(message);
            chatMemory.add(conversationId, userMessage);
            try {
                var rsp = chatClient.prompt(new Prompt(chatMemory.get(conversationId))).call().chatResponse();
                if (rsp != null) {
                    shellHelper.write(String.format("\uD83E\uDD16 %s: ", rsp.getMetadata().getModel()), PromptColor.MAGENTA);
                    shellHelper.printSuccess(String.format("%s",rsp.getResult().getOutput().getText()));
                    chatMemory.add(conversationId, rsp.getResult().getOutput());
                } else {
                    shellHelper.printError("TODO: implement failure jaja");
                }
            } catch (Exception e) {
                shellHelper.printError("Error getting response: " + e.getMessage());
            }
        }
    }
}
