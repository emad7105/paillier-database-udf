# paillier-mongodb
Paillier Additive Homomorphic Encryption implemented as MongoDB functions. [`phe_add.js`](https://github.com/emad7105/paillier-mongodb/blob/master/phe_add.js) implements a function for MongoDB allowing to perform addition over two [Paillier](https://en.wikipedia.org/wiki/Paillier_cryptosystem)-encrypted values homomorphically.

This implementation works against the Paillier implementation in the [Javallier](https://github.com/n1analytics/javallier) repository. That means if those values are encrypted using Java/Scala implementations of Javallier, and stored in MongoDB, by having this JavaScript function in [`phe_add.js`](https://github.com/emad7105/paillier-mongodb/blob/master/phe_add.js) added to MongoDB, one can eventually perform aggregate queries, in particular those with additions, and perform the required additive homomorphic addition inside the database and close to the actual data.


## How to deploy the function in MongoDB?
MongoDB does not support User-Defined Functions (UDF) as most databases. "MongoDB does not support user defined functions (UDFs) out-of-the-box. But it allows creating and saving JavaScript functions using the db.system.js.save command. The JavaScript functions thus created can then be reused in the MapReduce functions. [[1]](https://www.infoq.com/articles/implementing-aggregation-functions-in-mongodb/)"

This function can be used in aggregate queries such as Map/Reduce in MongoDB.
