CREATE FUNCTION facturis.my_sin3(input varint)
    RETURNS NULL ON NULL INPUT
    RETURNS varint
    LANGUAGE java
    AS $$return input.multiply(java.math.BigInteger.valueOf(10));$$;