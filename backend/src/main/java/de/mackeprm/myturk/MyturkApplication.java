package de.mackeprm.myturk;

import de.mackeprm.myturk.model.Experiment;
import de.mackeprm.myturk.model.ExperimentRepository;
import de.mackeprm.myturk.mturk.Endpoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class MyturkApplication {

    @Value("${de.mackeprm.myturk.devSetup}")
    public boolean devSetup = false;

    public static void main(String[] args) {
        SpringApplication.run(MyturkApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/v1/**")
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .allowedOrigins("http://localhost:9011");
            }
        };
    }

    //TODO devsetup
    /*
    	production: [
	{
		title: "WebConf Study 1: Recoin Original with Onboarding",
		description: "This is the description of the experiment",
		available: "2 / 10",
		pending: "3 / 10",
		waitingForApproval: "5 / 10",
		completed: "0 / 10",
		id: generate('1234567890ABCDEFGHIJKLMNOPKR', 25),

		hits: [
		{
			id: generate('1234567890ABCDEFGHIJKLMNOPKR', 25),
			available: "2 / 10",
			pending: "3 / 10",
			waitingForApproval: "5 / 10",
			completed: "0 / 10",
		},
		{
			id: generate('1234567890ABCDEFGHIJKLMNOPKR', 25),
			available: "2 / 10",
			pending: "3 / 10",
			waitingForApproval: "5 / 10",
			completed: "0 / 10",
		}]
	},
    private double reward;
        //TODO levels of trust here
    //TODO set this explicitly?
    private boolean includeDefaultRequirements;

    //Entrypoint
    @Column(length = 5_000)
    private String entrypoint;

    //Endpoint
    //TODO can i make this endpoint independent?
    @Enumerated(EnumType.STRING)
    private Endpoint endpoint;
     */
    @Bean
    public CommandLineRunner devSetup(ExperimentRepository experimentRepository) {
        if (devSetup) {
            return (args) -> {
                if (experimentRepository.count() == 0) {
                    experimentRepository.save(new Experiment(
                            "idea-similarities-25-pairs",
                            "Rate the similarity between 25 idea texts",
                            "Read short ideas about technologies, and then rate the similarity between them.",
                            Arrays.asList("Data Annotation", "Rating", "Ideas", "Research", "Creativity"),
                            Duration.ofDays(3),
                            Duration.ofMinutes(180),
                            2,
                            2.0d,
                            null,
                            false,
                            "https://i2m-research.imp.fu-berlin.de/#/intro?ratingProjectId=random-idea-pairs-25",
                            Endpoint.sandbox
                    ));
                }
            };
        } else {
            return null;
        }
    }
}
