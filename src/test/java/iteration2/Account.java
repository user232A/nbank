package iteration2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Account {
    private Integer id;
    private String accountNumber;
    private Double balance;
    private List<Transaction> transactions = new ArrayList<>();

    public double getBalance() {
        return balance;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public int getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return id == account.id && Double.compare(balance, account.balance) == 0 &&
                Objects.equals(accountNumber, account.accountNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accountNumber, balance);
    }
}
