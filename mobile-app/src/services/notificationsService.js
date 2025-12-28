import api from './api';

export const notificationsService = {
  getMyNotifications: (recipientId) =>
    api.get(`/notifications/recipient/${recipientId}`),
  
  getUnreadNotifications: (recipientId) =>
    api.get(`/notifications/recipient/${recipientId}/unread`),
  
  getMyAlerts: (recipientId) =>
    api.get(`/notifications/alerts/recipient/${recipientId}`),
  
  getUnacknowledgedAlerts: (recipientId) =>
    api.get(`/notifications/alerts/recipient/${recipientId}/unacknowledged`),
  
  acknowledgeAlert: (alertId) =>
    api.post(`/notifications/alerts/${alertId}/acknowledge`),
};


