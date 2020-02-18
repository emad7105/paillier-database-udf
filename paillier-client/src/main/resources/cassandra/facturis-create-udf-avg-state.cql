CREATE OR REPLACE FUNCTION facturis.avgState ( state tuple<int,varint>, invoice_amount varint, nsquared varint)
    RETURNS NULL ON NULL INPUT
    RETURNS tuple<int,varint>
    LANGUAGE java AS
    $$
    state.setInt(0, state.getInt(0)+1);
    state.setVarint(1, state.getVarint(1).multiply(invoice_amount).mod(nsquared));
    return state;
    $$;