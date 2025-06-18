package com.veeva.ipppapp;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class IPPPJobStatusChecker {

  private static final int MAX_ATTEMPTS = 30;
  private static final int POLL_INTERVAL_MS = 1000;
  private static final String DEFAULT_PRINTER_URI = "http://localhost:9163/printers/VirtualPrinter";

  public static void main(String[] args) {
    if (args.length == 0) {
      // No job URI provided: list all jobs
      listAllJobs(DEFAULT_PRINTER_URI);
    } else {
      // Job URI provided: poll that job
      pollJobRecursive(args[0], 0);
    }
  }

  private static void listAllJobs(String printerUri) {
    try {
      byte[] ippRequest = buildGetJobsRequest(printerUri);
      byte[] ippResponse = sendIppRequest(printerUri, ippRequest);

      // Parse and print all job-ids, job-uris, and job-states
      int offset = 0;
      while (offset < ippResponse.length) {
        // Look for job-id
        if (matches(ippResponse, offset, "job-id")) {
          int jobId = extractIntValue(ippResponse, offset + 6);
          String jobUri = findStringAttr(ippResponse, offset, "job-uri");
          int jobState = findIntAttr(ippResponse, offset, "job-state");
          String jobStateStr = jobStateToString(jobState);

          System.out.printf("Job ID: %d, URI: %s, State: %s (%d)%n",
               jobId,
               jobUri != null ? jobUri : "N/A",
               jobStateStr,
               jobState
          );
        }
        offset++;
      }
    } catch (Exception e) {
      System.out.println("Error listing jobs: " + e.getMessage());
    }
  }

  private static void pollJobRecursive(String jobUri, int attempt) {
    if (attempt >= MAX_ATTEMPTS) {
      System.out.println("Max polling attempts reached");
      return;
    }
    try {
      int jobState = getJobState(jobUri);
      String stateDescription = jobStateToString(jobState);

      System.out.printf("Attempt %d/%d: %s%n",
           attempt + 1, MAX_ATTEMPTS, stateDescription);

      if (isTerminalState(jobState)) {
        handleCompletion(jobState);
        return;
      }
      Thread.sleep(POLL_INTERVAL_MS);
      pollJobRecursive(jobUri, attempt + 1);
    } catch (Exception e) {
      System.out.println("Error checking job: " + e.getMessage());
    }
  }

  private static int getJobState(String jobUri) throws Exception {
    String printerUri = jobUri.split("/jobs/")[0];
    byte[] ippRequest = buildGetJobAttributesRequest(jobUri);
    byte[] ippResponse = sendIppRequest(printerUri, ippRequest);
    return extractJobStateFromIppResponse(ippResponse);
  }

  // --- IPP Request builders and helpers ---

  private static byte[] buildGetJobsRequest(String printerUri) {
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    buffer.put((byte) 0x01); // version major
    buffer.put((byte) 0x01); // version minor
    buffer.putShort((short) 0x000A); // operation-id: Get-Jobs
    buffer.putInt(3); // request-id

    buffer.put((byte) 0x01); // operation attributes tag
    putAttr(buffer, (byte) 0x47, "attributes-charset", "utf-8");
    putAttr(buffer, (byte) 0x48, "attributes-natural-language", "en");
    putAttr(buffer, (byte) 0x45, "printer-uri", printerUri);

    buffer.put((byte) 0x03); // end of attributes

    byte[] result = new byte[buffer.position()];
    buffer.flip();
    buffer.get(result);
    return result;
  }

  private static byte[] buildGetJobAttributesRequest(String jobUri) {
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    buffer.put((byte) 0x01); // version major
    buffer.put((byte) 0x01); // version minor
    buffer.putShort((short) 0x0009); // operation-id: Get-Job-Attributes
    buffer.putInt(2); // request-id

    buffer.put((byte) 0x01); // operation attributes tag
    putAttr(buffer, (byte) 0x47, "attributes-charset", "utf-8");
    putAttr(buffer, (byte) 0x48, "attributes-natural-language", "en");
    putAttr(buffer, (byte) 0x45, "job-uri", jobUri);

    buffer.put((byte) 0x03); // end of attributes

    byte[] result = new byte[buffer.position()];
    buffer.flip();
    buffer.get(result);
    return result;
  }

  private static void putAttr(ByteBuffer buffer, byte tag, String name, String value) {
    buffer.put(tag);
    byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
    buffer.putShort((short) nameBytes.length);
    buffer.put(nameBytes);
    byte[] valBytes = value.getBytes(StandardCharsets.UTF_8);
    buffer.putShort((short) valBytes.length);
    buffer.put(valBytes);
  }

  private static byte[] sendIppRequest(String printerUri, byte[] ippRequest) throws Exception {
    URL url = new URL(printerUri);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "application/ipp");
    try (OutputStream out = conn.getOutputStream()) {
      out.write(ippRequest);
    }
    try (InputStream in = conn.getInputStream()) {
      return in.readAllBytes();
    }
  }

  // --- IPP response parsing helpers ---

  private static boolean matches(byte[] data, int offset, String keyword) {
    if (offset + keyword.length() > data.length) return false;
    for (int i = 0; i < keyword.length(); i++) {
      if (data[offset + i] != (byte) keyword.charAt(i)) return false;
    }
    return true;
  }

  private static int extractIntValue(byte[] data, int offset) {
    int valueLen = ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
    if (valueLen == 4) {
      int valueOffset = offset + 2;
      return ((data[valueOffset] & 0xFF) << 24)
           | ((data[valueOffset + 1] & 0xFF) << 16)
           | ((data[valueOffset + 2] & 0xFF) << 8)
           | (data[valueOffset + 3] & 0xFF);
    }
    return -1;
  }

  private static String findStringAttr(byte[] data, int start, String attrName) {
    for (int i = start; i < data.length - attrName.length(); i++) {
      if (matches(data, i, attrName)) {
        int nameLen = attrName.length();
        int valueLenOffset = i + nameLen;
        int valueLen = ((data[valueLenOffset] & 0xFF) << 8) | (data[valueLenOffset + 1] & 0xFF);
        int valueOffset = valueLenOffset + 2;
        return new String(data, valueOffset, valueLen, StandardCharsets.UTF_8);
      }
    }
    return null;
  }

  private static int findIntAttr(byte[] data, int start, String attrName) {
    for (int i = start; i < data.length - attrName.length(); i++) {
      if (matches(data, i, attrName)) {
        int nameLen = attrName.length();
        int valueLenOffset = i + nameLen;
        int valueLen = ((data[valueLenOffset] & 0xFF) << 8) | (data[valueLenOffset + 1] & 0xFF);
        if (valueLen == 4) {
          int valueOffset = valueLenOffset + 2;
          return ((data[valueOffset] & 0xFF) << 24)
               | ((data[valueOffset + 1] & 0xFF) << 16)
               | ((data[valueOffset + 2] & 0xFF) << 8)
               | (data[valueOffset + 3] & 0xFF);
        }
      }
    }
    return -1;
  }

  private static int extractJobStateFromIppResponse(byte[] ippResponse) {
    return findIntAttr(ippResponse, 0, "job-state");
  }

  private static String jobStateToString(int state) {
    switch (state) {
      case 3: return "pending";
      case 4: return "pending-held";
      case 5: return "processing";
      case 6: return "processing-stopped";
      case 7: return "canceled";
      case 8: return "aborted";
      case 9: return "completed";
      default: return "unknown";
    }
  }

  private static boolean isTerminalState(int state) {
    return state == 7 || state == 8 || state == 9;
  }

  private static void handleCompletion(int state) {
    switch (state) {
      case 9:
        System.out.println("Print job completed successfully.");
        break;
      case 7:
        System.out.println("Print job was canceled.");
        break;
      case 8:
        System.out.println("Print job was aborted.");
        break;
      default:
        System.out.println("Print job ended with state: " + jobStateToString(state));
    }
  }
}
