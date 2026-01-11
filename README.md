# ShopJoy E-Commerce Management System

![Build Status](https://github.com/eugeneanokye99/shopjoy/actions/workflows/maven.yml/badge.svg)

## Project Overview
ShopJoy is a comprehensive e-commerce management system built with JavaFX and PostgreSQL. It provides both admin and customer interfaces for managing products, orders, inventory, and customer relationships.

## Features

### Admin Features
- **Dashboard**: Overview statistics and quick access to key metrics
- **Product Management**: Add, edit, delete, and search products
- **Category Management**: Hierarchical category organization
- **Order Management**: Track and update order status
- **Inventory Management**: Monitor stock levels and low stock alerts
- **Customer Management**: View customer information and order history
- **Reviews Management**: Moderate product reviews
- **Reports**: Generate sales and inventory reports

### Customer Features
- **Product Browsing**: Browse products by category
- **Search**: Fast product search with filtering
- **Shopping Cart**: Add products to cart
- **Order Placement**: Create and track orders
- **Reviews**: Write and view product reviews
- **Profile Management**: Update personal information

### Technical Features
- **In-Memory Caching**: Improves performance by 80-95%
- **Database Indexing**: Optimized queries for fast data retrieval
- **Password Encryption**: BCrypt hashing for security
- **Input Validation**: Prevents SQL injection and invalid data
- **Responsive UI**: Modern, clean interface

## Technology Stack
- **Language**: Java 21
- **UI Framework**: JavaFX 21
- **Database**: PostgreSQL 14+
- **Password Hashing**: jBCrypt
- **Build Tool**: Maven
- **Architecture**: MVC pattern with service layer

## Prerequisites
- Java JDK 21 or higher
- PostgreSQL 14 or higher
- Maven 3.8+
- 4GB RAM minimum
- 500MB disk space

## Installation

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/shopjoy.git
cd shopjoy
```

### 2. Set Up Database
```bash
# Create PostgreSQL database
psql -U postgres
CREATE DATABASE shopjoy;
\q

# Run schema creation script
psql -U postgres -d shopjoy -f sql/create_tables.sql

# Load test data (optional)
psql -U postgres -d shopjoy -f sql/test_data.sql
```

### 3. Configure Database Connection
Edit `src/main/resources/application.properties`:
```properties
db.url=jdbc:postgresql://localhost:5432/shopjoy
db.username=postgres
db.password=your_password
db.driver=org.postgresql.Driver
```

### 4. Build the Project
```bash
mvn clean install
```

### 5. Run the Application
```bash
mvn javafx:run
```

## Default Login Credentials

### Admin Account
- Username: `admin`
- Password: `password123`

### Customer Account
- Username: `john_doe`
- Password: `password123`

## Project Structure

| Component / Directory | Description |
| :--- | :--- |
| **`com.shopjoy.model`** | Core entity classes (User, Product, Order, etc.) |
| **`com.shopjoy.dao`** | Data Access Objects for raw SQL interactions |
| **`com.shopjoy.service`** | Business logic layer (authentication, order processing) |
| **`com.shopjoy.controller`** | JavaFX controllers for UI event handling |
| **`com.shopjoy.cache`** | Thread-safe in-memory caching implementation |
| **`com.shopjoy.util`** | AppConfig, Logger, and ExceptionHandler utilities |
| **`src/main/resources/fxml`** | UI layout definitions for Admin and Customer views |
| **`src/main/resources/css`** | Styling, color palettes, and design tokens |
| **`sql/`** | Database schema scripts and sampled test data |
| **`pom.xml`** | Maven dependency and build configuration |

## Database Schema

### Main Tables
1. **users** - Customer and admin accounts
2. **categories** - Product categories (hierarchical)
3. **products** - Product information
4. **inventory** - Stock levels
5. **orders** - Customer orders
6. **order_items** - Items in each order
7. **reviews** - Product reviews
8. **addresses** - Customer shipping addresses

### Key Relationships
- Products belong to Categories
- Orders belong to Users
- Order Items link Orders and Products
- Reviews link Users and Products
- Inventory tracks Product stock

## Performance Optimizations

### Database Indexes
- Indexed on frequently queried columns
- Product name, SKU, category
- Order user ID and date
- Review product ID and user ID

### Caching Strategy
- Product cache: 5-minute expiry
- Category cache: 10-minute expiry
- Search results cached
- Cache invalidation on data changes

### Measured Improvements
- 90%+ faster repeat queries
- Sub-millisecond cache hits
- Reduced database load during peak usage

## Testing

### Run All Tests
```bash
# DAO Tests
java com.shopjoy.test.MasterTestRunner

# Integration Tests
java com.shopjoy.test.IntegrationTest

# Performance Tests
java com.shopjoy.test.PerformanceTest
```

### Test Coverage
- DAO layer: CRUD operations for all entities
- Service layer: Business logic validation
- Integration: End-to-end workflows
- Performance: Cache effectiveness

## Troubleshooting

### Database Connection Fails
- Verify PostgreSQL is running: `sudo systemctl status postgresql`
- Check credentials in application.properties
- Ensure database exists: `psql -U postgres -l`

### Application Won't Start
- Verify Java version: `java -version` (should be 21+)
- Check JavaFX is installed
- Run `mvn clean install` again

### Login Fails
- Verify test data loaded
- Check database connection
- Ensure passwords are hashed with BCrypt

## Future Enhancements
- [ ] Payment gateway integration
- [ ] Email notifications
- [ ] Advanced reporting with charts
- [ ] Product image uploads
- [ ] Multi-currency support
- [ ] Mobile responsive design

## Contributing
1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

## License
This project is licensed under the MIT License - see LICENSE file for details.

## Author
**Eugene Anokye**
- Email: [yawanokye99@gail.com](mailto:yawanokye99@gail.com)
- GitHub: [@eugeneanokye99](https://github.com/eugeneanokye99)

## Acknowledgments
- JavaFX community for excellent documentation
- PostgreSQL team for robust database
- Stack Overflow community for troubleshooting help

---

**Built with ❤️ for Database Fundamentals Course**
