package mousyclick.aish;

import org.springframework.shell.command.annotation.Command;

@Command
public class TestCommands {

    @Command(command = "ping", description = "Ping / pong")
    public String ping() {
        return "pong";
    }

    @Command(command = "whatsurname", description = "What is your name?")
    public String whatsurname(String name) {
        return "Hello, " + name;
    }
}
