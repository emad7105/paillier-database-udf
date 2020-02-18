CREATE OR REPLACE AGGREGATE facturis.homsum ( varint, varint)
    SFUNC homsumState
    STYPE tuple<int,varint>
    FINALFUNC homsumFinal
    INITCOND (1,1);