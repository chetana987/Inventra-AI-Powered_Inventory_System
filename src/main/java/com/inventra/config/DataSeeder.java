package com.inventra.config;

import com.inventra.entity.InventoryTransaction;
import com.inventra.entity.Product;
import com.inventra.entity.Role;
import com.inventra.entity.TransactionType;
import com.inventra.entity.User;
import com.inventra.repository.InventoryTransactionRepository;
import com.inventra.repository.ProductRepository;
import com.inventra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile({"dev", "railway"})
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded, skipping");
            return;
        }

        log.info("Seeding database with sample data...");

        User admin = User.builder()
                .name("Admin")
                .email("admin@inventra.com")
                .password(passwordEncoder.encode("Admin@123"))
                .role(Role.ADMIN)
                .build();
        userRepository.save(admin);

        User staff = User.builder()
                .name("Staff")
                .email("staff@inventra.com")
                .password(passwordEncoder.encode("Staff@123"))
                .role(Role.STAFF)
                .build();
        userRepository.save(staff);

        List<Product> products = List.of(
                Product.builder().productCode("PRD001").name("Laptop ThinkPad X1").category("Electronics").description("Business laptop 16GB RAM, 512GB SSD").price(new BigDecimal("1200.00")).quantity(18).minimumStockLevel(10).build(),
                Product.builder().productCode("PRD002").name("Wireless Mouse MX Master").category("Electronics").description("Logitech wireless mouse with ergonomic design").price(new BigDecimal("79.99")).quantity(42).minimumStockLevel(20).build(),
                Product.builder().productCode("PRD003").name("Office Desk 140x70").category("Furniture").description("Wooden office desk with cable management").price(new BigDecimal("450.00")).quantity(7).minimumStockLevel(5).build(),
                Product.builder().productCode("PRD004").name("Ergonomic Office Chair").category("Furniture").description("Mesh back chair with lumbar support").price(new BigDecimal("350.00")).quantity(3).minimumStockLevel(10).build(),
                Product.builder().productCode("PRD005").name("USB-C Hub 7-in-1").category("Electronics").description("7-in-1 USB-C hub with HDMI, USB-A, SD card").price(new BigDecimal("45.00")).quantity(88).minimumStockLevel(30).build(),
                Product.builder().productCode("PRD006").name("Notebook A5 Lined").category("Stationery").description("Pack of 10 premium notebooks").price(new BigDecimal("15.00")).quantity(210).minimumStockLevel(100).build(),
                Product.builder().productCode("PRD007").name("Ballpoint Pen Pack").category("Stationery").description("Pack of 12 blue ink pens").price(new BigDecimal("8.50")).quantity(140).minimumStockLevel(50).build(),
                Product.builder().productCode("PRD008").name("Monitor 27-inch 4K").category("Electronics").description("Dell 4K UHD USB-C monitor").price(new BigDecimal("599.99")).quantity(3).minimumStockLevel(5).build(),
                Product.builder().productCode("PRD009").name("Standing Desk Converter").category("Furniture").description("Adjustable height standing desk converter").price(new BigDecimal("280.00")).quantity(2).minimumStockLevel(5).build(),
                Product.builder().productCode("PRD010").name("Keyboard Mechanical RGB").category("Electronics").description("Cherry MX Blue switches, RGB backlit").price(new BigDecimal("129.99")).quantity(25).minimumStockLevel(10).build()
        );
        productRepository.saveAll(products);

        List<InventoryTransaction> transactions = buildTransactions(products, admin, staff);
        transactionRepository.saveAll(transactions);

        long count = productRepository.count();
        long transactionCount = transactionRepository.count();
        log.info("Seeded {} users, {} products, and {} transactions", 2, count, transactionCount);
    }

    private List<InventoryTransaction> buildTransactions(List<Product> products, User admin, User staff) {
        LocalDateTime now = LocalDateTime.now();

        return List.of(
                // PRD001 - Laptop
                t(products.get(0), TransactionType.STOCK_IN, 30, admin, now.minusDays(28), "Initial stock purchase"),
                t(products.get(0), TransactionType.STOCK_OUT, 8, staff, now.minusDays(20), "Employee laptop allocation"),
                t(products.get(0), TransactionType.STOCK_OUT, 4, staff, now.minusDays(10), "New hire equipment"),

                // PRD002 - Mouse
                t(products.get(1), TransactionType.STOCK_IN, 60, admin, now.minusDays(27), "Bulk order from Logitech"),
                t(products.get(1), TransactionType.STOCK_OUT, 12, staff, now.minusDays(15), "Department requisition"),
                t(products.get(1), TransactionType.STOCK_OUT, 6, staff, now.minusDays(5), "Replacement requests"),

                // PRD003 - Desk
                t(products.get(2), TransactionType.STOCK_IN, 10, admin, now.minusDays(25), "Furniture shipment"),
                t(products.get(2), TransactionType.STOCK_OUT, 3, staff, now.minusDays(12), "Office expansion"),

                // PRD004 - Chair (low stock item)
                t(products.get(3), TransactionType.STOCK_IN, 15, admin, now.minusDays(30), "Warehouse restock"),
                t(products.get(3), TransactionType.STOCK_OUT, 7, staff, now.minusDays(18), "Hiring batch"),
                t(products.get(3), TransactionType.STOCK_OUT, 5, staff, now.minusDays(7), "Ergonomics upgrade program"),

                // PRD005 - USB Hub
                t(products.get(4), TransactionType.STOCK_IN, 120, admin, now.minusDays(22), "Supplier delivery"),
                t(products.get(4), TransactionType.STOCK_OUT, 20, staff, now.minusDays(14), "IT equipment pack"),
                t(products.get(4), TransactionType.STOCK_OUT, 12, staff, now.minusDays(3), "Remote worker kits"),

                // PRD006 - Notebooks
                t(products.get(5), TransactionType.STOCK_IN, 300, admin, now.minusDays(26), "Annual stationery order"),
                t(products.get(5), TransactionType.STOCK_OUT, 50, staff, now.minusDays(16), "Department distribution"),
                t(products.get(5), TransactionType.STOCK_OUT, 40, staff, now.minusDays(6), "Training materials"),

                // PRD007 - Pens
                t(products.get(6), TransactionType.STOCK_IN, 200, admin, now.minusDays(24), "Stationery restock"),
                t(products.get(6), TransactionType.STOCK_OUT, 60, staff, now.minusDays(11), "Office supply refill"),

                // PRD008 - Monitor (low stock item)
                t(products.get(7), TransactionType.STOCK_IN, 10, admin, now.minusDays(29), "Dell order"),
                t(products.get(7), TransactionType.STOCK_OUT, 4, staff, now.minusDays(19), "Developer setup"),
                t(products.get(7), TransactionType.STOCK_OUT, 3, staff, now.minusDays(8), "Design team upgrade"),

                // PRD009 - Standing desk converter (low stock item)
                t(products.get(8), TransactionType.STOCK_IN, 8, admin, now.minusDays(23), "Wellness initiative order"),
                t(products.get(8), TransactionType.STOCK_OUT, 4, staff, now.minusDays(13), "Department pilot"),
                t(products.get(8), TransactionType.STOCK_OUT, 2, staff, now.minusDays(4), "Additional request"),

                // PRD010 - Keyboard
                t(products.get(9), TransactionType.STOCK_IN, 35, admin, now.minusDays(21), "Peripherals restock"),
                t(products.get(9), TransactionType.STOCK_OUT, 6, staff, now.minusDays(9), "Developer preference"),
                t(products.get(9), TransactionType.STOCK_OUT, 4, staff, now.minusDays(2), "Team replacement")
        );
    }

    private InventoryTransaction t(Product product, TransactionType type, int qty, User user, LocalDateTime date, String remarks) {
        return InventoryTransaction.builder()
                .product(product)
                .transactionType(type)
                .quantity(qty)
                .performedBy(user)
                .transactionDate(date)
                .remarks(remarks)
                .build();
    }
}
