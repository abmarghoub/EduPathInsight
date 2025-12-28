import PushNotification from 'react-native-push-notification';
import { Platform } from 'react-native';

class PushNotificationService {
  configure() {
    PushNotification.configure({
      onRegister: function (token) {
        console.log('TOKEN:', token);
        // Send token to your server
      },
      onNotification: function (notification) {
        console.log('NOTIFICATION:', notification);
        // Handle notification
      },
      permissions: {
        alert: true,
        badge: true,
        sound: true,
      },
      popInitialNotification: true,
      requestPermissions: Platform.OS === 'ios',
    });

    PushNotification.createChannel(
      {
        channelId: 'edupath-default',
        channelName: 'EduPath Notifications',
        channelDescription: 'Notifications pour EduPath Insight',
        playSound: true,
        soundName: 'default',
        importance: 4,
        vibrate: true,
      },
      (created) => console.log(`Channel created: ${created}`)
    );
  }

  localNotification(title, message, data = {}) {
    PushNotification.localNotification({
      channelId: 'edupath-default',
      title: title,
      message: message,
      data: data,
    });
  }

  cancelAllLocalNotifications() {
    PushNotification.cancelAllLocalNotifications();
  }
}

export default new PushNotificationService();


