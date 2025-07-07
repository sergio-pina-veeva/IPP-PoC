package com.veeva.ipppapp;

import com.veeva.ipppapp.util.IppJobStateParser;
import com.veeva.ipppapp.util.IppJobStatePoller;

import java.io.FileInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class IPPPrintAppToPrintServer {
  public static void print(Properties prop) throws Exception {


    // Read properties
    String ippUrl = prop.getProperty("ippUrl", "http://localhost:9163/printers/VirtualPrinter");
    String filename = prop.getProperty("filename", "test.pdf");
    String fileLocation = prop.getProperty("fileLocation", "./");
    String filepath = fileLocation + filename;

    String jobName = prop.getProperty("jobName", filename);
    String documentFormat = prop.getProperty("documentFormat", "application/pdf");
    int copies = Integer.parseInt(prop.getProperty("copies", "1"));
    String media = prop.getProperty("media");
    int orientationRequested = Integer.parseInt(prop.getProperty("orientationRequested"));
    String sides = prop.getProperty("sidesSp");
    String charset = prop.getProperty("charset", "utf-8");
    String naturalLanguage = prop.getProperty("naturalLanguage", "en");
    byte[] fileBytes= new FileInputStream(filepath).readAllBytes();
    byte[] ippRequest = buildIppPrintJobRequest(
         jobName, fileBytes, ippUrl, documentFormat, copies, media, orientationRequested, sides, charset, naturalLanguage
    );

    System.out.println("Print Attributes:");
    for (String key : prop.stringPropertyNames()) {
      System.out.println(key + " = " + prop.getProperty(key));
    }

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
         .uri(URI.create(ippUrl))
         .header("Content-Type", "application/ipp")
         .POST(HttpRequest.BodyPublishers.ofByteArray(ippRequest))
         .build();

    try {
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      System.out.println("Print job sent to: " + ippUrl);
      System.out.println("Server Response code: " + response.statusCode());
      IppJobStateParser.parseAndPrintJobStatus(response.body().getBytes(StandardCharsets.UTF_8));

      IppJobStatePoller.pollJobStatus(ippUrl, 10, 1);
    } catch (Exception e) {
      System.out.println("Error: ");
           e.printStackTrace();
    }

  }

  private static byte[] buildIppPrintJobRequest(
       String jobName,
       byte[] documentData,
       String ippUrl,
       String documentFormat,
       int copies,
       String media,
       int orientationRequested,
       String sides,
       String charset,
       String naturalLanguage
  ) throws Exception {
    int bufferSize = 1024 + documentData.length;
    ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

    // IPP Header
    buffer.put((byte)0x01); // version major
    buffer.put((byte)0x01); // version minor
    buffer.putShort((short)0x0002); // operation-id Print-Job
    buffer.putInt(1); // request-id

    // Operation Attributes Tag
    buffer.put((byte)0x01);

    putAttr(buffer, (byte)0x47, "attributes-charset", charset);
    putAttr(buffer, (byte)0x48, "attributes-natural-language", naturalLanguage);
    putAttr(buffer, (byte)0x45, "printer-uri", ippUrl); // uri type
    putAttr(buffer, (byte)0x42, "job-name", jobName);
    putAttr(buffer, (byte)0x49, "document-format", documentFormat);
    putAttr(buffer, (byte)0x21, "copies", copies); // integer
    putAttr(buffer, (byte)0x44, "media", media); // keyword
    putAttr(buffer, (byte)0x23, "orientation-requested", orientationRequested); // enum
    putAttr(buffer, (byte)0x44, "sides", sides); // keyword

    // End of attributes tag
    buffer.put((byte)0x03);

    // Document data
    buffer.put(documentData);

    // Return array with exact size
    byte[] result = new byte[buffer.position()];
    buffer.flip();
    buffer.get(result);
    return result;
  }

  // Helper method to put an attribute:
  // tag = value-tag (e.g. 0x47 = charset, 0x42 = nameWithoutLanguage, 0x45 = uri, 0x48 = naturalLanguage, 0x49 = mimeMediaType)
  private static void putAttr(ByteBuffer buffer, byte tag, String name, String value) {
    buffer.put(tag);

    byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
    buffer.putShort((short) nameBytes.length);
    buffer.put(nameBytes);

    byte[] valBytes = value.getBytes(StandardCharsets.UTF_8);
    buffer.putShort((short) valBytes.length);
    buffer.put(valBytes);
  }

  private static void putAttr(ByteBuffer buffer, byte tag, String name, int intValue) {
    buffer.put(tag);
    byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
    buffer.putShort((short) nameBytes.length);
    buffer.put(nameBytes);

    buffer.putShort((short) 4); // int = 4 bytes
    buffer.putInt(intValue);
  }
}