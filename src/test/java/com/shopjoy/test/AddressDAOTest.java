// package com.shopjoy.test;

// import com.shopjoy.dao.AddressDAO;
// import com.shopjoy.dao.UserDAO;
// import com.shopjoy.model.Address;
// import com.shopjoy.model.User;
// import com.shopjoy.util.DatabaseConfig;
// import com.shopjoy.util.DatabaseTestUtil;

// import java.time.LocalDateTime;
// import java.util.List;

// public class AddressDAOTest {
//     private static int passed = 0;
//     private static int failed = 0;

//     public static void main(String[] args) {
//         System.out.println("===== TESTING ADDRESS DAO =====");
//         AddressDAO addrDao = new AddressDAO();
//         UserDAO userDao = new UserDAO();

//         try {
//             DatabaseTestUtil.printTestHeader("Create Address");
//             testCreateAddress(addrDao, userDao);

//             DatabaseTestUtil.printTestHeader("Find Address By ID");
//             testFindAddressById(addrDao, userDao);

//             DatabaseTestUtil.printTestHeader("Find By UserId");
//             testFindByUserId(addrDao, userDao);

//             DatabaseTestUtil.printTestHeader("Set Default Address");
//             testSetDefaultAddress(addrDao, userDao);

//             DatabaseTestUtil.printTestHeader("Find Default Address");
//             testFindDefaultAddress(addrDao, userDao);

//             DatabaseTestUtil.printTestHeader("Find Shipping Addresses");
//             testFindShippingAddresses(addrDao, userDao);

//             DatabaseTestUtil.printTestHeader("Find Billing Addresses");
//             testFindBillingAddresses(addrDao, userDao);

//             DatabaseTestUtil.printTestHeader("Update Address");
//             testUpdateAddress(addrDao, userDao);

//             DatabaseTestUtil.printTestHeader("Delete Address");
//             testDeleteAddress(addrDao, userDao);

//             DatabaseTestUtil.printTestHeader("Delete User Addresses");
//             testDeleteUserAddresses(addrDao, userDao);

//         } finally {
//             System.out.println("Tests complete. Passed: " + passed + ", Failed: " + failed);
//             DatabaseConfig.getInstance().closeAllConnections();
//         }
//     }

//     private static User createTestUser(UserDAO userDAO, String username, String email) throws Exception {
//         User u = new User();
//         u.setUsername(username);
//         u.setEmail(email);
//         u.setPasswordHash("password");
//         u.setFirstName("T");
//         u.setLastName("U");
//         u.setCreatedAt(LocalDateTime.now());
//         u.setUpdatedAt(LocalDateTime.now());
//         return userDAO.save(u);
//     }

//     private static Address createAddress(AddressDAO dao, int userId, String type, boolean isDefault) throws Exception {
//         Address a = new Address();
//         a.setUserId(userId);
//         a.setAddressLine1("123 Main St");
//         a.setCity("City");
//         a.setState("State");
//         a.setPostalCode("12345");
//         a.setCountry("Country");
//         a.setAddressType(type);
//         a.setIsDefault(isDefault);
//         a.setCreatedAt(LocalDateTime.now());
//         a.setUpdatedAt(LocalDateTime.now());
//         return dao.save(a);
//     }

//     private static void testCreateAddress(AddressDAO dao, UserDAO udao) {
//         try {
//             DatabaseTestUtil.clearAllTables();
//             User u = createTestUser(udao, "addruser", "addr@test.com");
//             Address a = createAddress(dao, u.getUserId(), "SHIPPING", false);
//             boolean ok = a != null && a.getAddressId() > 0;
//             DatabaseTestUtil.printTestResult("testCreateAddress", ok);
//             if (ok) passed++; else failed++;
//         } catch (Exception e) {
//             failed++;
//             System.err.println("testCreateAddress exception:");
//             e.printStackTrace();
//         }
//     }

