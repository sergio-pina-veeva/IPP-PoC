package com.veeva.ipppapp.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class IppJobStatePoller {

  public static void pollJobStatus(String jobUri, int maxTries, int milisecondsSleep) throws Exception {
    URI uri = URI.create(jobUri);
    String printerUri = jobUri.split("/jobs/")[0]; // assumes /jobs/{id}
    int tries = 0;
    while (tries < maxTries) {
      byte[] ippRequest = buildGetJobAttributesRequest(jobUri);
      byte[] ippResponse = sendIppRequest(printerUri, ippRequest);

      int jobState = extractJobStateFromIppResponse(ippResponse);
      String jobStateStr = jobStateToString(jobState);

      System.out.println("Job state: " + jobStateStr + (jobState != -1 ? " (" + jobState + ")" : ""));

      if (jobState == 9) { // completed
        System.out.println("Print job completed successfully.");
        break;
      } else if (jobState == 7) { // canceled
        System.out.println("Print job was canceled.");
        break;
      } else if (jobState == 8) { // aborted
        System.out.println("Print job was aborted.");
        break;
      }
      Thread.sleep(milisecondsSleep); // wait 1 second before next poll
      tries++;
    }
    if (tries == maxTries) {
      System.out.println("Timed out waiting for job to complete.");
    }
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

  public static byte[] sendIppRequest(String printerUri, byte[] ippRequest) throws Exception {
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

  private static int extractJobStateFromIppResponse(byte[] ippResponse) {
    String needle = "job-state";
    for (int i = 0; i < ippResponse.length - needle.length(); i++) {
      boolean found = true;
      for (int j = 0; j < needle.length(); j++) {
        if (ippResponse[i + j] != (byte) needle.charAt(j)) {
          found = false;
          break;
        }
      }
      if (found) {
        int nameLen = 9;
        int valueLenOffset = i + nameLen;
        int valueLen = ((ippResponse[valueLenOffset] & 0xFF) << 8) | (ippResponse[valueLenOffset + 1] & 0xFF);
        if (valueLen == 4) {
          int valueOffset = valueLenOffset + 2;
          return ((ippResponse[valueOffset] & 0xFF) << 24)
               | ((ippResponse[valueOffset + 1] & 0xFF) << 16)
               | ((ippResponse[valueOffset + 2] & 0xFF) << 8)
               | (ippResponse[valueOffset + 3] & 0xFF);
        }
      }
    }
    return -1;
  }

  public static String jobStateToString(int state) {
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
}
