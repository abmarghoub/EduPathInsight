import pika
import json
from typing import Dict, Any
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class NotificationService:
    
    def __init__(self, host: str, port: int, user: str, password: str, exchange: str, routing_key: str):
        self.host = host
        self.port = port
        self.user = user
        self.password = password
        self.exchange = exchange
        self.routing_key = routing_key
        self.connection = None
        self.channel = None
    
    def connect(self):
        """Établir la connexion RabbitMQ"""
        try:
            credentials = pika.PlainCredentials(self.user, self.password)
            parameters = pika.ConnectionParameters(
                host=self.host,
                port=self.port,
                credentials=credentials
            )
            self.connection = pika.BlockingConnection(parameters)
            self.channel = self.connection.channel()
            self.channel.exchange_declare(exchange=self.exchange, exchange_type='topic', durable=True)
            logger.info("Connexion RabbitMQ établie")
        except Exception as e:
            logger.error(f"Erreur de connexion RabbitMQ: {e}")
    
    def send_notification(self, message_type: str, data: Dict[str, Any]):
        """Envoyer une notification"""
        if not self.channel:
            self.connect()
        
        try:
            from datetime import datetime
            message = {
                'type': message_type,
                'timestamp': datetime.utcnow().isoformat(),
                'data': data
            }
            
            self.channel.basic_publish(
                exchange=self.exchange,
                routing_key=self.routing_key,
                body=json.dumps(message),
                properties=pika.BasicProperties(
                    delivery_mode=2,  # Faire persister le message
                )
            )
            logger.info(f"Notification envoyée: {message_type}")
        except Exception as e:
            logger.error(f"Erreur lors de l'envoi de la notification: {e}")
    
    def send_anomaly_notification(self, anomaly_data: Dict[str, Any]):
        """Envoyer une notification d'anomalie"""
        self.send_notification('ANOMALY_DETECTED', anomaly_data)
    
    def send_presence_notification(self, presence_data: Dict[str, Any]):
        """Envoyer une notification de présence"""
        self.send_notification('PRESENCE_RECORDED', presence_data)
    
    def send_activity_notification(self, activity_data: Dict[str, Any]):
        """Envoyer une notification d'activité"""
        self.send_notification('ACTIVITY_RECORDED', activity_data)
    
    def close(self):
        """Fermer la connexion"""
        if self.connection and not self.connection.is_closed:
            self.connection.close()
            logger.info("Connexion RabbitMQ fermée")

