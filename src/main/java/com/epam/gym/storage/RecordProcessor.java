package com.epam.gym.storage;

/**
 * Interface for processing different types of CSV records
 */
public interface RecordProcessor {
    /**
     * The CSV record type this processor handles, e.g. "Trainee", "Trainer"...
     */
    String getRecordType();

    /**
     * Process the parsed CSV columns (p[]).
     * @param columns The CSV columns
     */
    void process(String[] columns);
}