package com.epam.gym.storage;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@Component
public class CsvDataLoader implements InitializingBean {
    private final Environment env;
    private final List<RecordProcessor> processors;

    public CsvDataLoader(Environment env, List<RecordProcessor> processors) {
        this.env = env;
        this.processors = processors;
    }

    private static final int RECORD_TYPE_COLUMN = 0;

    private void processRecordType(List<String> lines, String targetType) {
        lines.stream()
            .map(line -> line.split(","))
            .filter(columns -> columns[RECORD_TYPE_COLUMN].equals(targetType))
            .forEach(columns ->
                processors.stream()
                    .filter(p -> p.getRecordType().equals(targetType))
                    .findFirst()
                    .ifPresent(processor -> processor.process(columns))
            );
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String location = env.getProperty("seed.data.csv", "classpath:data.csv");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getClassLoader()
                        .getResourceAsStream(location.replace("classpath:", ""))),
                StandardCharsets.UTF_8))) {

            // Read all lines
            List<String> lines = br.lines().skip(1).toList(); // Skip header

            // Process TrainingType records first
            processRecordType(lines, "TrainingType");

            // Then process User records (Trainee and Trainer)
            processRecordType(lines, "Trainee");
            processRecordType(lines, "Trainer");

            // Finally process Training records that depend on the others
            processRecordType(lines, "Training");
        }
    }
}