//     private static void testFindAddressById(AddressDAO dao, UserDAO udao) {
//         try {
//             DatabaseTestUtil.clearAllTables();
//             User u = createTestUser(udao, "fauser", "fa@test.com");
//             Address a = createAddress(dao, u.getUserId(), "SHIPPING", false);
//             Address found = dao.findById(a.getAddressId());
//             boolean ok = found != null && found.getAddressId() == a.getAddressId();
//             DatabaseTestUtil.printTestResult("testFindAddressById", ok);
//             if (ok) passed++; else failed++;
//         } catch (Exception e) {
//             failed++;
//             System.err.println("testFindAddressById exception:");
//             e.printStackTrace();
//         }
//     }

//     private static void testFindByUserId(AddressDAO dao, UserDAO udao) {
//         try {
//             DatabaseTestUtil.clearAllTables();
//             User u = createTestUser(udao, "fuuser", "fu@test.com");
//             createAddress(dao, u.getUserId(), "SHIPPING", false);
//             createAddress(dao, u.getUserId(), "BILLING", false);
//             List<Address> list = dao.findByUserId(u.getUserId());
//             boolean ok = list != null && list.size() == 2;
//             DatabaseTestUtil.printTestResult("testFindByUserId", ok);
//             if (ok) passed++; else failed++;
//         } catch (Exception e) {
//             failed++;
//             System.err.println("testFindByUserId exception:");
//             e.printStackTrace();
//         }
//     }

//     private static void testSetDefaultAddress(AddressDAO dao, UserDAO udao) {
//         try {
//             DatabaseTestUtil.clearAllTables();
//             User u = createTestUser(udao, "defuser", "def@test.com");
//             Address a1 = createAddress(dao, u.getUserId(), "SHIPPING", false);
//             Address a2 = createAddress(dao, u.getUserId(), "SHIPPING", false);
//             Address a3 = createAddress(dao, u.getUserId(), "SHIPPING", false);

//             // set a1 default
//             boolean ok1 = dao.setDefaultAddress(u.getUserId(), a1.getAddressId());
//             Address d1 = dao.findById(a1.getAddressId());
//             Address d2 = dao.findById(a2.getAddressId());
//             Address d3 = dao.findById(a3.getAddressId());
//             boolean state1 = d1 != null && d1.getIsDefault() && d2 != null && !d2.getIsDefault() && d3 != null && !d3.getIsDefault();

//             // set a2 default
//             boolean ok2 = dao.setDefaultAddress(u.getUserId(), a2.getAddressId());
//             d1 = dao.findById(a1.getAddressId());
//             d2 = dao.findById(a2.getAddressId());
//             d3 = dao.findById(a3.getAddressId());
//             boolean state2 = d2 != null && d2.getIsDefault() && d1 != null && !d1.getIsDefault() && d3 != null && !d3.getIsDefault();

//             boolean ok = ok1 && ok2 && state1 && state2;
//             DatabaseTestUtil.printTestResult("testSetDefaultAddress", ok);
//             if (ok) passed++; else failed++;
//         } catch (Exception e) {
//             failed++;
//             System.err.println("testSetDefaultAddress exception:");
//             e.printStackTrace();
//         }
//     }

//     private static void testFindDefaultAddress(AddressDAO dao, UserDAO udao) {
//         try {
//             DatabaseTestUtil.clearAllTables();
//             User u = createTestUser(udao, "dfuser", "df@test.com");
//             Address s1 = createAddress(dao, u.getUserId(), "SHIPPING", false);
//             Address s2 = createAddress(dao, u.getUserId(), "SHIPPING", false);
//             Address b1 = createAddress(dao, u.getUserId(), "BILLING", false);
//             dao.setDefaultAddress(u.getUserId(), s2.getAddressId());
//             Address def = dao.findDefaultAddress(u.getUserId(), "SHIPPING");
//             boolean ok = def != null && def.getAddressId() == s2.getAddressId();
//             DatabaseTestUtil.printTestResult("testFindDefaultAddress", ok);
//             if (ok) passed++; else failed++;
//         } catch (Exception e) {
//             failed++;
//             System.err.println("testFindDefaultAddress exception:");
//             e.printStackTrace();
//         }
//     }

