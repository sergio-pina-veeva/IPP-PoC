package com.veeva.ipppapp.util;

import java.nio.charset.StandardCharsets;

public class IppJobStateParser {

  public static void parseAndPrintJobStatus(byte[] ippResponse) {
    String jobStateStr = null;
    int jobState = -1;
    int jobId = -1;
    String jobUri = null;

    for (int i = 0; i < ippResponse.length - 9; i++) {
      // Look for job-id (integer)
      if (matches(ippResponse, i, "job-id")) {
        int valueLenOffset = i + 6;
        int valueLen = ((ippResponse[valueLenOffset] & 0xFF) << 8) | (ippResponse[valueLenOffset + 1] & 0xFF);
        if (valueLen == 4) {
          int valueOffset = valueLenOffset + 2;
          jobId = ((ippResponse[valueOffset] & 0xFF) << 24)
               | ((ippResponse[valueOffset + 1] & 0xFF) << 16)
               | ((ippResponse[valueOffset + 2] & 0xFF) << 8)
               | (ippResponse[valueOffset + 3] & 0xFF);
        }
      }
      // Look for job-uri (uri)
      if (matches(ippResponse, i, "job-uri")) {
        int nameLen = 7;
        int valueLenOffset = i + nameLen;
        int valueLen = ((ippResponse[valueLenOffset] & 0xFF) << 8) | (ippResponse[valueLenOffset + 1] & 0xFF);
        int valueOffset = valueLenOffset + 2;
        jobUri = new String(ippResponse, valueOffset, valueLen, StandardCharsets.UTF_8);
      }
      // Look for job-state (integer)
      if (matches(ippResponse, i, "job-state")) {
        int nameLen = 9;
        int valueLenOffset = i + nameLen;
        int valueLen = ((ippResponse[valueLenOffset] & 0xFF) << 8) | (ippResponse[valueLenOffset + 1] & 0xFF);
        if (valueLen == 4) {
          int valueOffset = valueLenOffset + 2;
          jobState = ((ippResponse[valueOffset] & 0xFF) << 24)
               | ((ippResponse[valueOffset + 1] & 0xFF) << 16)
               | ((ippResponse[valueOffset + 2] & 0xFF) << 8)
               | (ippResponse[valueOffset + 3] & 0xFF);
        }
      }
    }

    // Translate job-state to string
    switch (jobState) {
      case 3: jobStateStr = "pending"; break;
      case 4: jobStateStr = "pending-held"; break;
      case 5: jobStateStr = "processing"; break;
      case 6: jobStateStr = "processing-stopped"; break;
      case 7: jobStateStr = "canceled"; break;
      case 8: jobStateStr = "aborted"; break;
      case 9: jobStateStr = "completed"; break;
      default: jobStateStr = "unknown"; break;
    }

    // Print user-friendly output
    System.out.println("Job ID: " + (jobId != -1 ? jobId : "N/A"));
    System.out.println("Job URI: " + (jobUri != null ? jobUri : "N/A"));
    System.out.println("Job State: " + jobStateStr + (jobState != -1 ? " (" + jobState + ")" : ""));
  }

  private static boolean matches(byte[] data, int offset, String keyword) {
    if (offset + keyword.length() > data.length) return false;
    for (int i = 0; i < keyword.length(); i++) {
      if (data[offset + i] != (byte) keyword.charAt(i)) return false;
    }
    return true;
  }
}

