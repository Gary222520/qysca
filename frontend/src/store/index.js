import { createStore } from 'vuex'

export default createStore({
  state: {
    projects: [
      {
        name: '项目1',
        data: [
          { version: '2.0.1', language: 'java', target: 'pom.xml', build: 'maven', time: 'xxx', comment: 'xxxxx' },
          { version: '2.0.0', language: 'java', target: 'pom.xml', build: 'maven', time: 'xxx', comment: 'xxxxx' },
          { version: '1.0.0', language: 'java', target: 'pom.xml', build: 'maven', time: 'xxx', comment: 'xxxxx' }
        ]
      },
      {
        name: '项目2',
        data: [
          { version: '2.0.0', language: 'python', target: 'zip', build: 'pip', time: 'xxx', comment: 'xxxxx' },
          { version: '1.0.0', language: 'python', target: 'zip', build: 'pip', time: 'xxx', comment: 'xxxxx' }
        ]
      }
    ]
  },
  getters: {
    getProjects(state) {
      return state.projects
    },
    getProjectByName: (state) => (name) => {
      return state.projects.find((item) => item.name === name)
    }
  },
  mutations: {},
  actions: {},
  modules: {}
})
