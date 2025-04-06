'use strict'
const path = require('path')
const defaultSettings = require('./src/settings.js')

const resolve = dir => path.join(__dirname, dir)
const name = defaultSettings.title
const port = process.env.port || process.env.npm_config_port || 9527

module.exports = {
  publicPath: './',
  outputDir: 'dist',
  assetsDir: 'static',
  lintOnSave: process.env.NODE_ENV === 'development',
  productionSourceMap: false,

  css: {
    loaderOptions: {
      sass: {
        sassOptions: { outputStyle: 'expanded' }
      }
    }
  },

  devServer: {
    host: '0.0.0.0',
    port,
    open: true,
    overlay: {
      warnings: false,
      errors: true
    },
    disableHostCheck: true,
    proxy: {
      [`/${process.env.VUE_APP_BACKEND_NAME}`]: {
        target: process.env.VUE_APP_PROXY,
        changeOrigin: true,
        pathRewrite: {
          [`^/${process.env.VUE_APP_BACKEND_NAME}`]: ''
        }
      }
    }
  },

  configureWebpack: {
    name,
    resolve: {
      alias: {
        '@': resolve('src')
      }
    }
  },

  chainWebpack(config) {
    // Preload configuration
    config.plugin('preload').tap(() => [{
      rel: 'preload',
      fileBlacklist: [/\.map$/, /hot-update\.js$/, /runtime\..*\.js$/],
      include: 'initial'
    }])

    config.plugins.delete('prefetch')

    // SVG loader configuration
    config.module
      .rule('svg')
      .exclude.add(resolve('src/icons'))
      .end()

    config.module
      .rule('icons')
      .test(/\.svg$/)
      .include.add(resolve('src/icons'))
      .end()
      .use('svg-sprite-loader')
      .loader('svg-sprite-loader')
      .options({
        symbolId: 'icon-[name]'
      })
      .end()

    // Add babel loader configuration
    config.module
      .rule('js')
      .use('babel-loader')
      .loader('babel-loader')
      .tap(options => ({
        ...options,
        plugins: [
          '@babel/plugin-proposal-optional-chaining',
          ...(options.plugins || [])
        ]
      }))

    // Production optimization
    config.when(process.env.NODE_ENV !== 'development', config => {
      config
        .plugin('ScriptExtHtmlWebpackPlugin')
        .after('html')
        .use('script-ext-html-webpack-plugin', [{
          inline: /runtime\..*\.js$/
        }])
        .end()

      config.optimization.splitChunks({
        chunks: 'all',
        cacheGroups: {
          libs: {
            name: 'chunk-libs',
            test: /[\\/]node_modules[\\/]/,
            priority: 10,
            chunks: 'initial'
          },
          elementUI: {
            name: 'chunk-elementUI',
            priority: 20,
            test: /[\\/]node_modules[\\/]_?element-ui(.*)/
          },
          commons: {
            name: 'chunk-commons',
            test: resolve('src/components'),
            minChunks: 3,
            priority: 5,
            reuseExistingChunk: true
          }
        }
      })

      config.optimization.runtimeChunk('single')
    })
  }
}
