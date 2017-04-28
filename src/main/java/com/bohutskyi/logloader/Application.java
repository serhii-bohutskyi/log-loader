package com.bohutskyi.logloader;

import com.bohutskyi.logloader.ui.LoaderForm;
import com.jcraft.jsch.JSch;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author Serhii Bohutskyi
 */
@EnableAsync
@SpringBootApplication
@EnableAutoConfiguration
public class Application {

    @Bean
    public LoaderForm loaderForm() {
        return new LoaderForm();
    }

    @Bean
    public JSch jsch() {
        return new JSch();
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class).headless(false).run(args);
    }
}
