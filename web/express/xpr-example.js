// snippets from expressjs.com

// to run: node <this file>
const express = require("express")
const bodyParser = require("body-parser")

const { MongoClient } = require("mongodb")

const app = express()
/* With this we can serve files. Uncomment line below and try
 * curl localhost:3000/<file>
 */
//app.use(express.static('.'))

// Needed to read json body:
app.use(bodyParser.json())

const port = 3000

const client = new MongoClient("mongodb://localhost:27017", {
    useNewUrlParser: true,
    useUnifiedTopology: true
})

// curl localhost:3000
app.get('/', (req, res) => {
    client.connect()
    const database = client.db("sample_mflix")
    const movies = database.collection("movies")
    const query = { title: "Back to the future" }
    const movie = movies.findOne(query) // :: Promise
    movie.then(val => res.send(val))
})

// curl localhost:3000/wut
app.get("/wut", (req, res) => {
    res.send("hehe\n")
})

app.post("/", (req, res) => {
    console.log(req.body)
    res.send("got your post\n")
})

app.listen(port, () => {
    console.log("Listening")
})
