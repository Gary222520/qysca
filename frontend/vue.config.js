// const { defineConfig } = require('@vue/cli-service')
// module.exports = defineConfig({
//   transpileDependencies: true,
//   devServer: {
//     port: 9000,
//     proxy: {
//       '/qysca': {
//         target: 'http://localhost:9090',
//         ws: true,
//         changeOrigin: true,
//         pathRewrite: { '^/qysca': '' }
//       }
//     }
//   }
// })
module.exports = {
  devServer: {
    port: 9000,
    proxy: {
      '/qysca': {
        target: 'http://localhost:9090',
        ws: true,
        changeOrigin: true,
        pathRewrite: { '^/qysca': '' }
      }
    }
  }
}
