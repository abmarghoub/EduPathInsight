import pika
import json
import logging
from typing import Dict, Any
from datetime import datetime
import os
from dotenv import load_dotenv

load_dotenv()

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class RabbitMQService:
    
    def __init__(self):
        self.host = os.getenv("RABBITMQ_HOST", "localhost")
        self.port = int(os.getenv("RABBITMQ_PORT", "5672"))
        self.user = os.getenv("RABBITMQ_USER", "guest")
        self.password = os.getenv("RABBITMQ_PASSWORD", "guest")
        self.exchange = os.getenv("RABBITMQ_EXCHANGE", "edupath.exchange")
        self.routing_key = os.getenv("RABBITMQ_ROUTING_KEY", "prediction.routing.key")
        self.connection = None
        self.channel = None
    
    async def connect(self):
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
    
    async def close(self):
        """Fermer la connexion"""
        if self.connection and not self.connection.is_closed:
            self.connection.close()
            logger.info("Connexion RabbitMQ fermée")
    
    def send_alert(self, alert_type: str, data: Dict[str, Any]):
        """Envoyer une alerte via RabbitMQ"""
        if not self.channel:
            logger.warning("Channel RabbitMQ non initialisé")
            return
        
        try:
            message = {
                'type': alert_type,
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
            logger.info(f"Alerte envoyée: {alert_type}")
        except Exception as e:
            logger.error(f"Erreur lors de l'envoi de l'alerte: {e}")
    
    def send_high_risk_alert(self, student_id: str, module_id: int, risk_level: str, prediction: Dict):
        """Envoyer une alerte pour un étudiant à haut risque"""
        self.send_alert('HIGH_RISK_STUDENT', {
            'student_id': student_id,
            'module_id': module_id,
            'risk_level': risk_level,
            'prediction': prediction
        })
    
    def send_risk_module_alert(self, module_id: int, risk_score: float, at_risk_count: int):
        """Envoyer une alerte pour un module à risque"""
        self.send_alert('RISK_MODULE', {
            'module_id': module_id,
            'risk_score': risk_score,
            'at_risk_students_count': at_risk_count
        })
