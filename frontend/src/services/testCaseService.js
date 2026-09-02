import { apiClient } from './apiClient.js'
export const testCaseService={
 create:(projectId,value)=>apiClient.post(`/projects/${projectId}/test-cases`,value),
 list:(projectId,params={})=>{const q=new URLSearchParams(params).toString();return apiClient.get(`/projects/${projectId}/test-cases${q?`?${q}`:''}`)},
 get:(id)=>apiClient.get(`/test-cases/${id}`), update:(id,value)=>apiClient.put(`/test-cases/${id}`,value),
 executions:(id)=>apiClient.get(`/test-cases/${id}/executions`)
 ,execute:(id,value)=>apiClient.post(`/test-cases/${id}/execute`,value)
}
