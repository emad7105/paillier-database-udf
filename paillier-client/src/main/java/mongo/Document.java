package mongo;

import java.util.Objects;

public class Document {

    public String name;
    public Integer amount;

    public Document() {
    }

    public Document(String name, Integer amount) {
        this.name = name;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return Objects.equals(name, document.name) &&
                Objects.equals(amount, document.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, amount);
    }
}
