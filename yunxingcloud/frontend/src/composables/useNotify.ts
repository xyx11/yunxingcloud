import { useMessage } from 'naive-ui'

export function useNotify() {
  const message = useMessage()

  return {
    success(msg: string) {
      message.success(msg, { duration: 3000 })
    },
    error(msg: string) {
      message.error(msg, { duration: 5000 })
    },
    warning(msg: string) {
      message.warning(msg, { duration: 4000 })
    },
    info(msg: string) {
      message.info(msg, { duration: 3000 })
    },
  }
}