//     private static void testFindShippingAddresses(AddressDAO dao, UserDAO udao) {
//         try {
//             DatabaseTestUtil.clearAllTables();
//             User u = createTestUser(udao, "shuser", "sh@test.com");
//             createAddress(dao, u.getUserId(), "SHIPPING", false);
//             createAddress(dao, u.getUserId(), "SHIPPING", false);
//             createAddress(dao, u.getUserId(), "SHIPPING", false);
//             createAddress(dao, u.getUserId(), "BILLING", false);
//             createAddress(dao, u.getUserId(), "BILLING", false);
//             List<Address> sh = dao.findByTypeForUser(u.getUserId(), "SHIPPING");
//             boolean ok = sh != null && sh.size() == 3;
//             DatabaseTestUtil.printTestResult("testFindShippingAddresses", ok);
//             if (ok) passed++; else failed++;
//         } catch (Exception e) {
//             failed++;
//             System.err.println("testFindShippingAddresses exception:");
//             e.printStackTrace();
//         }
//     }

//     private static void testFindBillingAddresses(AddressDAO dao, UserDAO udao) {
//         try {
//             DatabaseTestUtil.clearAllTables();
//             User u = createTestUser(udao, "billuser", "bill@test.com");
//             createAddress(dao, u.getUserId(), "SHIPPING", false);
//             createAddress(dao, u.getUserId(), "BILLING", false);
//             createAddress(dao, u.getUserId(), "BILLING", false);
//             List<Address> bill = dao.findByTypeForUser(u.getUserId(), "BILLING");
//             boolean ok = bill != null && bill.size() == 2;
//             DatabaseTestUtil.printTestResult("testFindBillingAddresses", ok);
//             if (ok) passed++; else failed++;
//         } catch (Exception e) {
//             failed++;
//             System.err.println("testFindBillingAddresses exception:");
//             e.printStackTrace();
//         }
//     }

//     private static void testUpdateAddress(AddressDAO dao, UserDAO udao) {
//         try {
//             DatabaseTestUtil.clearAllTables();
//             User u = createTestUser(udao, "upaddr", "upaddr@test.com");
//             Address a = createAddress(dao, u.getUserId(), "SHIPPING", false);
//             a.setCity("NewCity");
//             dao.update(a);
//             Address r = dao.findById(a.getAddressId());
//             boolean ok = r != null && "NewCity".equals(r.getCity());
//             DatabaseTestUtil.printTestResult("testUpdateAddress", ok);
//             if (ok) passed++; else failed++;
//         } catch (Exception e) {
//             failed++;
//             System.err.println("testUpdateAddress exception:");
//             e.printStackTrace();
//         }
//     }

//     private static void testDeleteAddress(AddressDAO dao, UserDAO udao) {
//         try {
//             DatabaseTestUtil.clearAllTables();
//             User u = createTestUser(udao, "deladdr", "deladdr@test.com");
//             Address a = createAddress(dao, u.getUserId(), "SHIPPING", false);
//             boolean del = dao.delete(a.getAddressId());
//             Address found = dao.findById(a.getAddressId());
//             boolean ok = del && found == null;
//             DatabaseTestUtil.printTestResult("testDeleteAddress", ok);
//             if (ok) passed++; else failed++;
//         } catch (Exception e) {
//             failed++;
//             System.err.println("testDeleteAddress exception:");
//             e.printStackTrace();
//         }
//     }

//     private static void testDeleteUserAddresses(AddressDAO dao, UserDAO udao) {
//         try {
//             DatabaseTestUtil.clearAllTables();
//             User u = createTestUser(udao, "deluseradd", "deluseradd@test.com");
//             createAddress(dao, u.getUserId(), "SHIPPING", false);
//             createAddress(dao, u.getUserId(), "BILLING", false);
//             dao.deleteByUserId(u.getUserId());
//             List<Address> list = dao.findByUserId(u.getUserId());
//             boolean ok = list == null || list.isEmpty();
//             DatabaseTestUtil.printTestResult("testDeleteUserAddresses", ok);
//             if (ok) passed++; else failed++;
//         } catch (Exception e) {
//             failed++;
//             System.err.println("testDeleteUserAddresses exception:");
//             e.printStackTrace();
//         }
//     }
// }
