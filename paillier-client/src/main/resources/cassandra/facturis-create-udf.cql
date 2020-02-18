CREATE FUNCTION facturis.hom_add(input varint)
    RETURNS NULL ON NULL INPUT
    RETURNS varint
    LANGUAGE java
    AS $$return input.multiply(java.math.BigInteger.valueOf(10));$$;





CREATE OR REPLACE FUNCTION avgState ( state tuple<int,varint>, invoice_amount varint, nsquared varint)
    RETURNS NULL ON NULL INPUT
    RETURNS tuple<int,varint>
    LANGUAGE java AS
    $$
    state.setInt(0, state.getInt(0)+1);
    state.setVarint(1, state.getVarint(1).multiply(invoice_amount).mod(nsquared));
    return state;
    $$;


CREATE OR REPLACE FUNCTION avgFinal ( state tuple<int,varint> )
    RETURNS NULL ON NULL INPUT
    RETURNS varint
    LANGUAGE java AS
    $$
    java.math.BigInteger r = state.getVarint(1);
    r = r.divide(java.math.BigInteger.valueOf(state.getInt(0)));
    return r;
    $$;


CREATE OR REPLACE AGGREGATE average ( varint, varint)
    SFUNC avgState
    STYPE tuple<int,varint>
    FINALFUNC avgFinal
    INITCOND (1,1);