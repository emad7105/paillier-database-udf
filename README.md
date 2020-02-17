# paillier-database-udf
Paillier Additive Homomorphic Encryption implemented as MongoDB and Cassandra user-defined functions (UDFs). [`phe_add.js`](https://github.com/emad7105/paillier-mongodb/blob/master/phe_add.js) implements a function for MongoDB and Cassandra allowing to perform addition over two [Paillier](https://en.wikipedia.org/wiki/Paillier_cryptosystem)-encrypted values homomorphically. We also implemented 'paillier-client' in Java to insert data to MongoDB and Cassandra. Afterwards, using the same client we perform MapReduce queries and use the database functions.

This implementation works against the Paillier implementation in the [Javallier](https://github.com/n1analytics/javallier) repository as well as the [jPaillier](https://github.com/kunerd/jpaillier) repository. That means if those values are encrypted using Java/Scala implementations of Javallier, and stored in MongoDB, by having this JavaScript function in [`phe_add.js`](https://github.com/emad7105/paillier-mongodb/blob/master/phe_add.js) added to MongoDB, one can eventually perform aggregate queries, in particular those with additions, and perform the required additive homomorphic addition inside the database and close to the actual data.


## How to deploy the function in MongoDB?
MongoDB does not support User-Defined Functions (UDF) as most databases. "MongoDB does not support user defined functions (UDFs) out-of-the-box. But it allows creating and saving JavaScript functions using the db.system.js.save command. The JavaScript functions thus created can then be reused in the MapReduce functions. [[1]](https://www.infoq.com/articles/implementing-aggregation-functions-in-mongodb/)".  

In this repository, we introduce two ways of deploying the function: (1) through MongoDB cli, and (2) through Java.

### Deploying through MongoDB cli
You need to firstly enable authentication in your MongoDB. To do this, you need to create a user with admin priviliges in the Admin database. Please follow the documentations of MongoDB. When you are done, connect to MongoDB as follows:  

```
mongo --host <IP>:27017 -u <admin-user> -p --authenticationDatabase admin phe_add.js
# when you are logged in, run:
loadServerScripts()
```

### Deploying through Java
This approach is interesting for when you would like to integrate the process in the life cycle of you Java application. Unfortunately, that means you Java application needs to have privilieged access rights in MongoDB. 

This function can be used in aggregate queries such as Map/Reduce in MongoDB.
