<template>
  <div>
    <div style="margin-bottom: 10px">应用漏洞风险 TOP 5</div>
    <a-spin :spinning="data.spinning" tip="图表加载中，请稍等...">
      <div id="app-vul-chart"></div>
    </a-spin>
  </div>
</template>

<script setup>
import { reactive, defineExpose } from 'vue'
import * as echarts from 'echarts'

const data = reactive({
  spinning: true,
  compareDTOList: []
})

let chart = null

const draw = (compareDTOList, hasData) => {
  data.spinning = false
  data.compareDTOList = compareDTOList
  if (!hasData) return
  if (chart !== null) hide()
  chart = echarts.init(document.getElementById('app-vul-chart'))
  const option = {
    legend: {},
    tooltip: {},
    grid: {
      left: '0%',
      top: '15%',
      right: '0%',
      bottom: '5%',
      containLabel: true
    },
    color: ['#ef0137', '#fe8389', '#ffd732', '#00bedc', '#00d7a0', '#0087be'],
    dataset: {
      dimensions: ['name', '严重', '高危', '中危', '低危', '无风险', '未知'],
      source: getData()
    },
    xAxis: {
      type: 'category',
      axisLabel: {
        show: true,
        interval: 0
      }
    },
    yAxis: {},
    series: [
      { type: 'bar', barWidth: '12' },
      { type: 'bar', barWidth: '12' },
      { type: 'bar', barWidth: '12' },
      { type: 'bar', barWidth: '12' },
      { type: 'bar', barWidth: '12' },
      { type: 'bar', barWidth: '12' }
    ]
  }
  chart.setOption(option)
}

const getData = () => {
  return data.compareDTOList.map((item) => {
    const res = { name: item.name + '\n' + item.version }
    res['严重'] = item.map.CRITICAL
    res['高危'] = item.map.HIGH
    res['中危'] = item.map.MEDIUM
    res['低危'] = item.map.LOW
    res['无风险'] = item.map.NONE
    res['未知'] = item.map.UNKNOWN
    return res
  })
}

const hide = () => {
  data.spinning = true
  chart?.dispose()
  chart = null
}

defineExpose({ draw, hide })
</script>

<style>
#app-vul-chart {
  width: 580px;
  height: 200px;
}
</style>
<style scoped src="@/atdv/spin.css"></style>
