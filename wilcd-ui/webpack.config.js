var path = require('path');

module.exports = {
    entry: "./main.js",
    output: {
        filename: "main.packed.js",
        path: path.resolve(__dirname, 'target/web/webpack')
    },
    context: path.resolve(__dirname, "app/assets")
};
