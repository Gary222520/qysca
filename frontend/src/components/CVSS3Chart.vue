<template>
  <div v-show="data.visible" style="margin-top: 20px">
    <a-descriptions>
      <a-descriptions-item label="可利用性分数">{{ data.cvss3?.exploitabilityScore || '-' }}</a-descriptions-item>
      <a-descriptions-item label="影响分数">{{ data.cvss3?.impactScore || '-' }}</a-descriptions-item>
      <a-descriptions-item label="版本">{{ data.cvss3?.version || '-' }}</a-descriptions-item>
      <a-descriptions-item label="范围" span="3">
        <div v-if="!data.cvss3?.scope">-</div>
        <div v-if="data.cvss3?.scope === 'UNCHANGED'">不变</div>
        <div v-if="data.cvss3?.scope === 'CHANGED'">变化</div>
      </a-descriptions-item>
      <a-descriptions-item label="向量" span="3">{{ data.cvss3?.vectorString || '-' }}</a-descriptions-item>
    </a-descriptions>
    <div style="display: flex; justify-content: space-between; margin-top: 20px">
      <div id="cvss3-chart-1"></div>
      <div id="cvss3-chart-2"></div>
    </div>
  </div>
</template>

<script setup>
import { reactive, defineExpose } from 'vue'
import * as echarts from 'echarts'

const data = reactive({
  visible: false,
  cvss3: {},
  chart1: null,
  chart2: null,
  count1: 0,
  count2: 0
})

const draw = (cvss3) => {
  data.visible = true
  data.cvss3 = cvss3
  if (data.chart1 !== null || data.chart2 !== null) hide()
  data.chart1 = echarts.init(document.getElementById('cvss3-chart-1'))
  data.chart2 = echarts.init(document.getElementById('cvss3-chart-2'))
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
        { name: '攻击向量', max: 4, color: '#00557c' },
        { name: '攻击复杂度', max: 4, color: '#00557c' },
        { name: '所需权限', max: 4, color: '#00557c' },
        { name: '用户交互', max: 4, color: '#00557c' }
      ]
    },
    series: [
      {
        name: 'cvss3可利用性度量',
        type: 'radar',
        areaStyle: {
          color: '#00557c'
        },
        data: getChart1Data(cvss3)
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
        { name: '保密性影响', max: 4, color: '#00557c' },
        { name: '完整性影响', max: 4, color: '#00557c' },
        { name: '可用性影响', max: 4, color: '#00557c' }
      ]
    },
    series: [
      {
        name: 'cvss3影响性度量',
        type: 'radar',
        areaStyle: {
          color: '#00557c'
        },
        data: getChart2Data(cvss3)
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

const getChart1Data = (cvss3) => {
  return [
    {
      name: '可利用性度量',
      value: [
        stringToNumber('attackVector', cvss3.attackVector),
        stringToNumber('attackComplexity', cvss3.attackComplexity),
        stringToNumber('privilegesRequired', cvss3.privilegesRequired),
        stringToNumber('userInteraction', cvss3.userInteraction)
      ],
      itemStyle: {
        color: '#00557c'
      },
      tooltip: {
        trigger: 'item',
        valueFormatter: (value) => {
          data.count1 = (data.count1 + 1) % 4
          if (data.count1 === 1) return numberToString('attackVector', value)
          if (data.count1 === 2) return numberToString('attackComplexity', value)
          if (data.count1 === 3) return numberToString('privilegesRequired', value)
          if (data.count1 === 0) return numberToString('userInteraction', value)
          return value
        }
      }
    }
  ]
}

const getChart2Data = (cvss3) => {
  return [
    {
      name: '影响性度量',
      value: [
        stringToNumber('confidentialityImpact', cvss3.confidentialityImpact),
        stringToNumber('integrityImpact', cvss3.integrityImpact),
        stringToNumber('availabilityImpact', cvss3.availabilityImpact)
      ],
      itemStyle: {
        color: '#00557c'
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
    case 'attackVector':
      if (string === 'NETWORK') return 1
      if (string === 'ADJACENT_NETWORK') return 2
      if (string === 'LOCAL') return 3
      if (string === 'PHYSICAL') return 4
      break
    case 'attackComplexity':
      if (string === 'HIGH') return 3
      if (string === 'LOW') return 1
      break
    case 'privilegesRequired':
      if (string === 'HIGH') return 3
      if (string === 'LOW') return 2
      if (string === 'NONE') return 1
      break
    case 'userInteraction':
      if (string === 'REQUIRED') return 3
      if (string === 'NONE') return 1
      break
    case 'confidentialityImpact':
      if (string === 'HIGH') return 3
      if (string === 'LOW') return 2
      if (string === 'NONE') return 1
      break
    case 'integrityImpact':
      if (string === 'HIGH') return 3
      if (string === 'LOW') return 2
      if (string === 'NONE') return 1
      break
    case 'availabilityImpact':
      if (string === 'HIGH') return 3
      if (string === 'LOW') return 2
      if (string === 'NONE') return 1
      break
  }
}

const numberToString = (name, value) => {
  switch (name) {
    case 'attackVector':
      if (value === 1) return '网络'
      if (value === 2) return '领接网络'
      if (value === 3) return '本地'
      if (value === 4) return '物理'
      break
    case 'attackComplexity':
      if (value === 3) return '高'
      if (value === 1) return '低'
      break
    case 'privilegesRequired':
      if (value === 3) return '高'
      if (value === 2) return '低'
      if (value === 1) return '无'
      break
    case 'userInteraction':
      if (value === 3) return '需要'
      if (value === 1) return '无需'
      break
    case 'confidentialityImpact':
      if (value === 3) return '高'
      if (value === 2) return '低'
      if (value === 1) return '无'
      break
    case 'integrityImpact':
      if (value === 3) return '高'
      if (value === 2) return '低'
      if (value === 1) return '无'
      break
    case 'availabilityImpact':
      if (value === 3) return '高'
      if (value === 2) return '低'
      if (value === 1) return '无'
      break
  }
}

defineExpose({ draw, hide })
</script>

<style>
#cvss3-chart-1 {
  width: 400px;
  height: 300px;
}
#cvss3-chart-2 {
  width: 400px;
  height: 300px;
}
</style>
<style scoped src="@/atdv/description.css"></style>
