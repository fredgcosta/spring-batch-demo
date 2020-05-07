package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import com.example.demo.models.RegTypeOne;
import com.example.demo.models.RegTypeThree;
import com.example.demo.models.RegTypeTwo;
import com.example.demo.models.Transaction;
import com.example.demo.repositories.TransactionRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class TransactionRepositoryTest {

        @Autowired
        private DataSource dataSource;
        @Autowired
        private JdbcTemplate jdbcTemplate;
        @Autowired
        private EntityManager entityManager;
        @Autowired
        private TransactionRepository transactionRepository;

        @Test
        void injectedComponentsAreNotNull() {
                assertThat(dataSource).isNotNull();
                assertThat(jdbcTemplate).isNotNull();
                assertThat(entityManager).isNotNull();
                assertThat(transactionRepository).isNotNull();
        }

        @Test
        public void testSaveAllTransactions() {

                List<Transaction> transactions = new ArrayList<>(3);

                Transaction transaction1 = new Transaction();
                transaction1.setRegId("0100");
                transaction1.setField01("field01");
                transaction1.setField02("field02");
                transaction1.setField03("field03");
                transaction1.setId(1L);

                RegTypeOne regTypeOne1 = new RegTypeOne();
                regTypeOne1.setRegId("0101");
                regTypeOne1.setField01("field11");
                regTypeOne1.setField02("field12");
                regTypeOne1.setField03("field13");
                regTypeOne1.setTransaction(transaction1);
                regTypeOne1.setId(1L);
                transaction1.setRegTypeOne(regTypeOne1);

                RegTypeTwo regTypeTwo1 = new RegTypeTwo();
                regTypeTwo1.setRegId("0102");
                regTypeTwo1.setField01("field21");
                regTypeTwo1.setField02("field22");
                regTypeTwo1.setField03("field23");
                regTypeTwo1.setTransaction(transaction1);
                regTypeTwo1.setId(1L);
                transaction1.setRegTypeTwo(regTypeTwo1);

                RegTypeThree regTypeThree1 = new RegTypeThree();
                regTypeThree1.setRegId("0103");
                regTypeThree1.setField01("field31");
                regTypeThree1.setField02("field32");
                regTypeThree1.setField03("field33");
                regTypeThree1.setTransaction(transaction1);
                regTypeThree1.setId(1L);
                transaction1.setRegTypeThree(regTypeThree1);

                Transaction transaction2 = new Transaction();
                transaction2.setRegId("0100");
                transaction2.setField01("field01");
                transaction2.setField02("field02");
                transaction2.setField03("field03");
                transaction2.setId(2L);

                RegTypeOne regTypeOne2 = new RegTypeOne();
                regTypeOne2.setRegId("0101");
                regTypeOne2.setField01("field11");
                regTypeOne2.setField02("field12");
                regTypeOne2.setField03("field13");
                regTypeOne2.setTransaction(transaction2);
                regTypeOne2.setId(2L);
                transaction2.setRegTypeOne(regTypeOne2);

                RegTypeTwo regTypeTwo2 = new RegTypeTwo();
                regTypeTwo2.setRegId("0102");
                regTypeTwo2.setField01("field21");
                regTypeTwo2.setField02("field22");
                regTypeTwo2.setField03("field23");
                regTypeTwo2.setTransaction(transaction2);
                regTypeTwo2.setId(2L);
                transaction2.setRegTypeTwo(regTypeTwo2);

                RegTypeThree regTypeThree2 = new RegTypeThree();
                regTypeThree2.setRegId("0103");
                regTypeThree2.setField01("field31");
                regTypeThree2.setField02("field32");
                regTypeThree2.setField03("field33");
                regTypeThree2.setTransaction(transaction2);
                regTypeThree2.setId(2L);
                transaction2.setRegTypeThree(regTypeThree2);

                Transaction transaction3 = new Transaction();
                transaction3.setRegId("0100");
                transaction3.setField01("field01");
                transaction3.setField02("field02");
                transaction3.setField03("field03");
                transaction3.setId(3L);

                RegTypeOne regTypeOne3 = new RegTypeOne();
                regTypeOne3.setRegId("0101");
                regTypeOne3.setField01("field11");
                regTypeOne3.setField02("field12");
                regTypeOne3.setField03("field13");
                regTypeOne3.setTransaction(transaction3);
                regTypeOne3.setId(3L);
                transaction3.setRegTypeOne(regTypeOne3);

                RegTypeTwo regTypeTwo3 = new RegTypeTwo();
                regTypeTwo3.setRegId("0102");
                regTypeTwo3.setField01("field21");
                regTypeTwo3.setField02("field22");
                regTypeTwo3.setField03("field23");
                regTypeTwo3.setTransaction(transaction3);
                regTypeTwo3.setId(3L);
                transaction3.setRegTypeTwo(regTypeTwo3);

                RegTypeThree regTypeThree3 = new RegTypeThree();
                regTypeThree3.setRegId("0103");
                regTypeThree3.setField01("field31");
                regTypeThree3.setField02("field32");
                regTypeThree3.setField03("field33");
                regTypeThree3.setTransaction(transaction3);
                regTypeThree3.setId(3L);
                transaction3.setRegTypeThree(regTypeThree3);

                transactions.add(transaction1);
                transactions.add(transaction2);
                transactions.add(transaction3);

                List<Transaction> savedTransactions = transactionRepository.saveAll(transactions);
                assertThat(savedTransactions).isEqualTo(transactions);

        }

        @Test
        public void testIdempotencyOfSaveAllTransactions() {

                List<Transaction> transactions = new ArrayList<>(3);

                Transaction transaction1 = new Transaction();
                transaction1.setRegId("0100");
                transaction1.setField01("field01");
                transaction1.setField02("field02");
                transaction1.setField03("field03");
                transaction1.setId(1L);

                RegTypeOne regTypeOne1 = new RegTypeOne();
                regTypeOne1.setRegId("0101");
                regTypeOne1.setField01("field11");
                regTypeOne1.setField02("field12");
                regTypeOne1.setField03("field13");
                regTypeOne1.setTransaction(transaction1);
                regTypeOne1.setId(1L);
                transaction1.setRegTypeOne(regTypeOne1);

                RegTypeTwo regTypeTwo1 = new RegTypeTwo();
                regTypeTwo1.setRegId("0102");
                regTypeTwo1.setField01("field21");
                regTypeTwo1.setField02("field22");
                regTypeTwo1.setField03("field23");
                regTypeTwo1.setTransaction(transaction1);
                regTypeTwo1.setId(1L);
                transaction1.setRegTypeTwo(regTypeTwo1);

                RegTypeThree regTypeThree1 = new RegTypeThree();
                regTypeThree1.setRegId("0103");
                regTypeThree1.setField01("field31");
                regTypeThree1.setField02("field32");
                regTypeThree1.setField03("field33");
                regTypeThree1.setTransaction(transaction1);
                regTypeThree1.setId(1L);
                transaction1.setRegTypeThree(regTypeThree1);

                Transaction transaction2 = new Transaction();
                transaction2.setRegId("0100");
                transaction2.setField01("field01");
                transaction2.setField02("field02");
                transaction2.setField03("field03");
                transaction2.setId(1L);

                RegTypeOne regTypeOne2 = new RegTypeOne();
                regTypeOne2.setRegId("0101");
                regTypeOne2.setField01("field11");
                regTypeOne2.setField02("field12");
                regTypeOne2.setField03("field13");
                regTypeOne2.setTransaction(transaction2);
                regTypeOne2.setId(1L);
                transaction2.setRegTypeOne(regTypeOne2);

                RegTypeTwo regTypeTwo2 = new RegTypeTwo();
                regTypeTwo2.setRegId("0102");
                regTypeTwo2.setField01("field21");
                regTypeTwo2.setField02("field22");
                regTypeTwo2.setField03("field23");
                regTypeTwo2.setTransaction(transaction2);
                regTypeTwo2.setId(1L);
                transaction2.setRegTypeTwo(regTypeTwo2);

                RegTypeThree regTypeThree2 = new RegTypeThree();
                regTypeThree2.setRegId("0103");
                regTypeThree2.setField01("field31");
                regTypeThree2.setField02("field32");
                regTypeThree2.setField03("field33");
                regTypeThree2.setTransaction(transaction2);
                regTypeThree2.setId(1L);
                transaction2.setRegTypeThree(regTypeThree2);

                Transaction transaction3 = new Transaction();
                transaction3.setRegId("0100");
                transaction3.setField01("field01");
                transaction3.setField02("field02");
                transaction3.setField03("field03");
                transaction3.setId(1L);

                RegTypeOne regTypeOne3 = new RegTypeOne();
                regTypeOne3.setRegId("0101");
                regTypeOne3.setField01("field11");
                regTypeOne3.setField02("field12");
                regTypeOne3.setField03("field13");
                regTypeOne3.setTransaction(transaction3);
                regTypeOne3.setId(1L);
                transaction3.setRegTypeOne(regTypeOne3);

                RegTypeTwo regTypeTwo3 = new RegTypeTwo();
                regTypeTwo3.setRegId("0102");
                regTypeTwo3.setField01("field21");
                regTypeTwo3.setField02("field22");
                regTypeTwo3.setField03("field23");
                regTypeTwo3.setTransaction(transaction3);
                regTypeTwo3.setId(1L);
                transaction3.setRegTypeTwo(regTypeTwo3);

                RegTypeThree regTypeThree3 = new RegTypeThree();
                regTypeThree3.setRegId("0103");
                regTypeThree3.setField01("field31");
                regTypeThree3.setField02("field32");
                regTypeThree3.setField03("field33");
                regTypeThree3.setTransaction(transaction3);
                regTypeThree3.setId(1L);
                transaction3.setRegTypeThree(regTypeThree3);

                transactions.add(transaction1);
                transactions.add(transaction2);
                transactions.add(transaction3);

                List<Transaction> savedTransactions = transactionRepository.saveAll(transactions);
                assertThat(savedTransactions).isEqualTo(transactions);
        }

        @Test
        public void testSaveTransaction() {

                Transaction transaction1 = new Transaction();
                transaction1.setRegId("0100");
                transaction1.setField01("field01");
                transaction1.setField02("field02");
                transaction1.setField03("field03");
                transaction1.setId(1L);

                RegTypeOne regTypeOne1 = new RegTypeOne();
                regTypeOne1.setRegId("0101");
                regTypeOne1.setField01("field11");
                regTypeOne1.setField02("field12");
                regTypeOne1.setField03("field13");
                regTypeOne1.setTransaction(transaction1);
                regTypeOne1.setId(1L);
                transaction1.setRegTypeOne(regTypeOne1);

                RegTypeTwo regTypeTwo1 = new RegTypeTwo();
                regTypeTwo1.setRegId("0102");
                regTypeTwo1.setField01("field21");
                regTypeTwo1.setField02("field22");
                regTypeTwo1.setField03("field23");
                regTypeTwo1.setTransaction(transaction1);
                regTypeTwo1.setId(1L);
                transaction1.setRegTypeTwo(regTypeTwo1);

                RegTypeThree regTypeThree1 = new RegTypeThree();
                regTypeThree1.setRegId("0103");
                regTypeThree1.setField01("field31");
                regTypeThree1.setField02("field32");
                regTypeThree1.setField03("field33");
                regTypeThree1.setTransaction(transaction1);
                regTypeThree1.setId(1L);
                transaction1.setRegTypeThree(regTypeThree1);

                transactionRepository.save(transaction1);
                Optional<Transaction> transaction2 = transactionRepository.findById(Long.valueOf(1L));
                assertNotNull(transaction2);
                assertEquals(transaction2.get().getField01(), transaction1.getField01());
                assertEquals(transaction2.get().getField02(), transaction1.getField02());
        }

        @Test
        public void testDeleteTransaction() {

                Transaction transaction1 = new Transaction();
                transaction1.setRegId("0100");
                transaction1.setField01("field01");
                transaction1.setField02("field02");
                transaction1.setField03("field03");
                transaction1.setId(1L);

                RegTypeOne regTypeOne1 = new RegTypeOne();
                regTypeOne1.setRegId("0101");
                regTypeOne1.setField01("field11");
                regTypeOne1.setField02("field12");
                regTypeOne1.setField03("field13");
                regTypeOne1.setTransaction(transaction1);
                regTypeOne1.setId(1L);
                transaction1.setRegTypeOne(regTypeOne1);

                RegTypeTwo regTypeTwo1 = new RegTypeTwo();
                regTypeTwo1.setRegId("0102");
                regTypeTwo1.setField01("field21");
                regTypeTwo1.setField02("field22");
                regTypeTwo1.setField03("field23");
                regTypeTwo1.setTransaction(transaction1);
                regTypeTwo1.setId(1L);
                transaction1.setRegTypeTwo(regTypeTwo1);

                RegTypeThree regTypeThree1 = new RegTypeThree();
                regTypeThree1.setRegId("0103");
                regTypeThree1.setField01("field31");
                regTypeThree1.setField02("field32");
                regTypeThree1.setField03("field33");
                regTypeThree1.setTransaction(transaction1);
                regTypeThree1.setId(1L);
                transaction1.setRegTypeThree(regTypeThree1);

                transactionRepository.save(transaction1);
                transactionRepository.delete(transaction1);
                Optional<Transaction> transaction2 = transactionRepository.findById(Long.valueOf(1L));
                assertThat(transaction2).isEmpty();
        }

        @Test
        public void findAllEmployees() {

                List<Transaction> transactions = new ArrayList<>(3);

                Transaction transaction1 = new Transaction();
                transaction1.setRegId("0100");
                transaction1.setField01("field01");
                transaction1.setField02("field02");
                transaction1.setField03("field03");
                transaction1.setId(1L);

                RegTypeOne regTypeOne1 = new RegTypeOne();
                regTypeOne1.setRegId("0101");
                regTypeOne1.setField01("field11");
                regTypeOne1.setField02("field12");
                regTypeOne1.setField03("field13");
                regTypeOne1.setTransaction(transaction1);
                regTypeOne1.setId(1L);
                transaction1.setRegTypeOne(regTypeOne1);

                RegTypeTwo regTypeTwo1 = new RegTypeTwo();
                regTypeTwo1.setRegId("0102");
                regTypeTwo1.setField01("field21");
                regTypeTwo1.setField02("field22");
                regTypeTwo1.setField03("field23");
                regTypeTwo1.setTransaction(transaction1);
                regTypeTwo1.setId(1L);
                transaction1.setRegTypeTwo(regTypeTwo1);

                RegTypeThree regTypeThree1 = new RegTypeThree();
                regTypeThree1.setRegId("0103");
                regTypeThree1.setField01("field31");
                regTypeThree1.setField02("field32");
                regTypeThree1.setField03("field33");
                regTypeThree1.setTransaction(transaction1);
                regTypeThree1.setId(1L);
                transaction1.setRegTypeThree(regTypeThree1);

                Transaction transaction2 = new Transaction();
                transaction2.setRegId("0100");
                transaction2.setField01("field01");
                transaction2.setField02("field02");
                transaction2.setField03("field03");
                transaction2.setId(2L);

                RegTypeOne regTypeOne2 = new RegTypeOne();
                regTypeOne2.setRegId("0101");
                regTypeOne2.setField01("field11");
                regTypeOne2.setField02("field12");
                regTypeOne2.setField03("field13");
                regTypeOne2.setTransaction(transaction2);
                regTypeOne2.setId(2L);
                transaction2.setRegTypeOne(regTypeOne2);

                RegTypeTwo regTypeTwo2 = new RegTypeTwo();
                regTypeTwo2.setRegId("0102");
                regTypeTwo2.setField01("field21");
                regTypeTwo2.setField02("field22");
                regTypeTwo2.setField03("field23");
                regTypeTwo2.setTransaction(transaction2);
                regTypeTwo2.setId(2L);
                transaction2.setRegTypeTwo(regTypeTwo2);

                RegTypeThree regTypeThree2 = new RegTypeThree();
                regTypeThree2.setRegId("0103");
                regTypeThree2.setField01("field31");
                regTypeThree2.setField02("field32");
                regTypeThree2.setField03("field33");
                regTypeThree2.setTransaction(transaction2);
                regTypeThree2.setId(2L);
                transaction2.setRegTypeThree(regTypeThree2);

                Transaction transaction3 = new Transaction();
                transaction3.setRegId("0100");
                transaction3.setField01("field01");
                transaction3.setField02("field02");
                transaction3.setField03("field03");
                transaction3.setId(3L);

                RegTypeOne regTypeOne3 = new RegTypeOne();
                regTypeOne3.setRegId("0101");
                regTypeOne3.setField01("field11");
                regTypeOne3.setField02("field12");
                regTypeOne3.setField03("field13");
                regTypeOne3.setTransaction(transaction3);
                regTypeOne3.setId(3L);
                transaction3.setRegTypeOne(regTypeOne3);

                RegTypeTwo regTypeTwo3 = new RegTypeTwo();
                regTypeTwo3.setRegId("0102");
                regTypeTwo3.setField01("field21");
                regTypeTwo3.setField02("field22");
                regTypeTwo3.setField03("field23");
                regTypeTwo3.setTransaction(transaction3);
                regTypeTwo3.setId(3L);
                transaction3.setRegTypeTwo(regTypeTwo3);

                RegTypeThree regTypeThree3 = new RegTypeThree();
                regTypeThree3.setRegId("0103");
                regTypeThree3.setField01("field31");
                regTypeThree3.setField02("field32");
                regTypeThree3.setField03("field33");
                regTypeThree3.setTransaction(transaction3);
                regTypeThree3.setId(3L);
                transaction3.setRegTypeThree(regTypeThree3);

                transactions.add(transaction1);
                transactions.add(transaction2);
                transactions.add(transaction3);

                transactionRepository.saveAll(transactions);
                assertNotNull(transactionRepository.findAll());
        }

        @Test
        public void deleteByTransactionIdTest() {
                Transaction transaction1 = new Transaction();
                transaction1.setRegId("0100");
                transaction1.setField01("field01");
                transaction1.setField02("field02");
                transaction1.setField03("field03");
                transaction1.setId(1L);

                RegTypeOne regTypeOne1 = new RegTypeOne();
                regTypeOne1.setRegId("0101");
                regTypeOne1.setField01("field11");
                regTypeOne1.setField02("field12");
                regTypeOne1.setField03("field13");
                regTypeOne1.setTransaction(transaction1);
                regTypeOne1.setId(1L);
                transaction1.setRegTypeOne(regTypeOne1);

                RegTypeTwo regTypeTwo1 = new RegTypeTwo();
                regTypeTwo1.setRegId("0102");
                regTypeTwo1.setField01("field21");
                regTypeTwo1.setField02("field22");
                regTypeTwo1.setField03("field23");
                regTypeTwo1.setTransaction(transaction1);
                regTypeTwo1.setId(1L);
                transaction1.setRegTypeTwo(regTypeTwo1);

                RegTypeThree regTypeThree1 = new RegTypeThree();
                regTypeThree1.setRegId("0103");
                regTypeThree1.setField01("field31");
                regTypeThree1.setField02("field32");
                regTypeThree1.setField03("field33");
                regTypeThree1.setTransaction(transaction1);
                regTypeThree1.setId(1L);
                transaction1.setRegTypeThree(regTypeThree1);

                Transaction transaction2 = transactionRepository.save(transaction1);
                transactionRepository.deleteById(transaction2.getId());

                assertThat(transactionRepository.count()).isEqualTo(Long.valueOf(0L));
        }
}
