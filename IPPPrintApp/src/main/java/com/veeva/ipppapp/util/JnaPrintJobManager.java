package com.veeva.ipppapp.util;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLEByReference;
import com.sun.jna.platform.win32.Winspool;
import com.sun.jna.platform.win32.WinspoolUtil;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.Memory;
import java.util.ArrayList;
import java.util.List;

public class JnaPrintJobManager {

  /**
   * Retrieves all print jobs for a specific printer using JNA
   * @param printerName The name of the printer
   * @param hostName The hostname (null for local machine)
   * @return List of PrintJobInfo objects containing job details
   */
  public List<JnaPrintJobInfo> getPrintJobs(String printerName, String hostName) {
    List<JnaPrintJobInfo> printJobs = new ArrayList<>();
    HANDLEByReference printerHandle = new HANDLEByReference();

    try {
      // Open printer handle
      String fullPrinterName = hostName != null ? "\\\\" + hostName + "\\" + printerName : printerName;
      boolean success = Winspool.INSTANCE.OpenPrinter(fullPrinterName, printerHandle, null);

      if (!success) {
        int errorCode = Kernel32.INSTANCE.GetLastError();
        throw new RuntimeException("Failed to open printer: " + printerName + " (Error: " + errorCode + ")");
      }

      // Get job information using EnumJobs
      IntByReference bytesNeeded = new IntByReference();
      IntByReference jobsReturned = new IntByReference();

      // First call to get required buffer size
      Winspool.INSTANCE.EnumJobs(
           printerHandle.getValue(),  // hPrinter
           0,                        // FirstJob
           999,                      // NoJobs (max jobs to enumerate)
           1,                        // Level (JOB_INFO_1)
           null,                     // pJob
           0,                        // cbBuf
           bytesNeeded,              // pcbNeeded
           jobsReturned              // pcReturned
      );

      if (bytesNeeded.getValue() > 0) {
        // Allocate memory for job information
        Memory jobBuffer = new Memory(bytesNeeded.getValue());

        // Second call to get actual job data
        success = Winspool.INSTANCE.EnumJobs(
             printerHandle.getValue(),
             0,
             999,
             1,
             jobBuffer,
             (int) jobBuffer.size(),
             bytesNeeded,
             jobsReturned
        );

        if (success && jobsReturned.getValue() > 0) {
          // Parse JOB_INFO_1 structures
          printJobs = parseJobInfo1Array(jobBuffer, jobsReturned.getValue());
        }
      }

    } catch (Exception e) {
      throw new RuntimeException("Error retrieving print jobs: " + e.getMessage(), e);
    } finally {
      // Always close the printer handle
      if (printerHandle.getValue() != null) {
        Winspool.INSTANCE.ClosePrinter(printerHandle.getValue());
      }
    }

    return printJobs;
  }

  /**
   * Alternative implementation using WinspoolUtil helper class
   */
  public List<JnaPrintJobInfo> getPrintJobsUsingUtil(String printerName) {
    List<JnaPrintJobInfo> printJobs = new ArrayList<>();
    HANDLEByReference printerHandle = new HANDLEByReference();

    try {
      boolean success = Winspool.INSTANCE.OpenPrinter(printerName, printerHandle, null);
      if (!success) {
        throw new RuntimeException("Failed to open printer: " + printerName);
      }

      // Use the utility method from JNA
      Winspool.JOB_INFO_1[] jobInfoArray = WinspoolUtil.getJobInfo1(printerHandle);

      for (Winspool.JOB_INFO_1 jobInfo : jobInfoArray) {
        JnaPrintJobInfo printJob = new JnaPrintJobInfo();
        printJob.setJobId(jobInfo.JobId);
        printJob.setPrinterName(jobInfo.pPrinterName);
        printJob.setMachineName(jobInfo.pMachineName);
        printJob.setUserName(jobInfo.pUserName);
        printJob.setDocumentName(jobInfo.pDocument);
        printJob.setDataType(jobInfo.pDatatype);
        printJob.setStatus(jobInfo.pStatus);
        printJob.setStatusCode(jobInfo.Status);
        printJob.setPriority(jobInfo.Priority);
        printJob.setPosition(jobInfo.Position);
        printJob.setTotalPages(jobInfo.TotalPages);
        printJob.setPagesPrinted(jobInfo.PagesPrinted);
        printJob.setSubmitted(jobInfo.Submitted);

        printJobs.add(printJob);
      }

    } finally {
      if (printerHandle.getValue() != null) {
        Winspool.INSTANCE.ClosePrinter(printerHandle.getValue());
      }
    }

    return printJobs;
  }

  /**
   * Parses JOB_INFO_1 structures from memory buffer
   */
  private List<JnaPrintJobInfo> parseJobInfo1Array(Memory buffer, int jobCount) {
    List<JnaPrintJobInfo> jobs = new ArrayList<>();
    int structSize = Native.getNativeSize(Winspool.JOB_INFO_1.class);

    for (int i = 0; i < jobCount; i++) {
      Pointer p = buffer.share((long) i * structSize);
      Winspool.JOB_INFO_1 jobInfo = Structure.newInstance(Winspool.JOB_INFO_1.class,
           p);
      jobInfo.read();

      JnaPrintJobInfo printJob = new JnaPrintJobInfo();
      printJob.setJobId(jobInfo.JobId);
      printJob.setPrinterName(jobInfo.pPrinterName);
      printJob.setMachineName(jobInfo.pMachineName);
      printJob.setUserName(jobInfo.pUserName);
      printJob.setDocumentName(jobInfo.pDocument);
      printJob.setDataType(jobInfo.pDatatype);
      printJob.setStatus(jobInfo.pStatus);
      printJob.setStatusCode(jobInfo.Status);
      printJob.setPriority(jobInfo.Priority);
      printJob.setPosition(jobInfo.Position);
      printJob.setTotalPages(jobInfo.TotalPages);
      printJob.setPagesPrinted(jobInfo.PagesPrinted);
      printJob.setSubmitted(jobInfo.Submitted);

      jobs.add(printJob);
    }

    return jobs;
  }
}

