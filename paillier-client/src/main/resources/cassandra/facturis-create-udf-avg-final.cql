CREATE OR REPLACE FUNCTION facturis.avgFinal ( state tuple<int,varint> )
    RETURNS NULL ON NULL INPUT
    RETURNS varint
    LANGUAGE java AS
    $$
    java.math.BigInteger r = state.getVarint(1);
    r = r.divide(java.math.BigInteger.valueOf(state.getInt(0)));
    return r;
    $$;