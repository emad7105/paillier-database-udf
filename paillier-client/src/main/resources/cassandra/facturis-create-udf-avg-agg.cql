CREATE OR REPLACE AGGREGATE facturis.average ( varint, varint)
    SFUNC avgState
    STYPE tuple<int,varint>
    FINALFUNC avgFinal
    INITCOND (1,1);