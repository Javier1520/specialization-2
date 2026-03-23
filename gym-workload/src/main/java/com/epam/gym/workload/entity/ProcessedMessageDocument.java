package com.epam.gym.workload.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "processed_messages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedMessageDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String messageKey;

    @Builder.Default
    private LocalDateTime processedAt = LocalDateTime.now();
}