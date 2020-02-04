# paillier-mongodb
Paillier Additive Homomorphic Encryption implemented as MongoDB functions. [`phe_add.js`](https://github.com/emad7105/paillier-mongodb/blob/master/phe_add.js) implements a function for MongoDB allowing to perform addition over two [Paillier](https://en.wikipedia.org/wiki/Paillier_cryptosystem)-encrypted values homomorphically.

This implementation works against the Paillier implementation in the [Javallier](https://github.com/n1analytics/javallier) repository. That means if those values are encrypted using Java/Scala implementations of Javallier, and stored in MongoDB, by having this JavaScript function in [`phe_add.js`](https://github.com/emad7105/paillier-mongodb/blob/master/phe_add.js) added to MongoDB, one can eventually perform aggregate queries, in particular those with additions, and perform the required additive homomorphic addition inside the database and close to the actual data.


This function can be used in aggregate queries such as Map/Reduce in MongoDB.
