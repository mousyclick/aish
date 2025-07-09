package mousyclick.aish;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.shell.command.annotation.CommandScan;

@CommandScan
@SpringBootApplication
public class AishApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(AishApplication.class)
				.web(WebApplicationType.NONE)
				.run(args);
	}

}
