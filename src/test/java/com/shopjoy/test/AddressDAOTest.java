 package com.shopjoy.test;

 import com.shopjoy.dao.AddressDAO;
 import com.shopjoy.dao.UserDAO;
import com.shopjoy.model.Address;
import com.shopjoy.model.AddressType;
 import com.shopjoy.model.User;
 import com.shopjoy.util.DatabaseTestUtil;

 import java.time.LocalDateTime;
 import java.util.List;

 public class AddressDAOTest {
     private static int passed = 0;
     private static int failed = 0;

     public static void main(String[] args) {
         System.out.println("===== TESTING ADDRESS DAO =====");
         AddressDAO addrDao = new AddressDAO();
         UserDAO userDao = new UserDAO();

             DatabaseTestUtil.printTestHeader("Create Address");
             testCreateAddress(addrDao, userDao);

             DatabaseTestUtil.printTestHeader("Find Address By ID");
             testFindAddressById(addrDao, userDao);

             DatabaseTestUtil.printTestHeader("Find By UserId");
             testFindByUserId(addrDao, userDao);

             DatabaseTestUtil.printTestHeader("Set Default Address");
             testSetDefaultAddress(addrDao, userDao);

             DatabaseTestUtil.printTestHeader("Find Default Address");
             testFindDefaultAddress(addrDao, userDao);

             DatabaseTestUtil.printTestHeader("Find Shipping Addresses");
             testFindShippingAddresses(addrDao, userDao);

             DatabaseTestUtil.printTestHeader("Find Billing Addresses");
             testFindBillingAddresses(addrDao, userDao);

             DatabaseTestUtil.printTestHeader("Update Address");
             testUpdateAddress(addrDao, userDao);

             DatabaseTestUtil.printTestHeader("Delete Address");
             testDeleteAddress(addrDao, userDao);

             DatabaseTestUtil.printTestHeader("Delete User Addresses");
             testDeleteUserAddresses(addrDao, userDao);

      
     }

     private static User createTestUser(UserDAO userDAO, String username, String email) throws Exception {
         User u = new User();
         u.setUsername(username);
         u.setEmail(email);
         u.setPasswordHash("password");
         u.setFirstName("T");
         u.setLastName("U");
         u.setCreatedAt(LocalDateTime.now());
         u.setUpdatedAt(LocalDateTime.now());
         return userDAO.save(u);
     }

    private static Address createAddress(AddressDAO dao, int userId, AddressType type, boolean isDefault) throws Exception {
        Address a = new Address();
        a.setUserId(userId);
        a.setStreetAddress("123 Main St");
        a.setCity("City");
        a.setState("State");
        a.setPostalCode("12345");
        a.setCountry("Country");
        a.setAddressType(type);
        a.setDefault(isDefault);
        a.setCreatedAt(LocalDateTime.now());
        return dao.save(a);
    }

     private static void testCreateAddress(AddressDAO dao, UserDAO udao) {
         try {
             DatabaseTestUtil.clearAllTables();
             User u = createTestUser(udao, "addruser", "addr@test.com");
            Address a = createAddress(dao, u.getUserId(), AddressType.SHIPPING, false);
             boolean ok = a != null && a.getAddressId() > 0;
             DatabaseTestUtil.printTestResult("testCreateAddress", ok);
             if (ok) passed++; else failed++;
         } catch (Exception e) {
             failed++;
             System.err.println("testCreateAddress exception:");
             e.printStackTrace();
         }
     }

     private static void testFindAddressById(AddressDAO dao, UserDAO udao) {
         try {
             DatabaseTestUtil.clearAllTables();
             User u = createTestUser(udao, "fauser", "fa@test.com");
            Address a = createAddress(dao, u.getUserId(), AddressType.SHIPPING, false);
             Address found = dao.findById(a.getAddressId());
             boolean ok = found != null && found.getAddressId() == a.getAddressId();
             DatabaseTestUtil.printTestResult("testFindAddressById", ok);
             if (ok) passed++; else failed++;
         } catch (Exception e) {
             failed++;
             System.err.println("testFindAddressById exception:");
             e.printStackTrace();
         }
     }

     private static void testFindByUserId(AddressDAO dao, UserDAO udao) {
         try {
             DatabaseTestUtil.clearAllTables();
             User u = createTestUser(udao, "fuuser", "fu@test.com");
            createAddress(dao, u.getUserId(), AddressType.SHIPPING, false);
            createAddress(dao, u.getUserId(), AddressType.BILLING, false);
             List<Address> list = dao.findByUserId(u.getUserId());
             boolean ok = list != null && list.size() == 2;
             DatabaseTestUtil.printTestResult("testFindByUserId", ok);
             if (ok) passed++; else failed++;
         } catch (Exception e) {
             failed++;
             System.err.println("testFindByUserId exception:");
             e.printStackTrace();
         }
     }

     private static void testSetDefaultAddress(AddressDAO dao, UserDAO udao) {
         try {
             DatabaseTestUtil.clearAllTables();
             User u = createTestUser(udao, "defuser", "def@test.com");
            Address a1 = createAddress(dao, u.getUserId(), AddressType.SHIPPING, false);
            Address a2 = createAddress(dao, u.getUserId(), AddressType.SHIPPING, false);
            Address a3 = createAddress(dao, u.getUserId(), AddressType.SHIPPING, false);

            // set a1 default (note DAO signature: setDefaultAddress(addressId, userId, type))
            boolean ok1 = dao.setDefaultAddress(a1.getAddressId(), u.getUserId(), AddressType.SHIPPING);
             Address d1 = dao.findById(a1.getAddressId());
             Address d2 = dao.findById(a2.getAddressId());
             Address d3 = dao.findById(a3.getAddressId());
             boolean state1 = d1 != null && d1.isDefault() && d2 != null && !d2.isDefault() && d3 != null && !d3.isDefault();

             // set a2 default
            boolean ok2 = dao.setDefaultAddress(a2.getAddressId(), u.getUserId(), AddressType.SHIPPING);
             d1 = dao.findById(a1.getAddressId());
             d2 = dao.findById(a2.getAddressId());
             d3 = dao.findById(a3.getAddressId());
             boolean state2 = d2 != null && d2.isDefault() && d1 != null && !d1.isDefault() && d3 != null && !d3.isDefault();

             boolean ok = ok1 && ok2 && state1 && state2;
             DatabaseTestUtil.printTestResult("testSetDefaultAddress", ok);
             if (ok) passed++; else failed++;
         } catch (Exception e) {
             failed++;
             System.err.println("testSetDefaultAddress exception:");
             e.printStackTrace();
         }
     }

     private static void testFindDefaultAddress(AddressDAO dao, UserDAO udao) {
         try {
             DatabaseTestUtil.clearAllTables();
             User u = createTestUser(udao, "dfuser", "df@test.com");
            Address s1 = createAddress(dao, u.getUserId(), AddressType.SHIPPING, false);
            Address s2 = createAddress(dao, u.getUserId(), AddressType.SHIPPING, false);
            Address b1 = createAddress(dao, u.getUserId(), AddressType.BILLING, false);
            dao.setDefaultAddress(s2.getAddressId(), u.getUserId(), AddressType.SHIPPING);
            Address def = dao.findDefaultAddress(u.getUserId(), AddressType.SHIPPING);
             boolean ok = def != null && def.getAddressId() == s2.getAddressId();
             DatabaseTestUtil.printTestResult("testFindDefaultAddress", ok);
             if (ok) passed++; else failed++;
         } catch (Exception e) {
             failed++;
             System.err.println("testFindDefaultAddress exception:");
             e.printStackTrace();
         }
     }

     private static void testFindShippingAddresses(AddressDAO dao, UserDAO udao) {
         try {
             DatabaseTestUtil.clearAllTables();
             User u = createTestUser(udao, "shuser", "sh@test.com");
            createAddress(dao, u.getUserId(), AddressType.SHIPPING, false);
            createAddress(dao, u.getUserId(), AddressType.SHIPPING, false);
            createAddress(dao, u.getUserId(), AddressType.SHIPPING, false);
            createAddress(dao, u.getUserId(), AddressType.BILLING, false);
            createAddress(dao, u.getUserId(), AddressType.BILLING, false);
            List<Address> sh = dao.findShippingAddresses(u.getUserId());
             boolean ok = sh != null && sh.size() == 3;
             DatabaseTestUtil.printTestResult("testFindShippingAddresses", ok);
             if (ok) passed++; else failed++;
         } catch (Exception e) {
             failed++;
             System.err.println("testFindShippingAddresses exception:");
             e.printStackTrace();
         }
     }

     private static void testFindBillingAddresses(AddressDAO dao, UserDAO udao) {
         try {
             DatabaseTestUtil.clearAllTables();
             User u = createTestUser(udao, "billuser", "bill@test.com");
            createAddress(dao, u.getUserId(), AddressType.SHIPPING, false);
            createAddress(dao, u.getUserId(), AddressType.BILLING, false);
            createAddress(dao, u.getUserId(), AddressType.BILLING, false);
            List<Address> bill = dao.findBillingAddresses(u.getUserId());
             boolean ok = bill != null && bill.size() == 2;
             DatabaseTestUtil.printTestResult("testFindBillingAddresses", ok);
             if (ok) passed++; else failed++;
         } catch (Exception e) {
             failed++;
             System.err.println("testFindBillingAddresses exception:");
             e.printStackTrace();
         }
     }

     private static void testUpdateAddress(AddressDAO dao, UserDAO udao) {
         try {
             DatabaseTestUtil.clearAllTables();
             User u = createTestUser(udao, "upaddr", "upaddr@test.com");
            Address a = createAddress(dao, u.getUserId(), AddressType.SHIPPING, false);
             a.setCity("NewCity");
             dao.update(a);
             Address r = dao.findById(a.getAddressId());
             boolean ok = r != null && "NewCity".equals(r.getCity());
             DatabaseTestUtil.printTestResult("testUpdateAddress", ok);
             if (ok) passed++; else failed++;
         } catch (Exception e) {
             failed++;
             System.err.println("testUpdateAddress exception:");
             e.printStackTrace();
         }
     }

     private static void testDeleteAddress(AddressDAO dao, UserDAO udao) {
         try {
             DatabaseTestUtil.clearAllTables();
             User u = createTestUser(udao, "deladdr", "deladdr@test.com");
            Address a = createAddress(dao, u.getUserId(), AddressType.SHIPPING, false);
             boolean del = dao.delete(a.getAddressId());
             Address found = dao.findById(a.getAddressId());
             boolean ok = del && found == null;
             DatabaseTestUtil.printTestResult("testDeleteAddress", ok);
             if (ok) passed++; else failed++;
         } catch (Exception e) {
             failed++;
             System.err.println("testDeleteAddress exception:");
             e.printStackTrace();
         }
     }

     private static void testDeleteUserAddresses(AddressDAO dao, UserDAO udao) {
         try {
             DatabaseTestUtil.clearAllTables();
             User u = createTestUser(udao, "deluseradd", "deluseradd@test.com");
            createAddress(dao, u.getUserId(), AddressType.SHIPPING, false);
            createAddress(dao, u.getUserId(), AddressType.BILLING, false);
             dao.deleteUserAddresses(u.getUserId());
             List<Address> list = dao.findByUserId(u.getUserId());
             boolean ok = list == null || list.isEmpty();
             DatabaseTestUtil.printTestResult("testDeleteUserAddresses", ok);
             if (ok) passed++; else failed++;
         } catch (Exception e) {
             failed++;
             System.err.println("testDeleteUserAddresses exception:");
             e.printStackTrace();
         }
     }
 }
