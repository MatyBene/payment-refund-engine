package com.matiasbenedetti.refund.config;

import com.matiasbenedetti.refund.model.Account;
import com.matiasbenedetti.refund.model.Transaction;
import com.matiasbenedetti.refund.model.TransactionStatus;
import com.matiasbenedetti.refund.model.TransactionType;
import com.matiasbenedetti.refund.repository.AccountRepository;
import com.matiasbenedetti.refund.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public DataInitializer(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void run(String... args) {
        if(accountRepository.count() > 0) {
            log.info("Database already contains data, skipping seed");
            return;
        }

        log.info("Seeding database with sample data...");

        // Create a default account with an initial balance
        Account account = new Account(new BigDecimal("5000.000"));
        account = accountRepository.save(account);
        log.info("Created account: {} with balance {}", account.getId(), account.getBalance());

        // Create some approved purchase transactions eligible for refund
        Transaction txn1 = new Transaction(
                account, new BigDecimal("150.00"),
                TransactionType.PURCHASE, TransactionStatus.APPROVED);
        txn1 = transactionRepository.save(txn1);
        log.info("Created transaction: {} amount {} (APPROVED)", txn1.getId(), txn1.getAmount());

        Transaction txn2 = new Transaction(
                account, new BigDecimal("299.99"),
                TransactionType.PURCHASE, TransactionStatus.APPROVED);
        txn2 = transactionRepository.save(txn2);
        log.info("Created transaction: {} amount {} (APPROVED)", txn2.getId(), txn2.getAmount());

        // A pending transaction — should NOT be refundable
        Transaction txn3 = new Transaction(
                account, new BigDecimal("50.00"),
                TransactionType.PURCHASE, TransactionStatus.PENDING);
        txn3 = transactionRepository.save(txn3);
        log.info("Created transaction: {} amount {} (PENDING — not refundable)", txn3.getId(), txn3.getAmount());

        // A rejected transaction — should NOT be refundable
        Transaction txn4 = new Transaction(
                account, new BigDecimal("75.00"),
                TransactionType.PURCHASE, TransactionStatus.REJECTED);
        txn4 = transactionRepository.save(txn4);
        log.info("Created transaction: {} amount {} (REJECTED — not refundable)", txn4.getId(), txn4.getAmount());

        log.info("Database seeding complete. Ready to process refunds.");
    }
}
