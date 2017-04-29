var path = require('path');
var webpack = require('webpack');
var ExtractTextPlugin = require('extract-text-webpack-plugin');

module.exports = {
    entry: "./main.js",
    output: {
        filename: "main.packed.js",
        path: path.resolve(__dirname, 'target/web/webpack')
    },
    devtool: "source-map",
    context: path.resolve(__dirname, "app/assets"),
    module: {
        rules: [
            {
                test: /\.(le|c)ss$/,
                use: ExtractTextPlugin.extract({
                    use: ['css-loader', "less-loader"]
                })
            },
            {
                test: /\.svg$/,
                use: 'svg-inline-loader'
            },
            {
                test: [/\.ttf$/, /\.woff2?$/, /\.eot$/],
                use: 'file-loader'
            }
        ]
    },
    plugins: [
        new ExtractTextPlugin('styles.packed.css'),
        new webpack.ProvidePlugin({
            $: 'jquery',
            jQuery: 'jquery',
            moment: 'moment'
        })
    ]
};
