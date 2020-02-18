package be.heydari.cassandra;

import java.math.BigInteger;

public class HomSumResult {

    public BigInteger sum;

    public HomSumResult(BigInteger sum) {
        this.sum = sum;
    }

    public HomSumResult() {
    }

    public BigInteger getSum() {
        return sum;
    }

    public void setSum(BigInteger sum) {
        this.sum = sum;
    }
}
