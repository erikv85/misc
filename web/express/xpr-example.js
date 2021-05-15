// snippets from expressjs.com

// to run: node <this file>
const express = require("express")
const bodyParser = require("body-parser")

const app = express()
/* With this we can serve files. Uncomment line below and try
 * curl localhost:3000/<file>
 */
//app.use(express.static('.'))

// Needed to read json body:
app.use(bodyParser.json())

const port = 3000

// curl localhost:3000
app.get('/', (req, res) => {
    res.send("Hello world!\n")
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
