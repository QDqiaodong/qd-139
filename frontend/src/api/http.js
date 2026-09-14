import axios from 'axios'

const instance = axios.create({
  baseURL: '/api',
  timeout: 10000
})

instance.interceptors.response.use(
  response => response.data,
  error => {
    // 业务错误（含 409 冲突、400 校验失败）后端仍返回统一 JSON 体，直接放行给调用处按 success 处理；
    // 仅在网络异常/无响应体时抛出。
    if (error.response && error.response.data) {
      return error.response.data
    }
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

export default instance
