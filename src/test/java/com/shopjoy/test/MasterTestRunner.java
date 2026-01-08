 package com.shopjoy.test;


 public class MasterTestRunner {
     public static void main(String[] args) {
         System.out.println("============================================");
         System.out.println("SHOPJOY - COMPLETE DAO TEST SUITE");
         System.out.println("============================================");
         System.out.println("Starting comprehensive database tests...");

         runTest("UserDAOTest", () -> UserDAOTest.main(new String[0]));
         runTest("CategoryDAOTest", () -> CategoryDAOTest.main(new String[0]));
         runTest("ProductDAOTest", () -> ProductDAOTest.main(new String[0]));
         runTest("InventoryDAOTest", () -> InventoryDAOTest.main(new String[0]));
         runTest("OrderDAOTest", () -> OrderDAOTest.main(new String[0]));
         runTest("OrderItemDAOTest", () -> OrderItemDAOTest.main(new String[0]));
         runTest("ReviewDAOTest", () -> ReviewDAOTest.main(new String[0]));
         runTest("AddressDAOTest", () -> AddressDAOTest.main(new String[0]));

         System.out.println("============================================");
         System.out.println("TEST SUITE COMPLETE");
         System.out.println("============================================");
         System.out.println("All DAO tests executed.");
         System.out.println("Check output above for individual results.");


     }

     private static void runTest(String name, Runnable testRunner) {
         System.out.println();
         System.out.println("--- Running: " + name + " ---");
         try {
             testRunner.run();
         } catch (Throwable t) {
             System.err.println("Test " + name + " threw an exception:");
             t.printStackTrace();
         }
         System.out.println("--- Finished: " + name + " ---");
     }
 }
