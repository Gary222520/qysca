const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 9000,
    proxy: {
      '/api': {
        target: 'http://localhost:9090',
        ws: false,
        changeOrigin: true,
        pathRewrite: { '^/api': '' }
      }
    }
  }
})
