package com.epam.gym.util;

import java.util.UUID;

public class TransactionIdGenerator {
  public static String generate() {
    return UUID.randomUUID().toString();
  }
}
