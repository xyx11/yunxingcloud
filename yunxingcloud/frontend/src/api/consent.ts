import axios from 'axios'

export interface ClientInfo {
  clientId: string
  clientName: string
  scopes: { scope: string; description: string }[]
}

export async function fetchClientInfo(clientId: string): Promise<ClientInfo> {
  const res = await axios.get('/api/oauth2/consent/client', {
    params: { client_id: clientId },
  })
  return res.data
}
