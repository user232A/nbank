package iteration2;

public class Transaction {
    private int id;
    private double amount;
    private String type;
    private String timestamp;
    private int relatedAccountId;

    public int getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getRelatedAccountId() {
        return relatedAccountId;
    }
}
