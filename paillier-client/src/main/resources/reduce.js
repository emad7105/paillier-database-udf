function (key, values) {
    var total = values[0];
    for (var i = 1; i < values.length; i++) {
        total = he_add(total, values[i]);
    }
    return total;
}