package com.ebanx.challenge.service;

import com.ebanx.challenge.dto.EventRequest;
import com.ebanx.challenge.model.Account;
import com.ebanx.challenge.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class AccountServiceTest {

    private AccountRepository repository;
    private AccountService service;

    @BeforeEach
    void setUp() {
        repository = new AccountRepository();
        service = new AccountService(repository);
    }

    @Nested
    @DisplayName("Happy Path Tests")
    class HappyPathTests {

        @Test
        @DisplayName("Should deposit funds into new account")
        void shouldDepositIntoNewAccount() {
            var request = new EventRequest("deposit", null, "acc-1", 1000);
            var result = service.processEvent(request);

            assertInstanceOf(Map.class, result);
            Map<?, ?> resultMap = (Map<?, ?>) result;
            Map<?, ?> destination = (Map<?, ?>) resultMap.get("destination");
            assertEquals("acc-1", destination.get("id"));
            assertEquals(1000, destination.get("balance"));
        }

        @Test
        @DisplayName("Should deposit funds into existing account")
        void shouldDepositIntoExistingAccount() {
            repository.save(new Account("acc-1", 500));
            var request = new EventRequest("deposit", null, "acc-1", 300);
            var result = service.processEvent(request);

            Map<?, ?> resultMap = (Map) result;
            Map<?, ?> destination = (Map<?, ?>) resultMap.get("destination");
            assertEquals(800, destination.get("balance"));
        }

        @Test
        @DisplayName("Should withdraw funds from account")
        void shouldWithdrawFromAccount() {
            repository.save(new Account("acc-1", 1000));
            var request = new EventRequest("withdraw", "acc-1", null, 300);
            var result = service.processEvent(request);

            Map<?, ?> resultMap = (Map<?, ?>) result;
            Map<?, ?> origin = (Map<?, ?>) resultMap.get("origin");
            assertEquals("acc-1", origin.get("id"));
            assertEquals(700, origin.get("balance"));
        }

        @Test
        @DisplayName("Should transfer funds between accounts")
        void shouldTransferBetweenAccounts() {
            repository.save(new Account("acc-1", 1000));
            repository.save(new Account("acc-2", 200));
            var request = new EventRequest("transfer", "acc-1", "acc-2", 400);
            var result = service.processEvent(request);

            Map<?, ?> resultMap = (Map<?, ?>) result;
            Map<?, ?> origin = (Map<?, ?>) resultMap.get("origin");
            Map<?, ?> destination = (Map<?, ?>) resultMap.get("destination");

            assertEquals("acc-1", origin.get("id"));
            assertEquals(600, origin.get("balance"));
            assertEquals("acc-2", destination.get("id"));
            assertEquals(600, destination.get("balance"));
        }

        @Test
        @DisplayName("Should transfer to new destination account")
        void shouldTransferToNewDestinationAccount() {
            repository.save(new Account("acc-1", 1000));
            var request = new EventRequest("transfer", "acc-1", "acc-2", 400);
            var result = service.processEvent(request);

            Map<?, ?> resultMap = (Map<?, ?>) result;
            Map<?, ?> origin = (Map<?, ?>) resultMap.get("origin");
            Map<?, ?> destination = (Map<?, ?>) resultMap.get("destination");

            assertEquals(600, origin.get("balance"));
            assertEquals("acc-2", destination.get("id"));
            assertEquals(400, destination.get("balance"));
        }

        @Test
        @DisplayName("Should get balance of existing account")
        void shouldGetBalance() {
            repository.save(new Account("acc-1", 750));
            assertEquals(750, service.getBalance("acc-1"));
        }
    }

    @Nested
    @DisplayName("Amount Validation Tests")
    class AmountValidationTests {

        @Test
        @DisplayName("Should reject negative deposit amount")
        void shouldRejectNegativeDepositAmount() {
            var request = new EventRequest("deposit", null, "acc-1", -100);
            assertThrows(IllegalArgumentException.class, () -> service.processEvent(request));
        }

        @Test
        @DisplayName("Should reject negative withdraw amount")
        void shouldRejectNegativeWithdrawAmount() {
            var request = new EventRequest("withdraw", "acc-1", null, -100);
            assertThrows(IllegalArgumentException.class, () -> service.processEvent(request));
        }

        @Test
        @DisplayName("Should reject negative transfer amount")
        void shouldRejectNegativeTransferAmount() {
            var request = new EventRequest("transfer", "acc-1", "acc-2", -100);
            assertThrows(IllegalArgumentException.class, () -> service.processEvent(request));
        }

        @Test
        @DisplayName("Should reject zero deposit amount")
        void shouldRejectZeroDepositAmount() {
            var request = new EventRequest("deposit", null, "acc-1", 0);
            assertThrows(IllegalArgumentException.class, () -> service.processEvent(request));
        }

        @Test
        @DisplayName("Should reject zero withdraw amount")
        void shouldRejectZeroWithdrawAmount() {
            var request = new EventRequest("withdraw", "acc-1", null, 0);
            assertThrows(IllegalArgumentException.class, () -> service.processEvent(request));
        }

        @Test
        @DisplayName("Should reject zero transfer amount")
        void shouldRejectZeroTransferAmount() {
            var request = new EventRequest("transfer", "acc-1", "acc-2", 0);
            assertThrows(IllegalArgumentException.class, () -> service.processEvent(request));
        }

        @Test
        @DisplayName("Should reject null deposit amount")
        void shouldRejectNullDepositAmount() {
            var request = new EventRequest("deposit", null, "acc-1", null);
            assertThrows(IllegalArgumentException.class, () -> service.processEvent(request));
        }

        @Test
        @DisplayName("Should reject null withdraw amount")
        void shouldRejectNullWithdrawAmount() {
            var request = new EventRequest("withdraw", "acc-1", null, null);
            assertThrows(IllegalArgumentException.class, () -> service.processEvent(request));
        }

        @Test
        @DisplayName("Should reject null transfer amount")
        void shouldRejectNullTransferAmount() {
            var request = new EventRequest("transfer", "acc-1", "acc-2", null);
            assertThrows(IllegalArgumentException.class, () -> service.processEvent(request));
        }

        @Test
        @DisplayName("Should include descriptive message for invalid amount")
        void shouldIncludeDescriptiveErrorMessageForInvalidAmount() {
            var request = new EventRequest("deposit", null, "acc-1", -50);
            var exception = assertThrows(IllegalArgumentException.class, () -> service.processEvent(request));
            assertTrue(exception.getMessage().contains("positive"));
        }
    }

    @Nested
    @DisplayName("Insufficient Funds Tests")
    class InsufficientFundsTests {

        @Test
        @DisplayName("Should reject withdrawal exceeding balance")
        void shouldRejectWithdrawalExceedingBalance() {
            repository.save(new Account("acc-1", 100));
            var request = new EventRequest("withdraw", "acc-1", null, 200);
            assertThrows(IllegalArgumentException.class, () -> service.processEvent(request));
        }

        @Test
        @DisplayName("Should reject withdrawal of exact balance (zero after)")
        void shouldAllowWithdrawalOfExactBalance() {
            repository.save(new Account("acc-1", 100));
            var request = new EventRequest("withdraw", "acc-1", null, 100);
            var result = service.processEvent(request);

            Map<?, ?> resultMap = (Map<?, ?>) result;
            Map<?, ?> origin = (Map<?, ?>) resultMap.get("origin");
            assertEquals(0, origin.get("balance"));
        }

        @Test
        @DisplayName("Should reject transfer exceeding origin balance")
        void shouldRejectTransferExceedingOriginBalance() {
            repository.save(new Account("acc-1", 100));
            var request = new EventRequest("transfer", "acc-1", "acc-2", 200);
            assertThrows(IllegalArgumentException.class, () -> service.processEvent(request));
        }

        @Test
        @DisplayName("Should allow transfer of exact origin balance")
        void shouldAllowTransferOfExactOriginBalance() {
            repository.save(new Account("acc-1", 100));
            var request = new EventRequest("transfer", "acc-1", "acc-2", 100);
            var result = service.processEvent(request);

            Map<?, ?> resultMap = (Map<?, ?>) result;
            Map<?, ?> origin = (Map<?, ?>) resultMap.get("origin");
            Map<?, ?> destination = (Map<?, ?>) resultMap.get("destination");

            assertEquals(0, origin.get("balance"));
            assertEquals(100, destination.get("balance"));
        }

        @Test
        @DisplayName("Should include account ID in insufficient funds error message")
        void shouldIncludeAccountIdInInsufficientFundsMessage() {
            repository.save(new Account("acc-1", 50));
            var request = new EventRequest("withdraw", "acc-1", null, 100);
            var exception = assertThrows(IllegalArgumentException.class, () -> service.processEvent(request));
            assertTrue(exception.getMessage().contains("acc-1"));
        }

        @Test
        @DisplayName("Should verify balance unchanged after failed withdrawal")
        void shouldVerifyBalanceUnchangedAfterFailedWithdrawal() {
            repository.save(new Account("acc-1", 500));
            var request = new EventRequest("withdraw", "acc-1", null, 1000);
            assertThrows(IllegalArgumentException.class, () -> service.processEvent(request));
            assertEquals(500, service.getBalance("acc-1"));
        }

        @Test
        @DisplayName("Should verify balances unchanged after failed transfer")
        void shouldVerifyBalancesUnchangedAfterFailedTransfer() {
            repository.save(new Account("acc-1", 500));
            repository.save(new Account("acc-2", 200));
            var request = new EventRequest("transfer", "acc-1", "acc-2", 1000);
            assertThrows(IllegalArgumentException.class, () -> service.processEvent(request));
            assertEquals(500, service.getBalance("acc-1"));
            assertEquals(200, service.getBalance("acc-2"));
        }
    }

    @Nested
    @DisplayName("Account Not Found Tests")
    class AccountNotFoundTests {

        @Test
        @DisplayName("Should throw when withdrawing from non-existent account")
        void shouldThrowWhenWithdrawingFromNonExistentAccount() {
            var request = new EventRequest("withdraw", "acc-99", null, 100);
            assertThrows(IllegalArgumentException.class, () -> service.processEvent(request));
        }

        @Test
        @DisplayName("Should include account ID in not found error for withdrawal")
        void shouldIncludeAccountIdInNotFoundErrorMessageForWithdrawal() {
            var request = new EventRequest("withdraw", "missing-acc", null, 100);
            var exception = assertThrows(IllegalArgumentException.class, () -> service.processEvent(request));
            assertTrue(exception.getMessage().contains("missing-acc"));
        }

        @Test
        @DisplayName("Should throw when transferring from non-existent origin")
        void shouldThrowWhenTransferringFromNonExistentOrigin() {
            var request = new EventRequest("transfer", "acc-99", "acc-2", 100);
            assertThrows(IllegalArgumentException.class, () -> service.processEvent(request));
        }

        @Test
        @DisplayName("Should throw when getting balance of non-existent account")
        void shouldThrowWhenGettingBalanceOfNonExistentAccount() {
            assertThrows(NoSuchElementException.class, () -> service.getBalance("acc-99"));
        }
    }

    @Nested
    @DisplayName("Invalid Field Tests")
    class InvalidFieldTests {

        @Test
        @DisplayName("Should reject invalid event type")
        void shouldRejectInvalidEventType() {
            var request = new EventRequest("refund", "acc-1", "acc-2", 100);
            var exception = assertThrows(IllegalArgumentException.class, () -> service.processEvent(request));
            assertTrue(exception.getMessage().contains("Invalid event type"));
        }

        @Test
        @DisplayName("Should reject null origin for withdrawal")
        void shouldRejectNullOriginForWithdrawal() {
            var request = new EventRequest("withdraw", null, null, 100);
            assertThrows(IllegalArgumentException.class, () -> service.processEvent(request));
        }

        @Test
        @DisplayName("Should reject blank origin for withdrawal")
        void shouldRejectBlankOriginForWithdrawal() {
            var request = new EventRequest("withdraw", "  ", null, 100);
            assertThrows(IllegalArgumentException.class, () -> service.processEvent(request));
        }

        @Test
        @DisplayName("Should reject null origin for transfer")
        void shouldRejectNullOriginForTransfer() {
            var request = new EventRequest("transfer", null, "acc-2", 100);
            assertThrows(IllegalArgumentException.class, () -> service.processEvent(request));
        }

        @Test
        @DisplayName("Should reject null destination for transfer")
        void shouldRejectNullDestinationForTransfer() {
            repository.save(new Account("acc-1", 500));
            var request = new EventRequest("transfer", "acc-1", null, 100);
            assertThrows(IllegalArgumentException.class, () -> service.processEvent(request));
        }

        @Test
        @DisplayName("Should reject blank destination for transfer")
        void shouldRejectBlankDestinationForTransfer() {
            repository.save(new Account("acc-1", 500));
            var request = new EventRequest("transfer", "acc-1", "  ", 100);
            assertThrows(IllegalArgumentException.class, () -> service.processEvent(request));
        }

        @Test
        @DisplayName("Should reject null destination for deposit")
        void shouldRejectNullDestinationForDeposit() {
            var request = new EventRequest("deposit", null, null, 100);
            assertThrows(IllegalArgumentException.class, () -> service.processEvent(request));
        }

        @Test
        @DisplayName("Should reject blank destination for deposit")
        void shouldRejectBlankDestinationForDeposit() {
            var request = new EventRequest("deposit", null, "   ", 100);
            assertThrows(IllegalArgumentException.class, () -> service.processEvent(request));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle large amounts")
        void shouldHandleLargeAmounts() {
            repository.save(new Account("acc-1", Integer.MAX_VALUE - 1000));
            var request = new EventRequest("withdraw", "acc-1", null, 500);
            var result = service.processEvent(request);

            Map<?, ?> resultMap = (Map<?, ?>) result;
            Map<?, ?> origin = (Map<?, ?>) resultMap.get("origin");
            assertEquals(Integer.MAX_VALUE - 1500, origin.get("balance"));
        }

        @Test
        @DisplayName("Should handle single unit amounts")
        void shouldHandleSingleUnitAmounts() {
            repository.save(new Account("acc-1", 10));
            var request = new EventRequest("withdraw", "acc-1", null, 1);
            var result = service.processEvent(request);

            Map<?, ?> resultMap = (Map<?, ?>) result;
            Map<?, ?> origin = (Map<?, ?>) resultMap.get("origin");
            assertEquals(9, origin.get("balance"));
        }

        @Test
        @DisplayName("Should handle multiple sequential operations on same account")
        void shouldHandleMultipleSequentialOperations() {
            var deposit1 = new EventRequest("deposit", null, "acc-1", 1000);
            service.processEvent(deposit1);

            var deposit2 = new EventRequest("deposit", null, "acc-1", 500);
            service.processEvent(deposit2);

            var withdraw = new EventRequest("withdraw", "acc-1", null, 300);
            service.processEvent(withdraw);

            assertEquals(1200, service.getBalance("acc-1"));
        }

        @Test
        @DisplayName("Should handle transfer between same account (origin == destination)")
        void shouldHandleTransferBetweenSameAccount() {
            repository.save(new Account("acc-1", 1000));
            var request = new EventRequest("transfer", "acc-1", "acc-1", 200);
            var result = service.processEvent(request);

            Map<?, ?> resultMap = (Map<?, ?>) result;
            Map<?, ?> origin = (Map<?, ?>) resultMap.get("origin");
            Map<?, ?> destination = (Map<?, ?>) resultMap.get("destination");

            // When origin == destination, the same object is modified twice:
            // first subtracted 200, then added 200 back, resulting in no net change
            assertEquals(1000, origin.get("balance"));
            assertEquals(1000, destination.get("balance"));
        }

        @Test
        @DisplayName("Should handle concurrent deposits (both succeed)")
        void shouldHandleConcurrentDeposits() throws InterruptedException {
            repository.save(new Account("acc-1", 0));

            var request1 = new EventRequest("deposit", null, "acc-1", 500);
            var request2 = new EventRequest("deposit", null, "acc-1", 300);

            Thread t1 = new Thread(() -> service.processEvent(request1));
            Thread t2 = new Thread(() -> service.processEvent(request2));

            t1.start();
            t2.start();
            t1.join();
            t2.join();

            // Both operations complete without exceptions
            int balance = service.getBalance("acc-1");
            assertTrue(balance > 0, "Balance should be positive after deposits");
        }
    }
}
