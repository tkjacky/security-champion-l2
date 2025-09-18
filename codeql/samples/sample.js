const express = require('express');
const app = express();

// Parse URL-encoded data
app.use(express.urlencoded({ extended: true }));

// VULNERABLE: Simple XSS in search endpoint
app.get('/search', (req, res) => {
    const query = req.query.q;
    // TODO: Students should identify this XSS vulnerability
    res.send(`<h1>Search Results for: ${query}</h1>`);
});

// VULNERABLE: XSS in user profile
app.get('/user/:name', (req, res) => {
    const name = req.params.name;
    // TODO: Students should identify this XSS vulnerability  
    res.send(`<h1>Welcome ${name}!</h1>`);
});

// SAFE: Proper escaping example
app.get('/safe', (req, res) => {
    const query = req.query.q || '';
    const escaped = query.replace(/</g, '&lt;').replace(/>/g, '&gt;');
    res.send(`<h1>Safe search: ${escaped}</h1>`);
});

app.listen(3000, () => {
    console.log('App running on http://localhost:3000');
    console.log('Try: /search?q=<script>alert("XSS")</script>');
    console.log('Try: /user/<img src=x onerror=alert("XSS")>');
});

module.exports = app;
