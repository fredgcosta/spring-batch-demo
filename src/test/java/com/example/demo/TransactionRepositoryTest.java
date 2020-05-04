package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

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
                transaction1.setField01("field01");
                transaction1.setField02("field02");
                transaction1.setField03("field03");
                transaction1.setField04("field04");
                transaction1.setField05("field05");
                transaction1.setId(1L);

                RegTypeOne regTypeOne1 = new RegTypeOne();
                regTypeOne1.setField11("field11");
                regTypeOne1.setField12("field12");
                regTypeOne1.setField13("field13");
                regTypeOne1.setField14("field14");
                regTypeOne1.setField15("field15");
                regTypeOne1.setTransaction(transaction1);
                regTypeOne1.setId(transaction1.getId());
                transaction1.setRegTypeOne(regTypeOne1);

                RegTypeTwo regTypeTwo1 = new RegTypeTwo();
                regTypeTwo1.setField21("field21");
                regTypeTwo1.setField22("field22");
                regTypeTwo1.setField23("field23");
                regTypeTwo1.setField24("field24");
                regTypeTwo1.setField25("field25");
                regTypeTwo1.setTransaction(transaction1);
                regTypeTwo1.setId(transaction1.getId());
                transaction1.setRegTypeTwo(regTypeTwo1);

                RegTypeThree regTypeThree1 = new RegTypeThree();
                regTypeThree1.setField31("field31");
                regTypeThree1.setField32("field32");
                regTypeThree1.setField33("field33");
                regTypeThree1.setField34("field34");
                regTypeThree1.setField35("field35");
                regTypeThree1.setTransaction(transaction1);
                regTypeThree1.setId(transaction1.getId());
                transaction1.setRegTypeThree(regTypeThree1);

                Transaction transaction2 = new Transaction();
                transaction2.setField01("field01");
                transaction2.setField02("field02");
                transaction2.setField03("field03");
                transaction2.setField04("field04");
                transaction2.setField05("field05");
                transaction2.setId(2L);

                RegTypeOne regTypeOne2 = new RegTypeOne();
                regTypeOne2.setField11("field11");
                regTypeOne2.setField12("field12");
                regTypeOne2.setField13("field13");
                regTypeOne2.setField14("field14");
                regTypeOne2.setField15("field15");
                regTypeOne2.setTransaction(transaction2);
                regTypeOne2.setId(transaction2.getId());
                transaction2.setRegTypeOne(regTypeOne2);

                RegTypeTwo regTypeTwo2 = new RegTypeTwo();
                regTypeTwo2.setField21("field21");
                regTypeTwo2.setField22("field22");
                regTypeTwo2.setField23("field23");
                regTypeTwo2.setField24("field24");
                regTypeTwo2.setField25("field25");
                regTypeTwo2.setTransaction(transaction2);
                regTypeTwo2.setId(transaction2.getId());
                transaction2.setRegTypeTwo(regTypeTwo2);

                RegTypeThree regTypeThree2 = new RegTypeThree();
                regTypeThree2.setField31("field31");
                regTypeThree2.setField32("field32");
                regTypeThree2.setField33("field33");
                regTypeThree2.setField34("field34");
                regTypeThree2.setField35("field35");
                regTypeThree2.setTransaction(transaction2);
                regTypeThree2.setId(transaction2.getId());
                transaction2.setRegTypeThree(regTypeThree2);

                Transaction transaction3 = new Transaction();
                transaction3.setField01("field01");
                transaction3.setField02("field02");
                transaction3.setField03("field03");
                transaction3.setField04("field04");
                transaction3.setField05("field05");
                transaction3.setId(3L);

                RegTypeOne regTypeOne3 = new RegTypeOne();
                regTypeOne3.setField11("field11");
                regTypeOne3.setField12("field12");
                regTypeOne3.setField13("field13");
                regTypeOne3.setField14("field14");
                regTypeOne3.setField15("field15");
                regTypeOne3.setTransaction(transaction3);
                regTypeOne3.setId(transaction3.getId());
                transaction3.setRegTypeOne(regTypeOne3);

                RegTypeTwo regTypeTwo3 = new RegTypeTwo();
                regTypeTwo3.setField21("field21");
                regTypeTwo3.setField22("field22");
                regTypeTwo3.setField23("field23");
                regTypeTwo3.setField24("field24");
                regTypeTwo3.setField25("field25");
                regTypeTwo3.setTransaction(transaction3);
                regTypeTwo3.setId(transaction3.getId());
                transaction3.setRegTypeTwo(regTypeTwo3);

                RegTypeThree regTypeThree3 = new RegTypeThree();
                regTypeThree3.setField31("field31");
                regTypeThree3.setField32("field32");
                regTypeThree3.setField33("field33");
                regTypeThree3.setField34("field34");
                regTypeThree3.setField35("field35");
                regTypeThree3.setTransaction(transaction3);
                regTypeThree3.setId(transaction3.getId());
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
                transaction1.setField01("field01");
                transaction1.setField02("field02");
                transaction1.setField03("field03");
                transaction1.setField04("field04");
                transaction1.setField05("field05");
                transaction1.setId(1L);

                RegTypeOne regTypeOne1 = new RegTypeOne();
                regTypeOne1.setField11("field11");
                regTypeOne1.setField12("field12");
                regTypeOne1.setField13("field13");
                regTypeOne1.setField14("field14");
                regTypeOne1.setField15("field15");
                regTypeOne1.setTransaction(transaction1);
                regTypeOne1.setId(1L);
                transaction1.setRegTypeOne(regTypeOne1);

                RegTypeTwo regTypeTwo1 = new RegTypeTwo();
                regTypeTwo1.setField21("field21");
                regTypeTwo1.setField22("field22");
                regTypeTwo1.setField23("field23");
                regTypeTwo1.setField24("field24");
                regTypeTwo1.setField25("field25");
                regTypeTwo1.setTransaction(transaction1);
                regTypeTwo1.setId(1L);
                transaction1.setRegTypeTwo(regTypeTwo1);

                RegTypeThree regTypeThree1 = new RegTypeThree();
                regTypeThree1.setField31("field31");
                regTypeThree1.setField32("field32");
                regTypeThree1.setField33("field33");
                regTypeThree1.setField34("field34");
                regTypeThree1.setField35("field35");
                regTypeThree1.setTransaction(transaction1);
                regTypeThree1.setId(1L);
                transaction1.setRegTypeThree(regTypeThree1);

                Transaction transaction2 = new Transaction();
                transaction2.setField01("field01");
                transaction2.setField02("field02");
                transaction2.setField03("field03");
                transaction2.setField04("field04");
                transaction2.setField05("field05");
                transaction2.setId(1L);

                RegTypeOne regTypeOne2 = new RegTypeOne();
                regTypeOne2.setField11("field11");
                regTypeOne2.setField12("field12");
                regTypeOne2.setField13("field13");
                regTypeOne2.setField14("field14");
                regTypeOne2.setField15("field15");
                regTypeOne2.setTransaction(transaction2);
                regTypeOne2.setId(1L);
                transaction2.setRegTypeOne(regTypeOne2);

                RegTypeTwo regTypeTwo2 = new RegTypeTwo();
                regTypeTwo2.setField21("field21");
                regTypeTwo2.setField22("field22");
                regTypeTwo2.setField23("field23");
                regTypeTwo2.setField24("field24");
                regTypeTwo2.setField25("field25");
                regTypeTwo2.setTransaction(transaction2);
                regTypeTwo2.setId(1L);
                transaction2.setRegTypeTwo(regTypeTwo2);

                RegTypeThree regTypeThree2 = new RegTypeThree();
                regTypeThree2.setField31("field31");
                regTypeThree2.setField32("field32");
                regTypeThree2.setField33("field33");
                regTypeThree2.setField34("field34");
                regTypeThree2.setField35("field35");
                regTypeThree2.setTransaction(transaction2);
                regTypeThree2.setId(1L);
                transaction2.setRegTypeThree(regTypeThree2);

                Transaction transaction3 = new Transaction();
                transaction3.setField01("field01");
                transaction3.setField02("field02");
                transaction3.setField03("field03");
                transaction3.setField04("field04");
                transaction3.setField05("field05");
                transaction3.setId(1L);

                RegTypeOne regTypeOne3 = new RegTypeOne();
                regTypeOne3.setField11("field11");
                regTypeOne3.setField12("field12");
                regTypeOne3.setField13("field13");
                regTypeOne3.setField14("field14");
                regTypeOne3.setField15("field15");
                regTypeOne3.setTransaction(transaction3);
                regTypeOne3.setId(1L);
                transaction3.setRegTypeOne(regTypeOne3);

                RegTypeTwo regTypeTwo3 = new RegTypeTwo();
                regTypeTwo3.setField21("field21");
                regTypeTwo3.setField22("field22");
                regTypeTwo3.setField23("field23");
                regTypeTwo3.setField24("field24");
                regTypeTwo3.setField25("field25");
                regTypeTwo3.setTransaction(transaction3);
                regTypeTwo3.setId(1L);
                transaction3.setRegTypeTwo(regTypeTwo3);

                RegTypeThree regTypeThree3 = new RegTypeThree();
                regTypeThree3.setField31("field31");
                regTypeThree3.setField32("field32");
                regTypeThree3.setField33("field33");
                regTypeThree3.setField34("field34");
                regTypeThree3.setField35("field35");
                regTypeThree3.setTransaction(transaction3);
                regTypeThree3.setId(1L);
                transaction3.setRegTypeThree(regTypeThree3);

                transactions.add(transaction1);
                transactions.add(transaction2);
                transactions.add(transaction3);

                List<Transaction> savedTransactions = transactionRepository.saveAll(transactions);
                assertThat(savedTransactions).isEqualTo(transactions);
        }

        // @Test
        // public void testSaveEmployee() {

        //     Transaction transaction = new Transaction("admin", "admin", "admin@gmail.com");
        //     transactionRepository.save(transaction);
        //     Transaction employee2 = transactionRepository.findByFirstName("admin");
        //     assertNotNull(transaction);
        //     assertEquals(employee2.getFirstName(), transaction.getFirstName());
        //     assertEquals(employee2.getLastName(), transaction.getLastName());
        // }

        // @Test
        // public void testGetEmployee() {

        //     Transaction transaction = new Transaction("admin", "admin", "admin@gmail.com");
        //     transactionRepository.save(transaction);
        //     Transaction employee2 = transactionRepository.findByFirstName("admin");
        //     assertNotNull(transaction);
        //     assertEquals(employee2.getFirstName(), transaction.getFirstName());
        //     assertEquals(employee2.getLastName(), transaction.getLastName());
        // }

        // @Test
        // public void testDeleteEmployee() {
        //     Transaction transaction = new Transaction("admin", "admin", "admin@gmail.com");
        //     transactionRepository.save(transaction);
        //     transactionRepository.delete(transaction);
        // }

        // @Test
        // public void findAllEmployees() {
        //     Transaction transaction = new Transaction("admin", "admin", "admin@gmail.com");
        //     transactionRepository.save(transaction);
        //     assertNotNull(transactionRepository.findAll());
        // }

        // @Test
        // public void deletByEmployeeIdTest() {
        //     Transaction transaction = new Transaction("admin", "admin", "admin@gmail.com");
        //     Transaction emp = transactionRepository.save(transaction);
        //     transactionRepository.deleteById(emp.getId());
        // }
}
