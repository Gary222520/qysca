<template>
  <div>
    <div style="margin-bottom: 10px">许可证使用次数 TOP 8</div>
    <a-spin :spinning="data.spinning" tip="图表加载中，请稍等...">
      <div id="license-chart"></div>
    </a-spin>
  </div>
</template>

<script setup>
import { reactive, defineExpose } from 'vue'
import * as echarts from 'echarts'

const data = reactive({
  spinning: true,
  licenseTypeNumberMap: []
})

let chart = null

const draw = (licenseTypeNumberMap, hasData) => {
  data.spinning = false
  data.licenseTypeNumberMap = licenseTypeNumberMap
  if (!hasData) return
  if (chart !== null) hide()
  chart = echarts.init(document.getElementById('license-chart'))
  const chartData = getData()
  const option = {
    tooltip: {},
    grid: {
      left: '3%',
      top: '0%',
      right: '4%',
      bottom: '0%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      boundaryGap: [0, 0.01]
    },
    yAxis: {
      type: 'category',
      data: chartData[0]
    },
    color: ['#00557c'],
    series: [
      {
        name: '许可证使用次数',
        type: 'bar',
        data: chartData[1],
        itemStyle: {
          color: function (params) {
            const color = ['#aa32be', '#0087be', '#00af8e', '#00d7a0', '#00bedc', '#ffd732', '#fe8389', '#ef0137']
            const index = params.dataIndex % 8
            return color[index]
          }
        }
      }
    ]
  }
  chart.setOption(option)
}

const getData = () => {
  const licenses = Object.keys(data.licenseTypeNumberMap)
  const values = Object.values(data.licenseTypeNumberMap)
  for (let i = 0; i < values.length - 1; i++) {
    for (let j = i + 1; j < values.length; j++) {
      if (values[i] > values[j]) {
        const key = licenses[i]
        const value = values[i]
        licenses[i] = licenses[j]
        values[i] = values[j]
        licenses[j] = key
        values[j] = value
      }
    }
  }
  return [licenses, values]
}

const hide = () => {
  data.spinning = true
  chart?.dispose()
  chart = null
}

defineExpose({ draw, hide })
</script>

<style>
#license-chart {
  width: 700px;
  height: 200px;
}
</style>
<style scoped src="@/atdv/spin.css"></style>
