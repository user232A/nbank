package iteration2;

import java.util.ArrayList;
import java.util.List;

public class Account {
    private int id;
    private String accountNumber;
    private double balance;
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
}
