/*
const { MongoClient } = require("mongodb")
const uri = "mongodb://localhost:27017";

const client = new MongoClient(uri, {
    useNewUrlParser: true,
    useUnifiedTopology: true
})

async function run() {
    try {
        await client.connect()

        const database = client.db("sample_mflix")
        const movies = database.collection("movies")

        const query = { title: "Back to the future" }
        const movie = await movies.findOne(query)

        console.log("here")
        console.log(movie)
        console.log("now here")
    } finally {
        await client.close()
    }
}
run().catch(console.dir)
*/

var MongoClient = require("mongodb").MongoClient
const url = "mongodb://localhost:27017/";

/* $ mongod
 * > use sample_mflix
 * > db.createCollection("movies")
 * > db.movies.insert({ ... })
 */
MongoClient.connect(url, (err, db) => {
    if (err)
        throw err;
    var dbo = db.db("sample_mflix")
    var myobj = { name: "Company Inc", address: "Highway 37" }
    dbo.collection("movies").insertOne(myobj, (err, res) => {
        if (err)
            throw err;
        console.log("1 document inserted")
        db.close()
    })
})

MongoClient.connect(url, (err, db) => {
    if (err)
        throw err;
    var dbo = db.db("sample_mflix")
    dbo.collection("movies").findOne({}, (err, result) => {
        if (err)
            throw err;
        if (result == null)
            console.log("result is null")
        else
            console.log(result.name)
        db.close()
    })
})
