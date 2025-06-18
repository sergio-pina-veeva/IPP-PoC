package com.veeva.ipppapp;

import com.veeva.ipppapp.util.JnaPrintJobInfo;
import com.veeva.ipppapp.util.JnaPrintJobManager;

import java.util.List;
import java.util.Properties;

public class JnaJobChecker {
  public static void checkJobs(Properties prop) {
    JnaPrintJobManager manager = new JnaPrintJobManager();

    try {
      // Get jobs from local printer
//      List<JnaPrintJobInfo> localJobs = manager.getPrintJobs("VirtualPrinter", null);
//      System.out.println("Printer jobs:");
//      localJobs.forEach(System.out::println);

      // Alternative using WinspoolUtil
      List<JnaPrintJobInfo> utilJobs = manager.getPrintJobsUsingUtil("VirtualPrinter");
      System.out.println("Jobs using Winspool utility method:");
      utilJobs.forEach(System.out::println);

    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
    }
  }
}

