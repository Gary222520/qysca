import { createStore } from 'vuex'

export default createStore({
  state: {
    projects: [
      {
        name: '项目1',
        data: [
          {
            version: '2.0.1',
            language: 'java',
            target: 'pom.xml',
            tool: 'maven',
            time: 'xxx',
            comment: 'xxxxx',
            popconfirm: false
          },
          {
            version: '2.0.0',
            language: 'java',
            target: 'pom.xml',
            tool: 'maven',
            time: 'xxx',
            comment: 'xxxxx',
            popconfirm: false
          },
          {
            version: '1.0.0',
            language: 'java',
            target: 'pom.xml',
            tool: 'maven',
            time: 'xxx',
            comment: 'xxxxx',
            popconfirm: false
          }
        ]
      },
      {
        name: '项目2',
        data: [
          {
            version: '2.0.0',
            language: 'python',
            target: 'zip',
            tool: 'pip',
            time: 'xxx',
            comment: 'xxxxx',
            popconfirm: false
          },
          {
            version: '1.0.0',
            language: 'python',
            target: 'zip',
            tool: 'pip',
            time: 'xxx',
            comment: 'xxxxx',
            popconfirm: false
          }
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
