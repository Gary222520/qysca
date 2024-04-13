<template>
  <div v-show="data.visible" style="margin-top: 20px">
    <a-descriptions>
      <a-descriptions-item label="可利用性分数">{{ data.cvss2?.exploitabilityScore || '-' }}</a-descriptions-item>
      <a-descriptions-item label="影响分数">{{ data.cvss2?.impactScore || '-' }}</a-descriptions-item>
      <a-descriptions-item label="版本">{{ data.cvss2?.version || '-' }}</a-descriptions-item>
      <a-descriptions-item label="严重性" span="3">
        <div v-if="!data.cvss2?.severity">-</div>
        <div v-if="data.cvss2?.severity === 'HIGH'">高危</div>
        <div v-if="data.cvss2?.severity === 'MEDIUM'">中等</div>
        <div v-if="data.cvss2?.severity === 'LOW'">低危</div>
      </a-descriptions-item>
      <a-descriptions-item label="向量" span="3">{{ data.cvss2?.vectorString || '-' }}</a-descriptions-item>
    </a-descriptions>
    <div style="display: flex; justify-content: space-between; margin-top: 20px">
      <div id="cvss2-chart-1"></div>
      <div id="cvss2-chart-2"></div>
    </div>
  </div>
</template>

<script setup>
import { reactive, defineExpose } from 'vue'
import * as echarts from 'echarts'

const data = reactive({
  visible: false,
  cvss2: {},
  chart1: null,
  chart2: null,
  count1: 0,
  count2: 0
})

const draw = (cvss2) => {
  data.visible = true
  data.cvss2 = cvss2
  if (data.chart1 !== null || data.chart2 !== null) hide()
  data.chart1 = echarts.init(document.getElementById('cvss2-chart-1'))
  data.chart2 = echarts.init(document.getElementById('cvss2-chart-2'))
  const option1 = {
    title: {
      text: '可利用性度量'
    },
    tooltip: {
      trigger: 'axis'
    },
    radar: {
      // shape: 'circle',
      splitNumber: 4,
      indicator: [
        { name: '访问向量', max: 4, color: '#6f005f' },
        { name: '访问复杂度', max: 4, color: '#6f005f' },
        { name: '认证', max: 4, color: '#6f005f' }
      ]
    },
    series: [
      {
        name: 'cvss2可利用性度量',
        type: 'radar',
        areaStyle: {
          color: '#6f005f'
        },
        data: getChart1Data(cvss2)
      }
    ]
  }
  const option2 = {
    title: {
      text: '影响性度量'
    },
    tooltip: {
      trigger: 'axis'
    },
    radar: {
      // shape: 'circle',
      splitNumber: 4,
      indicator: [
        { name: '保密性影响', max: 4, color: '#6f005f' },
        { name: '完整性影响', max: 4, color: '#6f005f' },
        { name: '可用性影响', max: 4, color: '#6f005f' }
      ]
    },
    series: [
      {
        name: 'cvss2影响性度量',
        type: 'radar',
        areaStyle: {
          color: '#6f005f'
        },
        data: getChart2Data(cvss2)
      }
    ]
  }
  data.chart1.setOption(option1)
  data.chart2.setOption(option2)
}

const hide = () => {
  data.visible = false
  data.chart1?.dispose()
  data.chart1 = null
  data.chart2?.dispose()
  data.chart2 = null
}

const getChart1Data = (cvss2) => {
  return [
    {
      name: '可利用性度量',
      value: [
        stringToNumber('accessVector', cvss2.accessVector),
        stringToNumber('accessComplexity', cvss2.accessComplexity),
        stringToNumber('authentication', cvss2.authentication)
      ],
      itemStyle: {
        color: '#6f005f'
      },
      tooltip: {
        trigger: 'item',
        valueFormatter: (value) => {
          data.count1 = (data.count1 + 1) % 3
          if (data.count1 === 1) return numberToString('accessVector', value)
          if (data.count1 === 2) return numberToString('accessComplexity', value)
          if (data.count1 === 0) return numberToString('authentication', value)
          return value
        }
      }
    }
  ]
}

const getChart2Data = (cvss2) => {
  return [
    {
      name: '影响性度量',
      value: [
        stringToNumber('confidentialityImpact', cvss2.confidentialityImpact),
        stringToNumber('integrityImpact', cvss2.integrityImpact),
        stringToNumber('availabilityImpact', cvss2.availabilityImpact)
      ],
      itemStyle: {
        color: '#6f005f'
      },
      tooltip: {
        trigger: 'item',
        valueFormatter: (value) => {
          data.count2 = (data.count1 + 1) % 3
          if (data.count1 === 1) return numberToString('confidentialityImpact', value)
          if (data.count1 === 2) return numberToString('integrityImpact', value)
          if (data.count1 === 0) return numberToString('availabilityImpact', value)
          return value
        }
      }
    }
  ]
}

const stringToNumber = (name, string) => {
  switch (name) {
    case 'accessVector':
      if (string === 'NETWORK') return 1
      if (string === 'ADJACENT_NETWORK') return 2
      if (string === 'LOCAL') return 3
      break
    case 'accessComplexity':
      if (string === 'HIGH') return 3
      if (string === 'MEDIUM') return 2
      if (string === 'LOW') return 1
      break
    case 'authentication':
      if (string === 'MULTIPLE') return 3
      if (string === 'SINGLE') return 2
      if (string === 'NONE') return 1
      break
    case 'confidentialityImpact':
      if (string === 'COMPLETE') return 3
      if (string === 'PARTIAL') return 2
      if (string === 'NONE') return 1
      break
    case 'integrityImpact':
      if (string === 'COMPLETE') return 3
      if (string === 'PARTIAL') return 2
      if (string === 'NONE') return 1
      break
    case 'availabilityImpact':
      if (string === 'COMPLETE') return 3
      if (string === 'PARTIAL') return 2
      if (string === 'NONE') return 1
      break
  }
}

const numberToString = (name, value) => {
  switch (name) {
    case 'accessVector':
      if (value === 1) return '网络'
      if (value === 2) return '领接网络'
      if (value === 3) return '本地'
      break
    case 'accessComplexity':
      if (value === 3) return '高'
      if (value === 2) return '中'
      if (value === 1) return '低'
      break
    case 'authentication':
      if (value === 3) return '多重'
      if (value === 2) return '单一'
      if (value === 1) return '无'
      break
    case 'confidentialityImpact':
      if (value === 3) return '完全'
      if (value === 2) return '部分'
      if (value === 1) return '无'
      break
    case 'integrityImpact':
      if (value === 3) return '完全'
      if (value === 2) return '部分'
      if (value === 1) return '无'
      break
    case 'availabilityImpact':
      if (value === 3) return '完全'
      if (value === 2) return '部分'
      if (value === 1) return '无'
      break
  }
}

defineExpose({ draw, hide })
</script>

<style>
#cvss2-chart-1 {
  width: 400px;
  height: 300px;
}
#cvss2-chart-2 {
  width: 400px;
  height: 300px;
}
</style>
<style scoped src="@/atdv/description.css"></style>
